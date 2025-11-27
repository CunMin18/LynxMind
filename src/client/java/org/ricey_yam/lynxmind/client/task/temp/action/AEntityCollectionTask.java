package org.ricey_yam.lynxmind.client.task.temp.action;

import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTaskType;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LAutoStrikeBackTask;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AEntityCollectionTask extends ATask {

    @Getter
    @Setter
    public static class EntityKillingQuota{
        private String entity_id;
        private List<ItemStackLite> needed_item;
        public EntityKillingQuota(String entity_id, List<ItemStackLite> needed_item) {
            this.entity_id = entity_id;
            this.needed_item = needed_item;
        }
    }

    public enum CollectionState{
        FINDING_LOOT,
        COLLECTING_LOOT,
        FINDING_AND_KILLING_ENTITY
    }

    private ItemEntity currentLootEntityTarget;

    private LAutoStrikeBackTask autoStrikeBackTask;
    private CollectionState collectionState;

    private LivingEntity currentTarget;
    private List<EntityKillingQuota> killingQuotas;
    public AEntityCollectionTask(List<EntityKillingQuota> killingQuotas, Action linkedAction) {
        super();
        setTaskType(ATaskType.ENTITY_COLLECTION);
        this.killingQuotas = killingQuotas;

        /// 由于该BTask需要依赖自动杀戮光环LTask
        /// 所以必须保证autoStrikeBackTask存在
        if(LynxMindEndTickEventManager.isTaskActive(LTaskType.AUTO_STRIKE_BACK)){
            this.autoStrikeBackTask = (LAutoStrikeBackTask)LynxMindEndTickEventManager.getTask(LTaskType.AUTO_STRIKE_BACK);
        }
        else {
            this.autoStrikeBackTask = new LAutoStrikeBackTask(5,10);
            LynxMindEndTickEventManager.registerTask(this.autoStrikeBackTask);
        }
    }
    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
        transitionToCollectingLoot();
    }

    @Override
    public void tick() {
        if(killingQuotas.isEmpty()){
            stop("收集任务已完成!");
            return;
        }

        switch (collectionState){
            /// 寻找是否有需要的物品的掉落物
            case FINDING_LOOT -> findingLootTick();

            /// 寻路到掉落物位置捡起掉落物
            case COLLECTING_LOOT -> collectingLootTick();

            /// 寻找并击杀相应实体以获取掉落物
            case FINDING_AND_KILLING_ENTITY -> findingAndKillingEntityTick();
        }
    }

    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.STOPPED;

        getAttackTask().disable();
        getPathingSubTask().disable();

        sendATaskStopMessage(cancelReason);

        LynxMindClient.sendModMessage(cancelReason);

        System.out.println("击杀生物获取材料的任务已停止: " + cancelReason);
    }

    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        getAttackTask().disable();
    }

    public void transitionToFindingLoot(){
        this.collectionState = CollectionState.FINDING_LOOT;
        getPathingSubTask().disable();
    }
    public void transitionToCollectingLoot(){
        this.collectionState = CollectionState.COLLECTING_LOOT;
    }
    public void transitionToFindingAndKillingEntity(){
        this.collectionState = CollectionState.FINDING_AND_KILLING_ENTITY;
    }

    private void findingLootTick(){
        var nearestLootEntity = EntityUtils.findNearestEntity(getPlayer(), ItemEntity.class,50,e -> e.getStack().getCount() > 0 && isNeededLoot(ItemUtils.getItemID(e.getStack())));
        if(nearestLootEntity != null) {
            currentLootEntityTarget = nearestLootEntity;
            transitionToCollectingLoot();
        }
        else {
            transitionToFindingAndKillingEntity();
        }
    }
    private void collectingLootTick(){
        if(!isLootDisappeared()){
            var newGoal = new GoalBlock(currentLootEntityTarget.getBlockPos());
            getPathingSubTask().enable(newGoal);
        }
        else{
            transitionToFindingLoot();
        }
    }
    private void findingAndKillingEntityTick(){
        System.out.println("find and killing entity tick");
        currentTarget = EntityUtils.findNearestEntity(getPlayer(), LivingEntity.class, 50, e -> isAttackTarget(EntityUtils.getEntityID(e)));
        if(currentTarget == null) {
            stop("附近没有可获取相应材料的生物!");
            return;
        }
        var originalTargetList = getAttackTask().getAttackTargetList();
        if(originalTargetList == null) originalTargetList  = new ArrayList<>();
        originalTargetList.add(currentTarget.getUuid());

        /// 靠近敌人 开启Killaura
        if(isNearbyTarget()){
            getPathingSubTask().disable();
            getAttackTask().enable(originalTargetList);
        }
        /// 不在敌人旁边 尝试寻路
        else{
            getPathingSubTask().enable(new GoalNear(currentTarget.getBlockPos(),2));
        }
    }

    /// 是否为攻击目标
    private boolean isAttackTarget(String entityID){
        for(var q : killingQuotas){
            if(q == null) continue;
            var qI = q.getEntity_id();
            if(qI.equals(entityID)) return true;
        }
        return false;
    }

    /// 掉落物是否是需要的
    private boolean isNeededLoot(String itemID){
        for(var quota : killingQuotas){
            var nItems = quota.getNeeded_item();
            for(var nItem : nItems){
                if(nItem.getItem_name().equals(itemID)) return true;
            }
        }
        return false;
    }

    /// 是否在敌人旁边
    private boolean isNearbyTarget(){
        if(currentTarget == null) return false;
        return currentTarget.distanceTo(getPlayer()) <= getAttackTask().getAttackRange();
    }

    /// 掉落物是否消失(被捡起)
    private boolean isLootDisappeared(){
        return currentLootEntityTarget == null || currentLootEntityTarget.getStack().getCount() <= 0;
    }
}


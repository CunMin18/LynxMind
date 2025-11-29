package org.ricey_yam.lynxmind.client.task.temp.action;

import baritone.api.pathing.goals.GoalNear;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AMurderTask extends ATask {
    private List<UUID> murderTargetUUIDs = new ArrayList<>();
    private LivingEntity currentTarget;

    public AMurderTask(List<UUID> murderTargetUUIDs, Action linkedAction) {
        super();
        setTaskType(ATaskType.MURDER);
        this.murderTargetUUIDs = filterToEntityWhichCanMurder(murderTargetUUIDs);
        this.linkedAction = linkedAction;
    }
    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
    }
    @Override
    public void tick() {
        if(murderTargetUUIDs.isEmpty()){
            stop("谋杀目标名单为空!");
            return;
        }

        if(currentTarget == null || currentTarget.getHealth() <= 0){
            currentTarget = EntityUtils.findNearestEntity(getPlayer(),LivingEntity.class,100,e -> murderTargetUUIDs.contains(e.getUuid()));
            if(currentTarget == null){
                System.out.println("无法找到指定目标!正在更换搜索方式!");
                currentTarget = EntityUtils.getEntityByUUID(murderTargetUUIDs.get(0));
                if(currentTarget == null) {
                    stop("更换搜索方式后依旧无法寻找到目标,任务终止!");
                    return;
                }
            }
        }

        if(isNearbyTarget()){
            var originalTargetList = getAttackTask().getAttackTargetList();
            if(originalTargetList == null) originalTargetList = new ArrayList<>();
            originalTargetList.add(currentTarget.getUuid());
            getAttackTask().enable(originalTargetList);
            currentTarget = null;
        }
        else{
            getPathingSubTask().enable(new GoalNear(currentTarget.getBlockPos(),2));
        }
    }
    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.STOPPED;

        getAttackTask().disable();
        getPathingSubTask().disable();
        getAttackTask().getAttackTargetList().removeAll(murderTargetUUIDs);
        murderTargetUUIDs.clear();

        sendATaskStopMessage(cancelReason);

        LynxMindClient.sendModMessage(cancelReason);

        System.out.println("谋杀任务已停止: " + cancelReason);
    }
    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        getPathingSubTask().disable();
    }

    /// 获取攻击目标
    private LivingEntity getPathingTarget(){
        var pathingTarget = EntityUtils.findNearestEntity(getPlayer(), LivingEntity.class,15,e -> murderTargetUUIDs.contains(e.getUuid()));
        if(pathingTarget == null){
            pathingTarget = EntityUtils.findNearestEntity(getPlayer(), LivingEntity.class,50,e -> murderTargetUUIDs.contains(e.getUuid()));
            if(pathingTarget == null) pathingTarget = EntityUtils.getEntityByUUID(murderTargetUUIDs.get(0));
        }
        return pathingTarget;
    }

    /// 处理UUID列表,筛选出有效实体的UUID
    private List<UUID> filterToEntityWhichCanMurder(List<UUID> murderTargetUUIDs){
        var result = new ArrayList<UUID>();
        for (int i = 0; i < murderTargetUUIDs.size(); i++) {
            var uuid = murderTargetUUIDs.get(i);
            var entity =  EntityUtils.getEntityByUUID(uuid);
            if(entity != null && entity.getHealth() > 0 && entity.canHit()) {
                result.add(uuid);
            }
        }
        return result;
    }

    /// 是否在敌人旁边
    private boolean isNearbyTarget(){
        if(currentTarget == null) return false;
        return currentTarget.distanceTo(getPlayer()) <= getAttackTask().getAttackRange();
    }
}

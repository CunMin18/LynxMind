package org.ricey_yam.lynxmind.client.task.temp.action;

import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.utils.RotationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.utils.game_ext.block.BlockUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.TransformUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.PlayerUtils;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ContainerHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemTagHelper;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class ABlockCollectionTask extends ATask {
    private List<String> targetBlockIDList = new ArrayList<>();
    /// 当前目标方块位置
    private BlockPos currentTargetBlockPos;
    private List<BlockPos> blackList = new ArrayList<>();
    /// 发现的掉落物位置
    private ItemEntity currentLootTarget;
    /// 当前玩家世界
    private World currentWorld;
    /// 状态
    private CollectingState collectingState;
    /// 需要的物品列表
    private List<ItemStackLite> neededItem;
    /// 重新寻找次数
    private int re_pathTicks;

    private boolean toolInHotbar;

    public enum CollectingState {
        MOVING_TO_LOOT,
        MOVING_TO_BLOCK,
        MINING_BLOCK,
    }

    public ABlockCollectionTask(List<ItemStackLite> neededItem, Action linkedAction) {
        super();
        this.taskType = ATaskType.BLOCK_COLLECTION;
        this.currentWorld = baritone.getPlayerContext().world();
        this.neededItem = neededItem;
        this.currentTaskState = TaskState.IDLE;
        this.collectingState = CollectingState.MOVING_TO_BLOCK;
        this.linkedAction = linkedAction;
    }

    @Override
    public void start() {
        if (neededItem.isEmpty() || currentWorld == null) {
            stop(currentWorld == null ? "玩家处于未知世界" : "需要收集的物品为空...");
            return;
        }

        nextBlock();
        blackList.clear();
        currentTaskState = TaskState.IDLE;
        transitionToMovingToBlock();
        updateBlockTargetList();
    }
    @Override
    public void tick() {
        try{
            var player = ClientUtils.getPlayer();

            /// 先检查任务状态是否正常
            if (currentTaskState != TaskState.IDLE) {
                stop("任务状态不对！");
                return;
            }

            /// 如果没有收集的东西 就完成任务
            if(neededItem.isEmpty()) {
                stop("收集任务已完成！");
                return;
            }

            switch(collectingState) {
                /// 寻路到方块
                case MOVING_TO_BLOCK -> movingToBlockTick();

                /// 移动到掉落物
                case MOVING_TO_LOOT -> movingToLootTick();

                /// 挖掘方块
                case MINING_BLOCK -> miningBlockTick();
            }
        }
        catch (Exception e){
            System.out.println("执行收集任务时，出现错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void stop(String stopReason) {
        getPathingSubTask().disable();
        getMineSubTask().disable();
        ContainerHelper.closeContainer();

        neededItem.clear();
        currentTargetBlockPos = null;
        collectingState = CollectingState.MOVING_TO_BLOCK;
        currentTaskState = TaskState.STOPPED;

        /// 发送任务停止事件给AI
        sendATaskStopMessage(stopReason);

        LynxMindClient.sendModMessage(stopReason);

        System.out.println("收集任务已停止：" + stopReason);
    }
    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        getPathingSubTask().disable();
        getMineSubTask().disable();
        ContainerHelper.closeContainer();
    }

    private void transitionToMovingToLoot(){
        collectingState = CollectingState.MOVING_TO_LOOT;
    }
    private void transitionToMovingToBlock(){
        collectingState = CollectingState.MOVING_TO_BLOCK;
    }
    private void transitionToMiningBlock(){
        collectingState = CollectingState.MINING_BLOCK;
    }

    private void movingToLootTick(){
        if(currentLootTarget != null && isNeededBlock(currentLootTarget.getStack()) && !currentLootTarget.getStack().isEmpty()){
            var newGoal = new GoalBlock(currentLootTarget.getBlockPos());
            getPathingSubTask().enable(newGoal);
        }
        else{
            currentLootTarget = null;
            transitionToMovingToBlock();
        }
    }
    private void movingToBlockTick(){
        var distanceToTargetBlock = currentTargetBlockPos != null ? TransformUtils.getDistance(getPlayer().getBlockPos(),currentTargetBlockPos) : 999;

        getMineSubTask().disable();

        /// 寻找是否有需要的物品的掉落物
        var nearestLootEntity = getNearestTargetLootEntity();
        if(nearestLootEntity != null && (nearestLootEntity.distanceTo(getPlayer()) < distanceToTargetBlock || isEnoughLoot(nearestLootEntity)) && nearestLootEntity.isOnGround()) {
            currentLootTarget = nearestLootEntity;
            transitionToMovingToLoot();
            return;
        }
        else currentLootTarget = null;

        /// 检查目标是否存在
        if(currentTargetBlockPos == null){
            nextBlock();
            return;
        }

        /// 检查目标是否正常
        var targetBlock = BlockUtils.getTargetBlock(currentTargetBlockPos);
        if(targetBlock == null || targetBlock.getDefaultState().isAir()) {
            nextBlock();
            return;
        }

        /// 确保能挖掘到方块
        if (isPlayerReachableBlock(currentTargetBlockPos) || isPlayerNearBlock(currentTargetBlockPos)) {
            transitionToMiningBlock();
            re_pathTicks = 0;
        }

        /// 如果寻路任务未启动 尝试启动
        /// 5秒内找不到路线 则将目标位置拉黑 寻找下一个目标
        else if(!getPathingSubTask().getCustomGoalProcess().isActive() || getPathingSubTask().getCustomGoalProcess().getGoal() == null){
            if(re_pathTicks <= 100 && getPathingSubTask().getCustomGoalProcess().getGoal() == null){
                getPathingSubTask().enable(new GoalNear(currentTargetBlockPos,2));
                re_pathTicks++;
            }
            else{
                blackList.add(currentTargetBlockPos);
                re_pathTicks = 0;
                currentTargetBlockPos = null;
            }
        }

    }
    private void miningBlockTick(){
        getPathingSubTask().disable();

        var miningBlockPos = getMineSubTask().getMiningBlockPos();

        /// 再次判断方块状态是否正常
        var targetBlock = BlockUtils.getTargetBlock(currentTargetBlockPos);
        if(targetBlock == null || targetBlock.getDefaultState().isAir()) {
            transitionToMovingToBlock();
            currentTargetBlockPos = null;
            System.out.println("目标方块异常");
            return;
        }

        /// 如果要收集的方块需要工具才能掉落 但玩家没工具 直接停止任务
        if(ClientUtils.getController().isBreakingBlock()){
            var miningBlockState = BlockUtils.getBlockState(miningBlockPos);
            if(miningBlockState != null && getMineSubTask().isEnabled() && BlockUtils.isPickaxeRequired(miningBlockState)){
                if(!hasSuitableTool(miningBlockPos) && !holdingBestTool(miningBlockPos)){
                    var miningBlockID = getMineSubTask().getMiningBlockID();
                    stop("背包内没有能够采集 " + miningBlockID + " 的工具!已停止任务!");
                    return;
                }
            }
        }

        /// 方块不在旁边,尝试寻路
        if (!isPlayerReachableBlock(currentTargetBlockPos) && !isPlayerNearBlock(currentTargetBlockPos)) {
            System.out.println("无法触及方块");
            transitionToMovingToBlock();
            return;
        }

        /// 开始挖掘
        if (currentTargetBlockPos != null) {
            getMineSubTask().enable(currentTargetBlockPos);
        }
    }

    /// 更新收集任务
    public void onBlockDropCollected(String itemName, int count){
        if(neededItem.isEmpty()) return;
        for (int i = neededItem.size() - 1; i >= 0; i--) {
            var item = neededItem.get(i);
            if(item == null) continue;
            if (ItemTagHelper.isFuzzyMatch(item.getItem_name(),itemName) && item.getCount() >= 0) {
                item.setCount(item.getCount() - count);
                if (item.getCount() <= 0) {
                    neededItem.remove(i);
                }
                return;
            }
        }
        updateBlockTargetList();
    }

    /// 是否为需要的物品
    private boolean isNeededBlock(ItemStack stack){
        for (int i = neededItem.size() - 1; i >= 0; i--) {
            var nItem = neededItem.get(i);
            if (ItemTagHelper.isFuzzyMatch(nItem.getItem_name(),ItemUtils.getItemID(stack)) && nItem.getCount() >= 0) {
                return true;
            }
        }
        return false;
    }

    /// 寻找下一个方块的位置
    private void nextBlock() {
        if(targetBlockIDList.isEmpty()){
            updateBlockTargetList();
        }
        currentTargetBlockPos = BlockUtils.findNearestBlock(baritone.getPlayerContext().player(), 50,pos -> !blackList.contains(pos) && targetBlockIDList.contains(BlockUtils.getBlockID(pos)));
        if(currentTargetBlockPos == null){
            currentLootTarget = getNearestTargetLootEntity();
            if(currentLootTarget == null) {
                stop("附近没有指定方块!");
            }
            else transitionToMovingToLoot();
        }
    }

    private ItemEntity getNearestTargetLootEntity(){
        return EntityUtils.findNearestEntity(getPlayer(),ItemEntity.class,15,ie -> !ie.getStack().isEmpty() && isNeededBlock(ie.getStack()));
    }

    /// 玩家靠近方块
    private boolean isPlayerNearBlock(BlockPos pos) {
        if (pos == null) return false;
        var player = getPlayer();
        double distSq = player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        var reachDistance = baritone.getPlayerContext().playerController().getBlockReachDistance();
        return distSq < reachDistance * reachDistance;
    }

    /// 玩家能摸到方块
    private boolean isPlayerReachableBlock(BlockPos pos) {
        if (pos == null) return false;
        double maxReach = baritone.getPlayerContext().playerController().getBlockReachDistance();
        return RotationUtils.reachable(baritone.getPlayerContext(), pos, maxReach, false).isPresent();
    }

    /// 方块被挖掘
    private boolean isBlockGone(BlockPos pos) {
        if (pos == null) return true;
        var state = BlockUtils.getBlockState(pos);
        return state == null || state.isAir();
    }

    /// 更新收集名单
    private void updateBlockTargetList(){
        targetBlockIDList.clear();
        for(var item : neededItem){
            if(item == null) continue;
            var itemName = item.getItem_name();
            var matchedBlockList = ItemTagHelper.getTagList(itemName);
            if (matchedBlockList != null && !matchedBlockList.isEmpty()) {
                targetBlockIDList.addAll(matchedBlockList);
            }
        }
    }

    /// 是否有合适的工具
    private boolean hasSuitableTool(BlockPos targetPos){
        var bestToolID = PlayerUtils.getBestToolIDInInventory(targetPos);
        return bestToolID != null && !bestToolID.isEmpty() && !bestToolID.contains("air");
    }

    /// 是否握着最佳武器
    private boolean holdingBestTool(BlockPos targetPos){
        var holdingItemStack = PlayerUtils.getHoldingItemStack();
        var bestToolID = PlayerUtils.getBestToolIDInInventory(targetPos);
        return ItemUtils.getItemID(holdingItemStack).equals(bestToolID);
    }

    /// 掉落物是否能满足任一物品的收集要求
    private boolean isEnoughLoot(ItemEntity lootTarget){
        if(lootTarget == null) return false;
        for(var item : neededItem){
            var allStackEntities = EntityUtils.scanAllEntity(getPlayer(),ItemEntity.class,5,ie -> !ie.getStack().isEmpty() && ItemUtils.getItemID(ie.getStack()).equals(item.getItem_name()));
            var itemCountInTotalAt = new AtomicInteger();
            allStackEntities.forEach(e -> itemCountInTotalAt.addAndGet(e.getStack().getCount()));
            if(itemCountInTotalAt.get() >= item.getCount() && item.getItem_name().equals(ItemUtils.getItemID(lootTarget.getStack()))) {
                return true;
            }
        }
        return false;
    }
}
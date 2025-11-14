package org.ricey_yam.lynxmind.client.task.baritone;

import baritone.api.IBaritone;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.ai.LynxJsonHandler;
import org.ricey_yam.lynxmind.client.ai.json.action.Action;
import org.ricey_yam.lynxmind.client.ai.json.event.player.child.PlayerBaritoneTaskStop;
import org.ricey_yam.lynxmind.client.ai.json.game_info.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.utils.game.BlockUtils;
import org.ricey_yam.lynxmind.client.utils.game.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class BCollectionTask extends BTask {
    private final IBaritone baritone;
    /// 当前目标方块位置
    private BlockPos currentTarget;
    /// 当前玩家世界
    private World currentWorld;
    /// 状态
    private CollectingState collectingState;
    /// 需要的物品列表
    private List<ItemStackLite> neededItem;
    /// 正在挖掘的方块ID
    private String miningBlockName;
    /// 正在挖掘的方块位置
    private BlockPos miningBlockPos;
    /// 重新寻找次数
    private int refindCount;

    public enum CollectingState {
        MOVING_TO_BLOCK,
        MINING_BLOCK,
    }

    public BCollectionTask(List<ItemStackLite> neededItem, Action linkedAction) {
        this.taskType = BTaskType.COLLECTION;
        this.baritone = BaritoneManager.getClientBaritone();
        this.currentWorld = baritone.getPlayerContext().world();
        this.neededItem = neededItem;
        this.currentTaskState = TaskState.IDLE;
        this.collectingState = CollectingState.MOVING_TO_BLOCK;
        this.linkedAction = linkedAction;
    }

    @Override
    public void start() {
        if (!currentTaskState.equals(TaskState.IDLE)) return;
        if (neededItem.isEmpty() || currentWorld == null) {
            stop(currentWorld == null ? "玩家处于未知世界" : "需要收集的物品为空...");
            return;
        }

        nextBlock();
        if (currentTarget == null) {
            System.out.println("未找到目标方块，正在重新寻找...");
            if(refindCount < 4){
                new Thread(() -> {
                    refindCount++;
                    try {
                        Thread.sleep(1000);
                        start();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
            else stop("附近没有目标方块，已自动取消BTASK！");
            return;
        }
        else{
            refindCount = 0;
        }

        if (baritone.getGetToBlockProcess() == null) {
            System.out.println("GetToBlockProcess 未初始化，重试");
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    start();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return;
        }

        currentTaskState = TaskState.IDLE;
        collectingState = CollectingState.MOVING_TO_BLOCK;
        Block startBlock = getTargetBlock(currentTarget);
        if (startBlock == null) {
            System.out.println("启动时目标方块为空，重新寻找");
            nextBlock();
            start();
            return;
        }
        baritone.getGetToBlockProcess().getToBlock(startBlock);
    }

    @Override
    public void tick() {
        try{
            /// 如果没有收集的东西 就完成任务
            if(neededItem.isEmpty()) {
                LynxMindClient.sendModMessage("收集任务已完成！");
                stop("收集任务已完成！");
                return;
            }

            if (currentTaskState != TaskState.IDLE || currentTarget == null || baritone.getGetToBlockProcess() == null) {
                if(currentTarget == null && refindCount < 4) {
                    new Thread(() -> {
                        try {
                            refindCount++;
                            Thread.sleep(500);
                            nextBlock();
                        }
                        catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                    return;
                }
                return;
            }
            else refindCount = 0;

            tickTimer++;
            var targetBlockState = BlockUtils.getBlockState(currentTarget);
            var targetBlock = getTargetBlock(currentTarget);
            if (collectingState == CollectingState.MOVING_TO_BLOCK) {
                if (targetBlock == null) {
                    nextBlock();
                }
                else {
                    if (isPlayerNearBlock(currentTarget)) {
                        miningBlockName = BlockUtils.getBlockName(currentTarget);
                        miningBlockPos = currentTarget;
                        collectingState = CollectingState.MINING_BLOCK;
                        baritone.getMineProcess().mine(targetBlock);
                    }
                    else{
                        baritone.getGetToBlockProcess().getToBlock(targetBlock);
                    }
                }
            }
            else if (collectingState == CollectingState.MINING_BLOCK) {
                if (isBlockGone(currentTarget) || targetBlock == null || currentTarget == null) {
                    nextBlock();
                }
                else {
                    baritone.getMineProcess().mine(targetBlock);
                    miningBlockName = BlockUtils.getBlockName(currentTarget);
                    miningBlockPos = currentTarget;
                }
            }
        }
        catch (Exception e){
            System.out.println("执行收集任务时，出现错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop(String stopReason) {
        if (baritone.getGetToBlockProcess() != null && baritone.getGetToBlockProcess().isActive()) {
            baritone.getGetToBlockProcess().getToBlock(getTargetBlock(currentTarget));
        }
        if (baritone.getMineProcess() != null && baritone.getMineProcess().isActive()) {
            baritone.getMineProcess().cancel();
        }
        baritone.getCustomGoalProcess().setGoal(null);
        baritone.getPathingBehavior().cancelEverything();

        neededItem.clear();
        currentTarget = null;
        miningBlockPos = null;
        miningBlockName = "";
        collectingState = CollectingState.MOVING_TO_BLOCK;
        currentTaskState = TaskState.FINISHED;

        /// 发送任务停止事件给AI
        if(stopReason != null && !stopReason.isEmpty() && AIServiceManager.isServiceActive && AIServiceManager.isTaskActive() && linkedAction != null){
            var bTaskStopEvent = new PlayerBaritoneTaskStop(linkedAction,stopReason);
            var serialized = LynxJsonHandler.serialize(bTaskStopEvent);
            Objects.requireNonNull(AIServiceManager.sendAndReceiveReplyAsync(serialized)).whenComplete((reply, error) -> ChatManager.handleAIReply(reply));
        }

        System.out.println("收集任务已停止：" + stopReason);
    }

    /// 更新收集任务
    public void onItemCollected(String itemName,int count){
        for (int i = neededItem.size() - 1; i >= 0; i--) {
            var item = neededItem.get(i);
            if (ItemUtils.ItemTagMatcher.isFuzzyMatch(item.getItem_name(),itemName) && item.getCount() >= 0) {
                item.setCount(item.getCount() - count);
                if (item.getCount() <= 0) {
                    neededItem.remove(i);
                }
                return;
            }
        }
    }

    /// 寻找下一个方块的位置
    private void nextBlock() {
        try {
            var blockNameList = new ArrayList<String>();
            for(var item : neededItem){
                blockNameList.add(item.getItem_name());
            }
            currentTarget = BlockUtils.findNearestBlock(baritone.getPlayerContext().player(), blockNameList, 80);
            if (currentTarget != null && baritone.getGetToBlockProcess() != null) {
                var targetBlock = getTargetBlock(currentTarget);
                if(targetBlock != null){
                    collectingState = CollectingState.MOVING_TO_BLOCK;
                    baritone.getGetToBlockProcess().getToBlock(targetBlock);
                    return;
                }
            }
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    nextBlock();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        catch (Exception e){
            System.out.println("寻找下一个方块时，遇到错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private Block getTargetBlock(BlockPos pos) {
        if (pos == null) return null;
        var state = BlockUtils.getBlockState(pos);
        return state != null ? state.getBlock() : null;
    }

    private boolean isPlayerNearBlock(BlockPos pos) {
        if (pos == null || baritone.getPlayerContext().player() == null) return false;
        return baritone.getPlayerContext().player().getPos().distanceTo(pos.toCenterPos()) < 4D;
    }

    private boolean isBlockGone(BlockPos pos) {
        if (pos == null) return true;
        var state = BlockUtils.getBlockState(pos);
        return state == null || state.isAir();
    }
}
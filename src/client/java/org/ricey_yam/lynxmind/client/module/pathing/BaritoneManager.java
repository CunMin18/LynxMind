package org.ricey_yam.lynxmind.client.module.pathing;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.module.pathing.status.sub.*;
import org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LFunctionHubTask;
import org.ricey_yam.lynxmind.client.task.temp.action.ABlockCollectionTask;
import org.ricey_yam.lynxmind.client.module.pathing.status.BStatus;
import org.ricey_yam.lynxmind.client.task.temp.action.ACraftingTask;
import org.ricey_yam.lynxmind.client.task.temp.action.ATaskType;

import java.util.Objects;

@Getter
@Setter
public class BaritoneManager {
    public static ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    public static IBaritone getClientBaritone() {
        if (getPlayer() == null) {
            return BaritoneAPI.getProvider().getPrimaryBaritone();
        }
        return BaritoneAPI.getProvider().getBaritoneForPlayer(getPlayer());
    }

    /// 获取当前Baritone状态
    public static BStatus getCurrentBStatus() {
        var baritone = getClientBaritone();

        /// 寻路
        if(isPathingTaskActive()){
            var pathingGoal = baritone.getPathingBehavior().getGoal();
            if(pathingGoal instanceof GoalBlock goalBlock){
                return new BPathingToGoalStatus(goalBlock);
            }
            else if(pathingGoal instanceof GoalXZ goalXZ){
                return new BPathingToGoalXZStatus(goalXZ.getX(),goalXZ.getZ());
            }
        }
        /// 收集
        if(isBlockCollectionTaskActive()){
            var task = LynxMindEndTickEventManager.getTask(ATaskType.BLOCK_COLLECTION);
            if(task instanceof ABlockCollectionTask collectionTask){
                if(collectionTask.getCurrentTargetBlockPos() != null){

                    var isNeededBlock = false;
                    var miningBlockID = Objects.requireNonNull(LFunctionHubTask.getActiveTask()).getMineSubTask().getMiningBlockID();
                    for(var item : collectionTask.getNeededItem()){
                        if(item == null) continue;
                        if (item.getItem_name().equals(miningBlockID)) {
                            isNeededBlock = true;
                            break;
                        }
                    }
                    var miningNeededBlock = collectionTask.getCollectingState() == ABlockCollectionTask.CollectingState.MINING_BLOCK && isNeededBlock;
                    var miningUnneededBlock = collectionTask.getCollectingState() == ABlockCollectionTask.CollectingState.MINING_BLOCK && !isNeededBlock;
                    if(miningNeededBlock){
                        return new BMiningStatus(miningBlockID);
                    }
                    else if(collectionTask.getCollectingState() == ABlockCollectionTask.CollectingState.MOVING_TO_BLOCK || miningUnneededBlock){
                        return new BFindingNeededBlocksStatus(collectionTask.getCurrentTargetBlockPos(),collectionTask.getNeededItem());
                    }
                }
            }
        }
        /// 制作
        if(isCraftingTaskActive()){
            var task = LynxMindEndTickEventManager.getTask(ATaskType.CRAFTING);
            if(task instanceof ACraftingTask craftingTask){
                return new BCraftingStatus(craftingTask.getTo_craft(),craftingTask.getCraft_failed(),craftingTask.getCraft_success());
            }
        }
        return new BStatus();
    }

    /// 停止所有BTask
    public static void stopAllTasks(String reason) {
        LynxMindEndTickEventManager.cleanTempTasks(reason);
    }

    /// 停止所有需要寻路的BTask
    public static void stopPathingRelatedTasks(String reason) {
        LynxMindEndTickEventManager.unregisterTask(ATaskType.PATHING,reason);
        LynxMindEndTickEventManager.unregisterTask(ATaskType.BLOCK_COLLECTION,reason);
        LynxMindEndTickEventManager.unregisterTask(ATaskType.CRAFTING,reason);
        LynxMindEndTickEventManager.unregisterTask(ATaskType.ENTITY_COLLECTION,reason);
        LynxMindEndTickEventManager.unregisterTask(ATaskType.MURDER,reason);
    }

    public static boolean isPathingTaskActive(){
        return LynxMindEndTickEventManager.isTaskActive(ATaskType.PATHING);
    }

    public static boolean isBlockCollectionTaskActive(){
        return LynxMindEndTickEventManager.isTaskActive(ATaskType.BLOCK_COLLECTION);
    }

    public static boolean isCraftingTaskActive(){
        return LynxMindEndTickEventManager.isTaskActive(ATaskType.CRAFTING);
    }
}

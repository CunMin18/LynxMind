package org.ricey_yam.lynxmind.client.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.ricey_yam.lynxmind.client.baritone.status.sub.BFindingNeededBlocks;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.baritone.BCollectionTask;
import org.ricey_yam.lynxmind.client.baritone.status.BStatus;
import org.ricey_yam.lynxmind.client.baritone.status.sub.BMiningStatus;
import org.ricey_yam.lynxmind.client.baritone.status.sub.BPathingToGoalStatus;
import org.ricey_yam.lynxmind.client.baritone.status.sub.BPathingToGoalXZStatus;
import org.ricey_yam.lynxmind.client.task.baritone.BTaskType;

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

    public static BStatus getCurrentBStatus() {
        var baritone = getClientBaritone();

        if(isPathingTaskActive()){
            var pathingGoal = baritone.getPathingBehavior().getGoal();
            if(pathingGoal instanceof GoalBlock goalBlock){
                return new BPathingToGoalStatus(goalBlock);
            }
            else if(pathingGoal instanceof GoalXZ goalXZ){
                return new BPathingToGoalXZStatus(goalXZ.getX(),goalXZ.getZ());
            }
        }
        if(isCollectionTaskActive()){
            var collectingTask = LynxMindEndTickEventManager.getTask(BTaskType.COLLECTION);
            if(collectingTask instanceof BCollectionTask bCT){
                if(bCT.getCurrentTarget() != null){

                    var isNeededBlock = false;
                    for(var item : bCT.getNeededItem()){
                        if(item == null) continue;
                        if (item.getItem_name().equals(bCT.getMiningBlockName())) {
                            isNeededBlock = true;
                            break;
                        }
                    }

                    var miningNeededBlock = bCT.getCollectingState() == BCollectionTask.CollectingState.MINING_BLOCK && isNeededBlock;
                    var miningUnneededBlock = bCT.getCollectingState() == BCollectionTask.CollectingState.MINING_BLOCK && !isNeededBlock;
                    if(miningNeededBlock){
                        return new BMiningStatus(bCT.getMiningBlockName());
                    }
                    else if(bCT.getCollectingState() == BCollectionTask.CollectingState.MOVING_TO_BLOCK || miningUnneededBlock){
                        return new BFindingNeededBlocks(bCT.getCurrentTarget(),bCT.getNeededItem());
                    }
                }
            }
        }
        return new BStatus();
    }

    public static void stopAllTasks(String reason) {
        LynxMindEndTickEventManager.cleanAllTasks(reason);
    }

    public static void stopPathingRelatedTasks(String reason) {
        LynxMindEndTickEventManager.unregisterTask(BTaskType.PATHING,reason);
        LynxMindEndTickEventManager.unregisterTask(BTaskType.COLLECTION,reason);
        LynxMindEndTickEventManager.unregisterTask(BTaskType.CRAFTING,reason);
    }

    public static boolean isPathingTaskActive(){
        return LynxMindEndTickEventManager.isTaskActive(BTaskType.PATHING);
    }

    public static boolean isCollectionTaskActive(){
        return LynxMindEndTickEventManager.isTaskActive(BTaskType.COLLECTION);
    }
}

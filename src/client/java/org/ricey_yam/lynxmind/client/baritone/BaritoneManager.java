package org.ricey_yam.lynxmind.client.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.ricey_yam.lynxmind.client.baritone.status.child.BFindingNeededBlocks;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.baritone.BCollectionTask;
import org.ricey_yam.lynxmind.client.baritone.status.BStatus;
import org.ricey_yam.lynxmind.client.baritone.status.child.BMiningStatus;
import org.ricey_yam.lynxmind.client.baritone.status.child.BPathingToGoalStatus;
import org.ricey_yam.lynxmind.client.baritone.status.child.BPathingToGoalXZStatus;
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
        var pathingGoal = baritone.getPathingBehavior().getGoal();
        var collectingTask = LynxMindEndTickEventManager.getTask(BTaskType.COLLECTION);
        if(pathingGoal != null){
            if(pathingGoal instanceof GoalBlock goalBlock){
                return new BPathingToGoalStatus(goalBlock);
            }
            else if(pathingGoal instanceof GoalXZ goalXZ){
                return new BPathingToGoalXZStatus(goalXZ.getX(),goalXZ.getZ());
            }
        }
        if(collectingTask != null){
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
}

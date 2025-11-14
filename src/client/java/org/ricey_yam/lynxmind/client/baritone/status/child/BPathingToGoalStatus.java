package org.ricey_yam.lynxmind.client.baritone.status.child;

import baritone.api.pathing.goals.GoalBlock;
import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.baritone.status.BStatus;
import org.ricey_yam.lynxmind.client.baritone.status.BStatusType;

@Getter
@Setter
public class BPathingToGoalStatus extends BStatus {
    private int x;
    private int y;
    private int z;
    public BPathingToGoalStatus(GoalBlock goalBlock){
        this.type =  BStatusType.BSTATUS_PATHING_TO_GOAL;
        this.x = goalBlock.x;
        this.y = goalBlock.y;
        this.z = goalBlock.z;
    }
}

package org.ricey_yam.lynxmind.client.baritone.status.child;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.baritone.status.BStatus;
import org.ricey_yam.lynxmind.client.baritone.status.BStatusType;

@Getter
@Setter
public class BPathingToGoalXZStatus extends BStatus {
    private int x;
    private int z;
    public BPathingToGoalXZStatus(int x, int z){
        this.type = BStatusType.BSTATUS_PATHING_TO_XZ;
        this.x = x;
        this.z = z;
    }
}

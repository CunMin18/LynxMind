package org.ricey_yam.lynxmind.client.module.pathing.status.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.module.pathing.status.BStatus;
import org.ricey_yam.lynxmind.client.module.pathing.status.BStatusType;

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

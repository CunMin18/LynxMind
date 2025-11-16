package org.ricey_yam.lynxmind.client.baritone.status.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.baritone.status.BStatus;
import org.ricey_yam.lynxmind.client.baritone.status.BStatusType;

@Getter
@Setter
public class BMiningStatus extends BStatus {
    private String mining_block_name;
    public BMiningStatus(String mining_block_name){
        this.type = BStatusType.BSTATUS_MINING;
        this.mining_block_name = mining_block_name;
    }
}

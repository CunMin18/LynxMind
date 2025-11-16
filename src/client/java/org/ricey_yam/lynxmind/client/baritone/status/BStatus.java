package org.ricey_yam.lynxmind.client.baritone.status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BStatus {
    protected BStatusType type;
    public BStatus(){
        this.type = BStatusType.NONE;
    }
}

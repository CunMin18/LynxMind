package org.ricey_yam.lynxmind.client.ai.json.action;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;

@Getter
@Setter
public class Action {
    protected ActionType type;
    public boolean invoke(){
        return true;
    }
}

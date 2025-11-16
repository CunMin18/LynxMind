package org.ricey_yam.lynxmind.client.ai.message.action;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Action {
    protected ActionType type;
    public boolean invoke(){
        return true;
    }
}

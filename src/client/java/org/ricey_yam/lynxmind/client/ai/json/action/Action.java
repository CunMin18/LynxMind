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

    protected void stopAllActions() {
        var baritone = BaritoneManager.getClientBaritone();
        if(baritone != null){
            BaritoneManager.stopAllTasks();
        }
        else{
            System.out.println("baritone is null");
        }
    }
}

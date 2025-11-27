package org.ricey_yam.lynxmind.client.ai.message.action.sub;

import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;

public class PlayerStopATaskAction extends Action {
    @Override
    public boolean invoke() {
        BaritoneManager.stopAllTasks("AI手动停止了全部 ActionTask");
        return super.invoke();
    }
}

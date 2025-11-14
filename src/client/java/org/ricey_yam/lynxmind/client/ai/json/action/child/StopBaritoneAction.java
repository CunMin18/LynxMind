package org.ricey_yam.lynxmind.client.ai.json.action.child;

import org.ricey_yam.lynxmind.client.ai.json.action.Action;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;

public class StopBaritoneAction extends Action {
    @Override
    public boolean invoke() {
        BaritoneManager.stopAllTasks("AI手动停止了全部 BaritoneTask");
        return super.invoke();
    }
}

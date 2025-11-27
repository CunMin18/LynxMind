package org.ricey_yam.lynxmind.client.task.non_temp.lynx;

import baritone.api.IBaritone;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.task.non_temp.NonTempTask;

public abstract class LTask extends NonTempTask<LTaskType> {
    protected IBaritone getBaritone(){
        return BaritoneManager.getClientBaritone();
    }
}

package org.ricey_yam.lynxmind.client.task.non_temp.life;

import baritone.api.IBaritone;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.module.pathing.BaritoneManager;
import org.ricey_yam.lynxmind.client.task.non_temp.NonTempTask;

public abstract class LTask extends NonTempTask<LTaskType> {
    public LTask(Object... args){

    }

    protected IBaritone getBaritone(){
        return BaritoneManager.getClientBaritone();
    }

    protected static LTask getActiveLTask(LTaskType lTaskType,Class<? extends LTask> lTaskClass,Object... args){
        if(LynxMindEndTickEventManager.isTaskActive(lTaskType)){
            var task = LynxMindEndTickEventManager.getTask(lTaskType);
            if(!(task instanceof LTask lTask)) return null;
            return lTask;
        }
        else{
            try {
                var registered = lTaskClass.getDeclaredConstructor().newInstance(args);;
                LynxMindEndTickEventManager.registerTask(registered);
                return registered;
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }
}

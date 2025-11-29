package org.ricey_yam.lynxmind.client.module.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEvent;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTaskType;
import org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LAutoStrikeBackTask;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AIAddStrikeBackTargetEvent extends AIEvent {
    private List<String> UUID_str_list;
    @Override
    public void onReceive() {
        for(var u : UUID_str_list){
            var ACT_UUID = UUID.fromString(u);
            if(LynxMindEndTickEventManager.isTaskActive(LTaskType.AUTO_STRIKE_BACK)){
                var strikeBackTask = (LAutoStrikeBackTask) LynxMindEndTickEventManager.getTask(LTaskType.AUTO_STRIKE_BACK);
                if(strikeBackTask != null){
                    strikeBackTask.getAdditionalStrikeBackTarget().add(ACT_UUID);
                    LynxMindClient.sendModMessage("AI添加了反击对象: " + u);
                }
            }
        }
    }
}

package org.ricey_yam.lynxmind.client.module.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIJsonHandler;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEvent;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.sub.PlayerReplyStrikeBackTargetListEvent;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTaskType;
import org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LAutoStrikeBackTask;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class AIGetStrikeBackTargetListEvent extends AIEvent {
    private String UUID_str;
    @Override
    public void onReceive() {
        var ACT_UUID = UUID.fromString(UUID_str);
        if(LynxMindEndTickEventManager.isTaskActive(LTaskType.AUTO_STRIKE_BACK)){
            var strikeBackTask = (LAutoStrikeBackTask) LynxMindEndTickEventManager.getTask(LTaskType.AUTO_STRIKE_BACK);
            if(strikeBackTask != null){
                var target = strikeBackTask.getAdditionalStrikeBackTarget();
                var replyEvent = new PlayerReplyStrikeBackTargetListEvent(target);
                var serialized = AIJsonHandler.serialize(replyEvent);
                Objects.requireNonNull(AIServiceManager.sendMessageAndReceiveReplyAsync(serialized)).whenComplete((reply, throwable) -> {
                    if(reply != null){
                        AIChatManager.handleAIReply(reply);
                    }
                });
                LynxMindClient.sendModMessage("AI正在检索反击对象列表...");
            }
        }
    }
}

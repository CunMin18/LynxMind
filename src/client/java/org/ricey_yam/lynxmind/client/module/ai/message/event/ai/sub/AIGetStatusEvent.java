package org.ricey_yam.lynxmind.client.module.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEvent;

@Getter
@Setter
public class AIGetStatusEvent extends AIEvent {
    @Override
    public void onReceive() {
        LynxMindClient.sendModMessage("AI正在检索玩家状态.....");
        AIChatManager.sendStatusJsonToAIAndReceiveReply().whenComplete((aiReply, throwable) -> AIChatManager.handleAIReply(aiReply));
    }
}

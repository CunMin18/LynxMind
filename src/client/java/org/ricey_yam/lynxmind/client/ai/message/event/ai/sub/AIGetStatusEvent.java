package org.ricey_yam.lynxmind.client.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.ai.message.event.ai.AIEvent;

@Getter
@Setter
public class AIGetStatusEvent extends AIEvent {
    @Override
    public void onReceive() {
        LynxMindClient.sendModMessage("AI正在检索玩家状态.....");
        ChatManager.sendStatusJsonToAIAndReceiveReply().whenComplete((aiReply, throwable) -> ChatManager.handleAIReply(aiReply));
    }
}

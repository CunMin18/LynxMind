package org.ricey_yam.lynxmind.client.module.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindBrain;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEvent;

@Getter
@Setter
public class AIStartEvent extends AIEvent {
    @Override
    public void onReceive() {
        LynxMindClient.sendModMessage("AI服务开启成功！");
        LynxMindBrain.wake();
    }
}

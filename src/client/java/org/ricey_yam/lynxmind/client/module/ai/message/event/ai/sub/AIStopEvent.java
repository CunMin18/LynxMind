package org.ricey_yam.lynxmind.client.module.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEvent;

@Getter
@Setter
public class AIStopEvent extends AIEvent {
    private String reason;
    @Override
    public void onReceive() {
        AIServiceManager.stopTask("AI终止了全部任务：" + reason);
        LynxMindClient.sendModMessage("任务终止，原因：" + reason);
    }
}

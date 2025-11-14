package org.ricey_yam.lynxmind.client.ai.json.event.ai.child;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.json.event.ai.AIEvent;

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

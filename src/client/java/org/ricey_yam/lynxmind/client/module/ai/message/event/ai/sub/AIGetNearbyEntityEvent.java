package org.ricey_yam.lynxmind.client.module.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIJsonHandler;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEvent;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEventType;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.sub.PlayerScanEntityEvent;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class AIGetNearbyEntityEvent extends AIEvent {
    private int radius;
    private List<String> target_entity_id;
    private AIGetNearbyEntityEvent(int radius, List<String> target_entity_id) {
        setType(AIEventType.EVENT_AI_GET_NEARBY_ENTITY);
        this.radius = radius;
        this.target_entity_id = target_entity_id;
    }

    @Override
    public void onReceive() {
        LynxMindClient.sendModMessage("AI正在扫描周边实体.....");
        var scanEntityEvent = new PlayerScanEntityEvent(radius, target_entity_id);
        var serialized = AIJsonHandler.serialize(scanEntityEvent);
        Objects.requireNonNull(AIServiceManager.sendMessageAndReceiveReplyAsync(serialized)).whenComplete((reply, error) -> AIChatManager.handleAIReply(reply));
    }
}

package org.ricey_yam.lynxmind.client.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.ai.LynxJsonHandler;
import org.ricey_yam.lynxmind.client.ai.message.event.ai.AIEvent;
import org.ricey_yam.lynxmind.client.ai.message.event.ai.AIEventType;
import org.ricey_yam.lynxmind.client.ai.message.event.player.sub.PlayerScanEntityEvent;

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
        var scanEntityEvent = new PlayerScanEntityEvent(radius, target_entity_id);
        var serialized = LynxJsonHandler.serialize(scanEntityEvent);
        Objects.requireNonNull(AIServiceManager.sendAndReceiveReplyAsync(serialized)).whenComplete((reply, error) -> ChatManager.handleAIReply(reply));
    }
}

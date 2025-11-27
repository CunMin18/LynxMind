package org.ricey_yam.lynxmind.client.ai.message.event.ai.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.ai.LynxJsonHandler;
import org.ricey_yam.lynxmind.client.ai.message.event.ai.AIEvent;
import org.ricey_yam.lynxmind.client.ai.message.event.ai.AIEventType;
import org.ricey_yam.lynxmind.client.ai.message.event.player.sub.PlayerScanBlockEvent;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class AIGetNearbyBlockEvent extends AIEvent {
    private int radius;
    private List<String> target_block_id;
    private AIGetNearbyBlockEvent(int radius, List<String> target_block_id) {
        setType(AIEventType.EVENT_AI_GET_NEARBY_BLOCK);
        this.radius = radius;
        this.target_block_id = target_block_id;
    }

    @Override
    public void onReceive() {
        LynxMindClient.sendModMessage("AI正在扫描周边方块.....");
        var scanBlockEvent = new PlayerScanBlockEvent(radius, target_block_id);
        var serialized = LynxJsonHandler.serialize(scanBlockEvent);
        Objects.requireNonNull(AIServiceManager.sendAndReceiveReplyAsync(serialized)).whenComplete((reply, error) -> ChatManager.handleAIReply(reply));
    }
}

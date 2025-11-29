package org.ricey_yam.lynxmind.client.module.ai.message.event.player.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.PlayerEvent;
import org.ricey_yam.lynxmind.client.module.ai.message.event.player.PlayerEventType;

@Getter
@Setter
public class PlayerRemoveTaskEvent extends PlayerEvent {
    public PlayerRemoveTaskEvent() {
        setType(PlayerEventType.EVENT_PLAYER_REMOVE_TASK);
    }
}

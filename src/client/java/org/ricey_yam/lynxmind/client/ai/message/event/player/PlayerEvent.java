package org.ricey_yam.lynxmind.client.ai.message.event.player;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.message.event.Event;

@Getter
@Setter
public class PlayerEvent extends Event {
    protected PlayerEventType type = PlayerEventType.NONE;
}

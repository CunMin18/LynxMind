package org.ricey_yam.lynxmind.client.ai.json.event.player;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.json.event.Event;

@Getter
@Setter
public class PlayerEvent extends Event {
    protected PlayerEventType type = PlayerEventType.NONE;
}

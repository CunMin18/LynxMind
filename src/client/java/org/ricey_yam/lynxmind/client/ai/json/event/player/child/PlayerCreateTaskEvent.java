package org.ricey_yam.lynxmind.client.ai.json.event.player.child;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.json.event.player.PlayerEvent;
import org.ricey_yam.lynxmind.client.ai.json.event.player.PlayerEventType;

@Getter
@Setter
public class PlayerCreateTaskEvent extends PlayerEvent {
    private String task;
    public PlayerCreateTaskEvent(String task) {
        setType(PlayerEventType.EVENT_PLAYER_CREATE_TASK);
        this.task = task;
    }
}

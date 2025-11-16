package org.ricey_yam.lynxmind.client.ai.message.event.player.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.ai.message.event.player.PlayerEvent;
import org.ricey_yam.lynxmind.client.ai.message.event.player.PlayerEventType;

@Getter
@Setter
public class PlayerBaritoneTaskStop extends PlayerEvent {
    private Action chained_action;
    private String reason;
    public PlayerBaritoneTaskStop(Action chained_action, String reason) {
        setType(PlayerEventType.EVENT_PLAYER_BARITONE_TASK_STOP);
        this.chained_action = chained_action;
        this.reason = reason;
    }
}

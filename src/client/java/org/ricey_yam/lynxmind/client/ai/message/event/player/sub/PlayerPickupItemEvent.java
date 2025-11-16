package org.ricey_yam.lynxmind.client.ai.message.event.player.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.message.event.player.PlayerEvent;
import org.ricey_yam.lynxmind.client.ai.message.event.player.PlayerEventType;

@Getter
@Setter
public class PlayerPickupItemEvent extends PlayerEvent {
    private String name;
    private int count;
    public PlayerPickupItemEvent(String name,int count) {
        this.type = PlayerEventType.EVENT_PLAYER_PICKUP_ITEM;
        this.name = name;
        this.count = count;
    }
}

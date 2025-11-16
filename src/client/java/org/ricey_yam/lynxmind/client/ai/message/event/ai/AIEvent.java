package org.ricey_yam.lynxmind.client.ai.message.event.ai;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.message.event.Event;


@Getter
@Setter
public class AIEvent extends Event {
    protected AIEventType type = AIEventType.NONE;
    public void onReceive() {
    }
}

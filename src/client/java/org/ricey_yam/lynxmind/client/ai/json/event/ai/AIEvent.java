package org.ricey_yam.lynxmind.client.ai.json.event.ai;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.json.event.Event;


@Getter
@Setter
public class AIEvent extends Event {
    protected AIEventType type = AIEventType.NONE;
    public void onReceive() {
    }
}

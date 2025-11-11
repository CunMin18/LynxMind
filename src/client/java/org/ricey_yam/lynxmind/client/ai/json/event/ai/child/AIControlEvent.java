package org.ricey_yam.lynxmind.client.ai.json.event.ai.child;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.config.AIServiceConfig;
import org.ricey_yam.lynxmind.client.ai.json.action.Action;
import org.ricey_yam.lynxmind.client.ai.json.event.ai.AIEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AIControlEvent extends AIEvent {

    @Expose(deserialize = false)
    protected List<Action> actions = new ArrayList<>();

    protected String plans = "";

    @Override
    public void onReceive(){
        if(!plans.isEmpty()) LynxMindClient.sendModMessage("[" + AIServiceConfig.getInstance().getModel() + "]" + plans);
        for (Action action : actions) {
            if (action == null) continue;
            action.invoke();
        }
    }
}

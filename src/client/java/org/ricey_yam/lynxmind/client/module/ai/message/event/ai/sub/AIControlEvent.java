package org.ricey_yam.lynxmind.client.module.ai.message.event.ai.sub;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.module.ai.message.event.ai.AIEvent;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;

@Getter
@Setter
public class AIControlEvent extends AIEvent {

    @Expose(deserialize = false)
    protected Action action = null;

    protected String plans = "";

    @Override
    public void onReceive(){
        if(!plans.isEmpty()) LynxMindClient.sendModMessage("[" + AIServiceManager.getCurrentService().getName() + "]" + plans);
        if(action != null) action.invoke();
    }
}

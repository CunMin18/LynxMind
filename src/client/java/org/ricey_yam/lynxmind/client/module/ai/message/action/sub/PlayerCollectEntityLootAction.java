package org.ricey_yam.lynxmind.client.module.ai.message.action.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.module.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.module.pathing.BaritoneManager;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.temp.action.AEntityCollectionTask;

import java.util.List;

@Getter
@Setter
public class PlayerCollectEntityLootAction extends Action {
    private List<AEntityCollectionTask.EntityKillingQuota> killingQuotas;
    public PlayerCollectEntityLootAction(List<AEntityCollectionTask.EntityKillingQuota> killingQuotas) {
        this.killingQuotas = killingQuotas;
    }

    @Override
    public boolean invoke() {
        BaritoneManager.stopPathingRelatedTasks("由于Killaura自带寻路，需取消普通寻路Task");
        var newKFCTask = new AEntityCollectionTask(killingQuotas,this);
        LynxMindEndTickEventManager.registerTask(newKFCTask);
        return super.invoke();
    }
}

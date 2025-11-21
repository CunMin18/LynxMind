package org.ricey_yam.lynxmind.client.task.temp.baritone;

import baritone.api.IBaritone;
import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.task.IAbsoluteTask;
import org.ricey_yam.lynxmind.client.task.temp.TempTask;

@Getter
@Setter
public abstract class BTask extends TempTask<BTaskType> implements IAbsoluteTask {
    /// 关联的Action（Action创建BTask）
    protected final IBaritone baritone;
    protected Action linkedAction;
    protected int weight;
    public BTask(){
        baritone = BaritoneManager.getClientBaritone();
    }
}

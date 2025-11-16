package org.ricey_yam.lynxmind.client.task.baritone;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.task.Task;

@Getter
@Setter
public abstract class BTask extends Task<BTaskType> {
    /// 关联的Action（Action创建BTask）
    protected Action linkedAction;
}

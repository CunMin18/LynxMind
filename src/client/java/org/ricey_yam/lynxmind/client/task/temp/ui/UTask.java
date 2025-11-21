package org.ricey_yam.lynxmind.client.task.temp.ui;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.task.IAbsoluteTask;
import org.ricey_yam.lynxmind.client.task.ICoexistingTask;
import org.ricey_yam.lynxmind.client.task.Task;
import org.ricey_yam.lynxmind.client.task.temp.TempTask;

@Getter
@Setter
public abstract class UTask extends TempTask<UTaskType> implements ICoexistingTask {
    protected UTaskResult result = UTaskResult.NONE;
    public enum UTaskResult {
        NONE,
        SUCCESS,
        FAILED
    }
}

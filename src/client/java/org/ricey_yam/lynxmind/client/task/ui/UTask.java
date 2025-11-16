package org.ricey_yam.lynxmind.client.task.ui;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.task.Task;

@Getter
@Setter
public abstract class UTask extends Task<UTaskType> {
    protected UTaskResult result = UTaskResult.NONE;
    public enum UTaskResult {
        NONE,
        SUCCESS,
        FAILED
    }
}

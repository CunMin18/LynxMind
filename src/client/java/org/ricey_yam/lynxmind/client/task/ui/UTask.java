package org.ricey_yam.lynxmind.client.task.ui;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.task.Task;

@Getter
@Setter
public abstract class UTask extends Task<UTaskType> {
    protected UTaskResult result;
    public enum UTaskResult {
        SUCCESS,
        FAILED
    }
}

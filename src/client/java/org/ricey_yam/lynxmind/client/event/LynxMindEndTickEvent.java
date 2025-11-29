package org.ricey_yam.lynxmind.client.event;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.task.Task;

import java.util.*;

@Getter
@Setter
public class LynxMindEndTickEvent<T extends Task<U>,U> {
    protected List<T> taskList = new ArrayList<>();

    public void tick(){
        if(taskList == null || taskList.isEmpty()) {
            return;
        }
        for (int i = 0; i < taskList.size(); i++) {
            var task = taskList.get(i);
            if(task == null) continue;
            if (task.getCurrentTaskState() == Task.TaskState.IDLE) {
                task.tick();
            }
        }
    }

    public void register(T task){
        if(task == null) return;

        if(taskList == null) {
            taskList = new ArrayList<>();
        }
        var containTask = false;
        for (int i = taskList.size() - 1; i >= 0; i--) {
            var t = taskList.get(i);
            if(t == null) continue;
            if(t.getTaskType() == task.getTaskType()) {
                t.stop("原有的Task已被替换。");
                taskList.set(i, task);
                containTask = true;
            }
        }
        if(!containTask) taskList.add(task);
        task.start();
    }

    public void unregister(U taskType, String reason){
        if(taskList == null) {
            taskList = new ArrayList<>();
            return;
        }
        for (int i = taskList.size() - 1; i >= 0; i--) {
            var task = taskList.get(i);
            if (task != null && task.getTaskType() == taskType) {
                task.stop(reason);
                taskList.remove(i);
            }
        }
    }

    public void clean(String reason){
        for(var task : taskList){
            if(task != null) task.stop("Task被手动清理");
        }
        taskList.clear();
    }

    public T getTask(U taskType){
        for(var task : taskList){
            if(task == null) continue;
            if(task.getTaskType() == taskType) return task;
        }
        return null;
    }
}

package org.ricey_yam.lynxmind.client.event;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.ricey_yam.lynxmind.client.task.Task;
import org.ricey_yam.lynxmind.client.task.baritone.BTask;
import org.ricey_yam.lynxmind.client.task.baritone.BTaskType;
import org.ricey_yam.lynxmind.client.task.ui.UTask;
import org.ricey_yam.lynxmind.client.task.ui.UTaskType;

@Getter
@Setter
public class LynxMindEndTickEventManager {
    private static LynxEndTickEvent<BTask,BTaskType> B_END_TICK_EVENT = null;
    private static LynxEndTickEvent<UTask,UTaskType> U_END_TICK_EVENT = null;

    /// 初始化Baritone任务管理器
    public static void init(){
        MinecraftClient.getInstance().execute(() -> {
            B_END_TICK_EVENT = new LynxEndTickEvent<>();
            U_END_TICK_EVENT =  new LynxEndTickEvent<>();
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (B_END_TICK_EVENT != null) B_END_TICK_EVENT.tick();
                if(U_END_TICK_EVENT != null)  U_END_TICK_EVENT.tick();
            });
        });
    }

    /// 注册事件
    public static <U> void registerTask(Task<U> task){
        MinecraftClient.getInstance().execute(() -> {
            if(task instanceof BTask bTask){
                B_END_TICK_EVENT.register(bTask);
            }
            else if(task instanceof UTask uTask){
                U_END_TICK_EVENT.register(uTask);
            }
        });
    }

    /// 取消注册事件
    public static <U> void unregisterTask(U taskType, String reason){
        MinecraftClient.getInstance().execute(() -> {
            if(taskType instanceof BTaskType bTaskType){
                B_END_TICK_EVENT.unregister(bTaskType,reason);
            }
            else if(taskType instanceof UTaskType uTaskType){
                U_END_TICK_EVENT.unregister(uTaskType,reason);
            }
        });
    }

    /// 获取任务
    public static <U> Task<?> getTask(U taskType){
        if(taskType instanceof BTaskType bTaskType){
            return B_END_TICK_EVENT.getTask(bTaskType);
        }
        else if(taskType instanceof UTaskType uTaskType){
            return U_END_TICK_EVENT.getTask(uTaskType);
        }
        return null;
    }

    public static <U> boolean isTaskActive(U taskType){
        var task = getTask(taskType);
        if(task == null) return false;
        return task.getCurrentTaskState() == Task.TaskState.IDLE;
    }

    /// 清理任务
    public static void cleanAllTasks(String reason){
        if(B_END_TICK_EVENT != null) {
            B_END_TICK_EVENT.clean(reason);
        }
        else if(U_END_TICK_EVENT != null) {
            U_END_TICK_EVENT.clean(reason);
        }
    }
}

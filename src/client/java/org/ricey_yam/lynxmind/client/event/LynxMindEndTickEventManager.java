package org.ricey_yam.lynxmind.client.event;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.ricey_yam.lynxmind.client.task.Task;
import org.ricey_yam.lynxmind.client.task.non_temp.INonTempType;
import org.ricey_yam.lynxmind.client.task.non_temp.NonTempTask;
import org.ricey_yam.lynxmind.client.task.temp.ITempTaskType;
import org.ricey_yam.lynxmind.client.task.temp.TempTask;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;

@Getter
@Setter
public class LynxMindEndTickEventManager {
    private static LynxMindEndTickEvent<TempTask<ITempTaskType>,ITempTaskType> TEMP_TASK_END_TICK_EVENT = null;
    private static LynxMindEndTickEvent<NonTempTask<INonTempType>,INonTempType> NON_TEMP_TASK_END_TICK_EVENT = null;

    /// 初始化Baritone任务管理器
    public static void init(){
        MinecraftClient.getInstance().execute(() -> {
            TEMP_TASK_END_TICK_EVENT = new LynxMindEndTickEvent<>();
            NON_TEMP_TASK_END_TICK_EVENT = new LynxMindEndTickEvent<>();
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if(ClientUtils.getPlayer() == null || ClientUtils.getPlayer().getHealth() <= 0) return;
                TEMP_TASK_END_TICK_EVENT.tick();
                NON_TEMP_TASK_END_TICK_EVENT.tick();
            });
        });
    }

    /// 注册事件
    public static <T> void registerTask(Task<T> task){
        MinecraftClient.getInstance().execute(() -> {
            if(task instanceof TempTask<T> tempTask){
                TEMP_TASK_END_TICK_EVENT.register((TempTask<ITempTaskType>) tempTask);
            }
            else if(task instanceof NonTempTask<T> nonTempTask){
                NON_TEMP_TASK_END_TICK_EVENT.register((NonTempTask<INonTempType>) nonTempTask);
            }
        });
    }

    /// 取消注册事件
    public static <U> void unregisterTask(U taskType, String reason){
        MinecraftClient.getInstance().execute(() -> {
            if(taskType instanceof ITempTaskType iTempTaskType){
                TEMP_TASK_END_TICK_EVENT.unregister(iTempTaskType,reason);
            }
            else if(taskType instanceof INonTempType iNonTempTaskType){
                NON_TEMP_TASK_END_TICK_EVENT.unregister(iNonTempTaskType,reason);
            }
        });
    }

    /// 获取任务
    public static <U> Task<?> getTask(U taskType){
        if(taskType instanceof ITempTaskType tempTaskType){
            return TEMP_TASK_END_TICK_EVENT.getTask(tempTaskType);
        }
        else if(taskType instanceof INonTempType nonTempTaskType){
            return NON_TEMP_TASK_END_TICK_EVENT.getTask(nonTempTaskType);
        }
        return null;
    }

    public static <U> boolean isTaskActive(U taskType){
        var task = getTask(taskType);
        if(task == null) return false;
        return task.getCurrentTaskState() == Task.TaskState.IDLE || task.getCurrentTaskState() == Task.TaskState.PAUSED;
    }

    /// 清理任务
    public static void cleanTempTasks(String reason){
        if(TEMP_TASK_END_TICK_EVENT != null) {
            TEMP_TASK_END_TICK_EVENT.clean(reason);
        }
    }
    public static void cleanNonTempTasks(String reason){
        if(NON_TEMP_TASK_END_TICK_EVENT != null) {
            NON_TEMP_TASK_END_TICK_EVENT.clean(reason);
        }
    }
    public static void cleanAllTasks(String reason){
        cleanTempTasks(reason);
        cleanNonTempTasks(reason);
    }
}

package org.ricey_yam.lynxmind.client.task.non_temp.life.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.service.AIServiceManager;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTaskType;

@Getter
@Setter
public class LAutoHeartbeatTask extends LTask {
    public static LAutoHeartbeatTask getActiveTask(){
        return (LAutoHeartbeatTask) LTask.getActiveLTask(LTaskType.AUTO_HEARTBEAT, LAutoHeartbeatTask.class,600);
    }

    private int tickDelay;
    public LAutoHeartbeatTask(int tickDelay) {
        setTaskType(LTaskType.AUTO_HEARTBEAT);
        this.tickDelay = tickDelay;
    }

    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
        tickTimer = 0;
    }

    @Override
    public void tick(){
        tickTimer++;
        if(tickTimer >= tickDelay){
            tickTimer = 0;
            sendStatusToAI();
        }
    }

    @Override
    public void stop(String cancelReason) {
        tickTimer = 0;
        tickDelay = 99999;
        this.currentTaskState = TaskState.STOPPED;
        System.out.println("自动任务已停止：" + cancelReason);
    }

    @Override
    public void pause() {
        tickTimer = 0;
        this.currentTaskState = TaskState.PAUSED;
    }

    private static void sendStatusToAI(){
        if(AIServiceManager.isServiceActive() && AIServiceManager.isTaskActive()){
            LynxMindClient.sendModMessage("玩家状态更新成功!");
            AIChatManager.sendStatusJsonToAIAndReceiveReply().whenComplete((aiReply, throwable) -> AIChatManager.handleAIReply(aiReply));
        }
    }
}

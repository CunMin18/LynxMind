package org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTaskType;

@Getter
@Setter
public class LAutoHeartbeatTask extends LTask {
    private int tickDelay;
    public LAutoHeartbeatTask(int tickDelay) {
        this.tickDelay = tickDelay;
    }
    @Override
    public void start() {
        this.taskType = LTaskType.AUTO_HEARTBEAT;
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
        this.currentTaskState = TaskState.FINISHED;
        System.out.println("自动心跳任务已停止：" + cancelReason);
    }

    @Override
    public void pause() {
        tickTimer = 0;
        this.currentTaskState = TaskState.PAUSED;
    }

    private static void sendStatusToAI(){
        if(AIServiceManager.isServiceActive && AIServiceManager.isTaskActive()){
            LynxMindClient.sendModMessage("玩家状态心跳发送成功!");
            ChatManager.sendStatusJsonToAIAndReceiveReply().whenComplete((aiReply, throwable) -> ChatManager.handleAIReply(aiReply));
        }
    }
}

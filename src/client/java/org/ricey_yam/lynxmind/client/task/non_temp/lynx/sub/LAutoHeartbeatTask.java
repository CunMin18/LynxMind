package org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTaskType;

@Getter
@Setter
public class LAutoHeartbeatTask extends LTask {
    public static LAutoHeartbeatTask getActiveTask(){
        if(LynxMindEndTickEventManager.isTaskActive(LTaskType.AUTO_HEARTBEAT)){
            var task = LynxMindEndTickEventManager.getTask(LTaskType.AUTO_HEARTBEAT);
            if(!(task instanceof LAutoHeartbeatTask lTask)) return null;
            return lTask;
        }
        else{
            var registered = new LAutoHeartbeatTask(600);
            LynxMindEndTickEventManager.registerTask(registered);
            return registered;
        }
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
        if(AIServiceManager.isServiceActive && AIServiceManager.isTaskActive()){
            LynxMindClient.sendModMessage("玩家状态更新成功!");
            ChatManager.sendStatusJsonToAIAndReceiveReply().whenComplete((aiReply, throwable) -> ChatManager.handleAIReply(aiReply));
        }
    }
}

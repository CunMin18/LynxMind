package org.ricey_yam.lynxmind.client.task.temp.action;

import baritone.api.IBaritone;
import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.ai.LynxJsonHandler;
import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.ai.message.event.player.sub.PlayerATaskStop;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.task.temp.TempTask;
import static org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LFunctionHubTask.*;

import java.util.Objects;

@Getter
@Setter
public abstract class ATask extends TempTask<ATaskType> {
    /// 关联的Action（Action创建BTask）
    protected final IBaritone baritone;
    protected Action linkedAction;
    public ATask(){
        baritone = BaritoneManager.getClientBaritone();
    }

    /// 发送任务停止事件给AI
    protected void sendATaskStopMessage(String stopReason){
        if(stopReason != null && !stopReason.isEmpty() && AIServiceManager.isServiceActive && AIServiceManager.isTaskActive() && linkedAction != null){
            var bTaskStopEvent = new PlayerATaskStop(linkedAction,stopReason);
            var serialized = LynxJsonHandler.serialize(bTaskStopEvent);
            Objects.requireNonNull(AIServiceManager.sendAndReceiveReplyAsync(serialized)).whenComplete((reply, error) -> ChatManager.handleAIReply(reply));
        }
    }

    protected PathingSubTask getPathingSubTask(){
        return Objects.requireNonNull(getActiveTask()).getPathingSubTask();
    }

    protected MineSubTask getMineSubTask(){
        return Objects.requireNonNull(getActiveTask()).getMineSubTask();
    }

    protected AttackSubTask getAttackTask(){
        return Objects.requireNonNull(getActiveTask()).getAttackSubTask();
    }

    protected ClickSlotHostSubTask getClickSlotHostSubTask(){
        return Objects.requireNonNull(getActiveTask()).getClickSlotHostSubTask();
    }
}

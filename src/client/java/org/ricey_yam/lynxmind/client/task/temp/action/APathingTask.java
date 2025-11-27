package org.ricey_yam.lynxmind.client.task.temp.action;

import baritone.api.pathing.goals.GoalBlock;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.BlockPos;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.ai.message.action.Action;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ContainerHelper;

@Getter
@Setter
public class APathingTask extends ATask {

    /// 目标位置
    private int targetX;
    private int targetY;
    private int targetZ;

    /// 重试次数（无法到达某个点时重新规划路径）
    private int restartCount;
    public APathingTask(float x, float y, float z, Action linked_action) {
        super();
        this.taskType = ATaskType.PATHING;
        this.targetX = Math.round(x);
        this.targetY = Math.round(y);
        this.targetZ = Math.round(z);
        this.currentTaskState = TaskState.IDLE;
        this.linkedAction = linked_action;
    }

    @Override
    public void start() {
        var goal = new GoalBlock(targetX, targetY, targetZ);
        getPathingSubTask().enable(goal);
    }

    @Override
    public void tick() {
        if(baritone != null){
            if(getPlayer().getBlockPos().isWithinDistance(new BlockPos(targetX,targetY,targetZ),1)){
                stop("已到达目的地！");
            }
            else if(getPathingSubTask().getCustomGoalProcess().getGoal() == null){
                restartCount++;
                if(restartCount > 5){
                    stop("无法到达目的地。");
                }
                else {
                    System.out.println("无法找到路径，正在重试，次数：" + restartCount);
                    getPathingSubTask().enable(new GoalBlock(targetX, targetY, targetZ));
                }
            }
            else restartCount = 0;
        }
    }

    @Override
    public void stop(String stopReason) {
        /// 发送任务停止事件给AI
        sendATaskStopMessage(stopReason);

        ContainerHelper.closeContainer();

        currentTaskState = TaskState.STOPPED;

        getPathingSubTask().disable();

        LynxMindClient.sendModMessage(stopReason);

        System.out.println("寻路任务已停止：" + stopReason);
    }

    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        getPathingSubTask().disable();
        ContainerHelper.closeContainer();
    }

}

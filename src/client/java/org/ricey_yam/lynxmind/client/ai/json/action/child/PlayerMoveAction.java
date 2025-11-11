package org.ricey_yam.lynxmind.client.ai.json.action.child;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.baritone.BEndTickEventManager;
import org.ricey_yam.lynxmind.client.baritone.task.BPathingTask;
import org.ricey_yam.lynxmind.client.ai.json.action.Action;

@Getter
@Setter
public class PlayerMoveAction extends Action {
    private float x;
    private float y;
    private float z;
    public PlayerMoveAction(float x, float y, float z) {
        x = Math.round(x);
        y = Math.round(y);
        z = Math.round(z);
        this.x = x;
        this.y = y;
        this.z = z;
    }
    @Override
    public boolean invoke() {
        try {
            /// 先停下其他动作
            stopAllActions();
            /// 创建寻路任务
            var newPathingTask = new BPathingTask(x, y, z);
            BEndTickEventManager.registerTask(newPathingTask);
            LynxMindClient.sendModMessage("正在寻路到[" + x + "," + y + "," + z + "]");
            return super.invoke();
        }
        catch (Exception e) {
            LynxMindClient.sendModMessage("§c执行收集方块任务时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

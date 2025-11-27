package org.ricey_yam.lynxmind.client;

import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LAutoGearSwitcher;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LAutoHeartbeatTask;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LAutoStrikeBackTask;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LFunctionHubTask;

public class LynxMindBrain {
    /// AI开启服务时的初始化
    public static void wake(){
        /// 定时状态心跳
        LynxMindEndTickEventManager.registerTask(new LAutoHeartbeatTask(600));

        /// 自动杀戮光环
        LynxMindEndTickEventManager.registerTask(new LAutoStrikeBackTask(5,10));

        /// 寻路/杀戮光环等功能枢纽
        LynxMindEndTickEventManager.registerTask(new LFunctionHubTask());

        /// 自动切换工具/武器
        LynxMindEndTickEventManager.registerTask(new LAutoGearSwitcher());
    }
    /// AI关闭服务,需要取消所有定时任务
    public static void sleep(String reason){
        LynxMindEndTickEventManager.cleanAllTasks(reason);
    }
}

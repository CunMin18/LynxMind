package org.ricey_yam.lynxmind.client.module.ai.service;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindBrain;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;
import org.ricey_yam.lynxmind.client.module.pathing.BaritoneManager;
import org.ricey_yam.lynxmind.client.task.non_temp.life.sub.LAIServiceMessageContainerTask;

import java.util.concurrent.CompletableFuture;

public class AIServiceManager {
    /// 当前AI服务
    @Getter
    @Setter
    private static IAIService<?> currentService;

    /// 当前任务详情
    @Setter
    @Getter
    private static String currentTask = "";

    /// 开启AI服务
    public static CompletableFuture<Boolean> openServiceAsync(AIServiceType serviceType) {
        AIChatManager.setWrongReplyFormatTriedCount(0);
        try{
            if(currentService != null) {
                LynxMindClient.sendModMessage("AI服务为开启状态,无需再次开启!");
                return CompletableFuture.completedFuture(false);
            }
            else{
                currentService = createAIService(serviceType);
                if(currentService == null) {
                    LynxMindClient.sendModMessage("未知的AI服务类型!");
                    return CompletableFuture.completedFuture(false);
                }
                LynxMindClient.sendModMessage("正在连接到AI服务......");
                return currentService.openServiceAsync().whenComplete((success, error) -> {
                    if(success) {
                        System.out.println("开启AI服务成功!");
                    }
                    else{
                        System.out.println("开启AI服务失败!");
                        currentService = null;
                    }
                });
            }
        }
        catch(Exception e){
            System.out.println("开启AI服务时出现错误!" + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    /// 关闭AI服务
    public static CompletableFuture<Boolean> closeServiceAsync() {
        if(currentService != null) {
            LynxMindClient.sendModMessage("已关闭AI服务！");

            /// 清空派发给AI的任务
            stopTask("AI服务已关闭！");

            /// 停止生命周期任务
            LynxMindBrain.sleep("AI服务已关闭!");

            return currentService.closeServiceAsync().whenComplete((success, error) -> currentService = null);
        }
        else{
            LynxMindClient.sendModMessage("AI服务未开启,无需关闭!");
            return CompletableFuture.completedFuture(false);
        }
    }

    /// 发送消息并接收回复
    public static CompletableFuture<String> sendMessageAndReceiveReplyAsync(String message){
        return currentService.sendMessageAndReceiveReplyAsync(message).whenComplete((reply, error) -> System.out.println("\n[ASSISTANT] " + reply));
    }

    /// 把消息存放到待发送清单
    public static void storeMessage(String message){
        LAIServiceMessageContainerTask.getActiveTask().storeNewMessage(message);
    }

    /// 是否有任务
    public static boolean isTaskActive(){
        return !currentTask.isEmpty();
    }

    /// 停止当前任务
    public static void stopTask(String reason){
        currentTask = "";
        currentService.onTaskRemove();
        BaritoneManager.stopAllTasks(reason);
    }

    /// 判断AI服务是否为开启状态
    public static boolean isServiceActive() {
        return currentService != null;
    }

    /// 根据类型创建AI服务
    private static IAIService<?> createAIService(AIServiceType serviceType) {
        switch (serviceType) {
            case OPEN_AI -> {
                return new OpenAIService();
            }
            case VOLC_ENGINE -> {
                return new VolcEngineService();
            }
        }
        return null;
    }
}
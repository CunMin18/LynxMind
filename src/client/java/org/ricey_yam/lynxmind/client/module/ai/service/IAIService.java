package org.ricey_yam.lynxmind.client.module.ai.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IAIService<T> {
    /// 获取聊天记录
    List<T> getChatMessages();

    /// 开启服务
    CompletableFuture<Boolean> openServiceAsync();

    /// 关闭服务
    CompletableFuture<Boolean> closeServiceAsync();

    /// 发送并接收信息
    CompletableFuture<String> sendMessageAndReceiveReplyAsync(String message);

    /// 任务被重置时触发的逻辑
    void onTaskRemove();

    /// 当前的AI服务类型
    String getName();
}

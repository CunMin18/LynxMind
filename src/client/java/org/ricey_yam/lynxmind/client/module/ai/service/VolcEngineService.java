package org.ricey_yam.lynxmind.client.module.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import lombok.Getter;
import lombok.Setter;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.service.ArkService;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.config.service.VolcEngineServiceConfig;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;

@Getter
@Setter
public class VolcEngineService implements IAIService<ChatMessage> {
    private static final ExecutorService AI_THREAD_POOL = Executors.newFixedThreadPool(2);
    private static ArkService arkService = null;

    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final String apiUrl;
    private final String modelName;
    private final String apiKey;

    public VolcEngineService() {
        var volcEngineService = VolcEngineServiceConfig.getInstance();
        this.apiUrl = volcEngineService.getApi_url();
        this.modelName = volcEngineService.getModel();
        this.apiKey = volcEngineService.getApi_key();
    }

    @Override
    public CompletableFuture<Boolean> openServiceAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var dispatcher = new Dispatcher();
                dispatcher.setMaxRequestsPerHost(1);
                arkService = ArkService.builder()
                        .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                        .dispatcher(dispatcher)
                        .apiKey(apiKey)
                        .build();
                if (validateService()) {
                    return AIChatManager.sendStartMessageToAIAndReceiveReply().thenApply((reply) -> {
                        if (!reply.contains("EVENT_AI_START")) {
                            System.out.println("AI产生幻觉了: " + reply);
                            return false;
                        } else {
                            AIChatManager.handleAIReply(reply);
                            return true;
                        }
                    }).join();
                } else {
                    return false;
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        }, AI_THREAD_POOL).exceptionally(ex -> {
            System.err.println("开启 VolcEngineService 失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> closeServiceAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if(arkService != null){
                try {
                    arkService.shutdownExecutor();
                }
                catch (Exception e) {
                    System.err.println("关闭 VolcEngineService 时出错: " + e.getMessage());
                }
                arkService = null;
                chatMessages.clear();
                return true;
            }
            else return false;
        },AI_THREAD_POOL);
    }

    @Override
    public CompletableFuture<String> sendMessageAndReceiveReplyAsync(String message) {
        return CompletableFuture.supplyAsync(() -> {
            var chatMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
            chatMessages.add(chatMessage);

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(modelName)
                    .messages(chatMessages)
                    .thinking(new ChatCompletionRequest.ChatCompletionRequestThinking("disabled"))
                    .build();
            try {
                var response = arkService.createChatCompletion(chatCompletionRequest);
                String aiMessage = response.getChoices().get(0).getMessage().getContent().toString();
                var aiChatMessage =  ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(aiMessage).build();
                chatMessages.add(aiChatMessage);
                return aiMessage;
            }
            catch (Exception e) {
                System.out.println("发送消息时出现错误: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        },AI_THREAD_POOL);
    }

    @Override
    public void onTaskRemove() {

    }

    @Override
    public String getName() {
        return "火山方舟 VolcEngineService | " + modelName;
    }

    /// 验证 API Key 和模型是否有效
    private boolean validateService() {
        System.out.println("awa");
        if (arkService == null) {
            LynxMindClient.sendModMessage("验证失败：VolcEngineService 实例为 null。");
            return false;
        }

        if (modelName == null || modelName.trim().isEmpty()) {
            LynxMindClient.sendModMessage("验证失败：模型名称为空。");
            return false;
        }

        LynxMindClient.sendModMessage("正在验证 API Key 和模型 '" + modelName + "'...");

        ChatMessage testMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content("ping")
                .build();

        List<ChatMessage> testMessages = new ArrayList<>();
        testMessages.add(testMessage);

        ChatCompletionRequest testRequest = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(testMessages)
                .maxTokens(1)
                .build();

        try {
            arkService.createChatCompletion(testRequest);
            LynxMindClient.sendModMessage("API Key 和模型验证成功！");
            return true;
        }
        catch (Exception e) {
            LynxMindClient.sendModMessage("验证失败！原因: " + e.getMessage());
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("invalid api key") || errorMsg.contains("authentication failed")) {
                LynxMindClient.sendModMessage("=> 可能的原因：API Key 无效或已过期。");
            }
            else if (errorMsg.contains("model not found") || errorMsg.contains("invalid model")) {
                LynxMindClient.sendModMessage("=> 可能的原因：模型名称 '" + modelName + "' 不存在或拼写错误。");
            }
            else if (errorMsg.contains("quota") || errorMsg.contains("rate limit")) {
                LynxMindClient.sendModMessage("=> 可能的原因：API 调用配额已用完或触发了速率限制。");
            }
            else {
                LynxMindClient.sendModMessage("=> 请检查你的网络连接或火山引擎 Ark 服务状态。");
            }

            return false;
        }
    }
}

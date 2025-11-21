package org.ricey_yam.lynxmind.client.ai;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindBrain;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.baritone.BaritoneManager;
import org.ricey_yam.lynxmind.client.config.AIServiceConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AIServiceManager {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(30)).build();
    private static final int keepingMessagesCount = 15;

    /// User 和 Assistant 的聊天记录
    @Getter
    private final static List<ChatMessage> chatMessages = new ArrayList<>();

    /// 当前任务详情
    @Setter
    @Getter
    private static String currentTask = "";

    @Setter
    @Getter
    private static String currentConversationId = "";


    /// AI服务是否处于开启状态
    public static boolean isServiceActive;

    /// 开启AI服务
    public static void openServiceAsync() {
        /// 关闭先前的服务
        closeServiceAsync();

        ChatManager.sendStartMessageToAIAndReceiveReply().whenComplete((reply, throwable) -> {
            if(throwable != null){
                System.out.println("连接AI服务时异常" + throwable.getMessage());
            }
            else{
                ChatManager.handleAIReply(reply);
            }
        });
    }

    /// 关闭AI服务
    public static void closeServiceAsync() {
        if(isServiceActive) {
            LynxMindClient.sendModMessage("已关闭AI服务！");
        }
        currentConversationId = "";
        isServiceActive = false;
        chatMessages.clear();
        stopTask("AI服务已关闭！");
        LynxMindBrain.sleep("AI服务已关闭!");
    }

    /// 发送消息并接收回复
    public static CompletableFuture<String> sendAndReceiveReplyAsync(String message){
        var aiServiceConfig = AIServiceConfig.getInstance();

        /// 构造用户消息
        var chatMessage = new ChatMessage("user",message);
        chatMessages.add(chatMessage);

        /// 检查消息列表是否过长，如果过长则删除第二条（第一条是规则介绍）
        var requestChatMessagesList = getResponseMessageList();

        /// 构造请求体
        var requestData = new RequestData(aiServiceConfig.getModel(),false,requestChatMessagesList,currentConversationId);
        /// 创建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceConfig.getApi_url()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + aiServiceConfig.getToken())
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(requestData)))
                .build();

        /// 发送请求
        try {
            return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .orTimeout(30, TimeUnit.SECONDS)
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        var textResponse = GSON.fromJson(body, TextResponse.class);
                        if(textResponse != null && !textResponse.getChoices().isEmpty()){
                            var reply = textResponse.getChoices().get(0).getMessage().getContent();
                            /// 构造AI返回消息 添加到聊天记录
                            var aiMsg = new ChatMessage("assistant",reply);
                            chatMessages.add(aiMsg);
                            currentConversationId = textResponse.getId();
                            System.out.println("\n[ASSISTANT] " + reply);
                            return reply.replaceAll("```json","").replaceAll("```","");
                        }
                        return null;
                    })
                    .exceptionally(ex -> {
                        System.err.println("异步任务异常：" + ex.getMessage());
                        LynxMindClient.sendModMessage("连接AI服务失败，请检查网络连接！" + ex.getMessage());
                        ex.printStackTrace();
                        return null;
                    });
        }
        catch (Exception e) {
            System.out.println("接收AI返回的消息时出现错误： " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /// 获取请求数据发送的聊天列表
    private static List<ChatMessage> getResponseMessageList(){
        var aiServiceConfig = AIServiceConfig.getInstance();
        List<ChatMessage> result;

        /// 判断是否开启伪连续对话（容易受发送内容上限影响）
        /// 伪连续对话：将所有历史消息叠加到第一次发送的消息，然后重复创建新对话从而实现连续对话

        /// 伪连续对话，需要把历史全部消息进行删减后返回给AI
        if(aiServiceConfig.isPseudo_continuous_dialogue()){
            result = new ArrayList<>(chatMessages);
            while(result.size() > keepingMessagesCount){
                result.remove(3);
                result.get(3).setContent(
                        "---\n" +
                                "*注意：为节省上下文长度，聊天历史已自动删减。\n" +
                                "保留的内容：\n" +
                                "- (ROLE:USER) 索引 0：游戏规则\n" +
                                "- (ROLE:ASSISTANT) 索引 1：AI 启动事件 (EVENT_AI_START)\n" +
                                "- (ROLE:USER) 索引 2：当前任务内容 (EVENT_PLAYER_CREATE_TASK)\n" +
                                "已删减：索引 3 及之后的历史 AI 决策和玩家状态记录（仅保留最近" + keepingMessagesCount + "条，这不影响当前任务执行）。\n" +
                                "请基于接下来保留的最近信息继续处理当前任务。\n" +
                                "---\n" + result.get(3).getContent());
            }
        }
        /// 传统连续对话，只需把任务内容和当前消息加入任务列表
        else{
            result = new ArrayList<>();

            /// 介绍过规则且有任务
            if(chatMessages.size() >= 3 && isTaskActive()){
                /// 添加 索引2 到开头：任务详情
                result.add(chatMessages.get(2));
            }
            /// 索引chatMessages.size() - 1：Request即将发送的信息，无论如何都要添加到chatMessages
            var lastMessageIndex = chatMessages.size() - 1;
            result.add(chatMessages.get(lastMessageIndex));
        }
        return result;
    }

    /// 是否有任务
    public static boolean isTaskActive(){
        return !currentTask.isEmpty();
    }

    /// 停止当前任务
    public static void stopTask(String reason){
        currentTask = "";
        /// 删除布置任务后的聊天记录，只保留游戏规则
        if(chatMessages.size() > 2) chatMessages.subList(2, chatMessages.size()).clear();
        BaritoneManager.stopAllTasks(reason);
    }
}
@Getter
@Setter
class ChatMessage{
    private String role;
    private String content;
    public  ChatMessage(String role, String content){
        this.role = role;
        this.content = content;
    }
}

@Getter
@Setter
class RequestData {
    private String model;
    private boolean stream;
    private List<ChatMessage> messages;
    private String conversation_id;
    public RequestData(String model, boolean stream, List<ChatMessage> messages, String conversation_id) {
        this.model = model;
        this.stream = stream;
        this.messages = messages;
        this.conversation_id = conversation_id;
    }
}
@Getter
class Choice {
    private ChatMessage message;
}
@Getter
class TextResponse {
    private List<Choice> choices;
    private String id;
}

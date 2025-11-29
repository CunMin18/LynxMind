package org.ricey_yam.lynxmind.client.module.ai.service;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.LynxMindClient;
import org.ricey_yam.lynxmind.client.config.service.OpenAIServiceConfig;
import org.ricey_yam.lynxmind.client.module.ai.message.AIChatManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
@Setter
@Getter
public class OpenAIService implements IAIService<ChatMessage> {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(30)).build();
    private static final int keepingMessagesCount = 15;

    private String currentConversationID = "";
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private String apiUrl;
    private String modelName;
    private String token;
    private boolean isPseudoContinuousDialogue;

    public OpenAIService(){
        var openAIServiceConfig = OpenAIServiceConfig.getInstance();
        this.apiUrl = openAIServiceConfig.getApi_url();
        this.modelName = openAIServiceConfig.getModel();
        this.token = openAIServiceConfig.getApi_token();
        this.isPseudoContinuousDialogue = openAIServiceConfig.isPseudo_continuous_dialogue();
    }

    @Override
    public CompletableFuture<Boolean> openServiceAsync() {
        return AIChatManager.sendStartMessageToAIAndReceiveReply().thenApply((reply) -> {
            if(!reply.contains("EVENT_AI_START")){
                System.out.println("AI产生幻觉了: " + reply);
                return false;
            }
            else{
                AIChatManager.handleAIReply(reply);
                return true;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> closeServiceAsync() {
        currentConversationID = "";
        chatMessages.clear();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<String> sendMessageAndReceiveReplyAsync(String message){
        /// 构造用户消息
        var chatMessage = new ChatMessage("user",message);
        chatMessages.add(chatMessage);

        /// 检查消息列表是否过长，如果过长则删除第二条（第一条是规则介绍）
        var requestChatMessagesList = getResponseMessageList();

        /// 构造请求体
        var requestData = new RequestData(modelName,false,requestChatMessagesList, currentConversationID);

        /// 创建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
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
                            currentConversationID = textResponse.getId();
                            return reply;
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

    @Override
    public void onTaskRemove() {
        /// 删除布置任务后的聊天记录，只保留游戏规则
        if(chatMessages.size() > 2) chatMessages.subList(2, chatMessages.size()).clear();
    }

    @Override
    public String getName() {
        return "Custom | " + modelName;
    }

    /// 获取请求数据需要发送的聊天列表
    private List<ChatMessage> getResponseMessageList(){
        List<ChatMessage> result;

        /// 判断是否开启伪连续对话（容易受发送内容上限影响）
        /// 伪连续对话：将所有历史消息叠加到第一次发送的消息，然后重复创建新对话从而实现连续对话
        /// 豆包好像只能伪连续对话

        /// 伪连续对话，需要把历史全部消息进行删减后返回给AI
        if(isPseudoContinuousDialogue){
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

            /// 介绍过规则
            if(chatMessages.size() >= 3){
                /// 添加 索引2 到开头：任务详情
                result.add(chatMessages.get(2));
            }
            /// 索引chatMessages.size() - 1：Request即将发送的信息，无论如何都要添加到chatMessages
            var lastMessageIndex = chatMessages.size() - 1;
            result.add(chatMessages.get(lastMessageIndex));
        }
        return result;
    }
}
@Getter
@Setter
class ChatMessage {
    private String role;
    private String content;
    public ChatMessage(String role, String content){
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


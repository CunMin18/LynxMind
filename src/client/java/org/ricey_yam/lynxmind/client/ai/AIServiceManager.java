package org.ricey_yam.lynxmind.client.ai;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
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
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(15)).build();

    @Getter
    private final static List<ChatMessage> chatMessages = new ArrayList<>();

    @Setter
    @Getter
    private static String currentTask = "";

    /// 发送消息并接收回复
    public static CompletableFuture<String> sendAndReceiveReplyAsync(String message){
        var aiServiceConfig = AIServiceConfig.getInstance();
        /// 构造用户消息
        var chatMessage = new ChatMessage("user",message);
        chatMessages.add(chatMessage);
        //System.out.println("[用户]" + message.replaceAll(UserMessageStorage.getSchemasMD(), "[给AI的MD帮助信息]"));

        /// 构造请求体
        var requestBody = new RequestBody(aiServiceConfig.getModel(),false,chatMessages);
        /// 创建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceConfig.getApi_url()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + aiServiceConfig.getToken())
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(requestBody)))
                .build();

        /// 发送请求
        try {
            return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .orTimeout(15, TimeUnit.SECONDS)
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        var textResponse = GSON.fromJson(body, TextResponse.class);
                        System.out.println("Body:" + body);
                        if(textResponse != null && !textResponse.getChoices().isEmpty()){
                            var reply = textResponse.getChoices().get(0).getMessage().getContent();
                            /// 构造AI返回消息 添加到聊天记录
                            var aiMsg = new ChatMessage("assistant",reply);
                            chatMessages.add(aiMsg);
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

    /// 是否有任务
    public static boolean isTaskActive(){
        return !currentTask.isEmpty();
    }

    /// 停止当前任务
    public static void stopTask(){
        currentTask = "";
        chatMessages.clear();
        BaritoneManager.stopAllTasks();
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
class RequestBody{
    private String model;
    private boolean stream;
    private List<ChatMessage> messages;

    public RequestBody(String model, boolean stream, List<ChatMessage> messages){
        this.model = model;
        this.stream = stream;
        this.messages = messages;
    }
}
@Getter
class Choice {
    private ChatMessage message;
}
@Getter
class TextResponse {
    private List<Choice> choices;
}

package org.ricey_yam.lynxmind.client.task.non_temp.life.sub;

import lombok.Getter;
import lombok.Setter;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTaskType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Getter
@Setter
public class LAIServiceMessageContainerTask extends LTask {
    public static LAIServiceMessageContainerTask getActiveTask(){
        return (LAIServiceMessageContainerTask) LTask.getActiveLTask(LTaskType.AI_SERVICE_MESSAGE_CONTAINER, LAIServiceMessageContainerTask.class);
    }

    private int sendTickDelay;
    private List<StoredMessage> storedMessages = new ArrayList<>();
    public LAIServiceMessageContainerTask(int sendTickDelay) {
        this.sendTickDelay = sendTickDelay;
    }

    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
    }

    @Override
    public void tick() {
        tickTimer++;
        if(tickTimer >= sendTickDelay){
            tickTimer = 0;
            sendStoredMessages();
        }
    }

    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.STOPPED;
        storedMessages.clear();
        sendTickDelay = 9999;
    }

    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
    }

    private void sendStoredMessages(){
        //TODO SEND
    }

    public void storeNewMessage(String message){
        if(storedMessages == null) storedMessages = new ArrayList<>();
        storedMessages.add(new StoredMessage(System.currentTimeMillis(),message));
    }
}
@Getter
@Setter
class StoredMessage{
    /// 消息的时间戳
    private long timestamp;

    /// 消息
    private String message;

    public StoredMessage(long timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    /// 拼接时间和消息
    public String getFormedMessageStr(){
        return "\n|| 时间 " + getReadableTimeStr() + " ||\n" + message;
    }

    /// 把时间戳转化为可视的时间
    private String getReadableTimeStr(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000);
        var accurateMonth = calendar.get(Calendar.MONTH) + 1;
        var min = calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE);
        var sec = calendar.get(Calendar.SECOND) < 10 ? "0" + calendar.get(Calendar.SECOND) : calendar.get(Calendar.SECOND);
        return calendar.get(Calendar.YEAR) + "年" + accurateMonth + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日 " + calendar.get(Calendar.HOUR_OF_DAY) + ":" +  min + ":" + sec;
    }
}

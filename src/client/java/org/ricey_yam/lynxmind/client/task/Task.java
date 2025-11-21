package org.ricey_yam.lynxmind.client.task;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;

@Getter
@Setter
public abstract class Task<T> {
    protected T taskType;
    protected int tickTimer;
    protected TaskState currentTaskState;
    public abstract void start();
    public abstract void tick();
    public abstract void stop(String cancelReason);
    public abstract void pause();
    public PlayerEntity getPlayer(){
        return ClientUtils.getPlayer();
    }
    public enum TaskState{
        IDLE,
        FINISHED,
        PAUSED,
        FAILED
    }
}

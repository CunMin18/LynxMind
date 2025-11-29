package org.ricey_yam.lynxmind.client.task.non_temp.life.sub;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.life.LTaskType;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class LAutoStrikeBackTask extends LTask {
    public static LAutoStrikeBackTask getActiveTask(){
        return (LAutoStrikeBackTask) LTask.getActiveLTask(LTaskType.AUTO_STRIKE_BACK, LAutoStrikeBackTask.class,5,10);
    }

    private int strikeBoxSize;
    private int checkTickDelay;
    private List<UUID> toAddStrikeBackTargetUUIDs = new ArrayList<>();
    private List<UUID> additionalStrikeBackTarget = new ArrayList<>();
    public LAutoStrikeBackTask(int strikeBoxSize,int checkTickDelay) {
        setTaskType(LTaskType.AUTO_STRIKE_BACK);
        additionalStrikeBackTarget.clear();
        this.strikeBoxSize = strikeBoxSize;
        this.checkTickDelay = checkTickDelay;
    }

    @Override
    public void start() {
        this.currentTaskState = TaskState.IDLE;
        tickTimer = 0;
    }

    @Override
    public void tick(){
        tickTimer++;
        if(tickTimer >= checkTickDelay){
            tickTimer = 0;
            if(strikeBoxSize <= 0) return;
            var entities = EntityUtils.scanAllEntity(getPlayer(),LivingEntity.class,strikeBoxSize, e -> e.distanceTo(getPlayer()) <= 3 && (EntityUtils.isHostileToPlayer(e) || additionalStrikeBackTarget.contains(e.getUuid())));
            if(entities != null && !entities.isEmpty()){
                enableKillaura(entities);
            }
        }
    }

    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.STOPPED;
        checkTickDelay = 9999;
        tickTimer = 0;
        strikeBoxSize = 0;
        additionalStrikeBackTarget.clear();
        disableKillaura();
        System.out.println("自动还击已停止!" + cancelReason);
    }

    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        tickTimer = 0;
    }

    private void enableKillaura(List<LivingEntity> entities){
        var attackingSubTask = getAttackingSubTask();
        if(toAddStrikeBackTargetUUIDs == null) toAddStrikeBackTargetUUIDs = new ArrayList<>();
        toAddStrikeBackTargetUUIDs.addAll(attackingSubTask.getAttackTargetList());
        for(var entity : entities){
            toAddStrikeBackTargetUUIDs.add(entity.getUuid());
        }
        toAddStrikeBackTargetUUIDs.addAll(additionalStrikeBackTarget);
        attackingSubTask.enable(toAddStrikeBackTargetUUIDs.stream().distinct().toList());
    }

    private void disableKillaura(){
        getAttackingSubTask().disable();
    }

    /// 获取攻击子任务
    private LFunctionHubTask.AttackSubTask getAttackingSubTask(){
        return Objects.requireNonNull(LFunctionHubTask.getActiveTask()).getAttackSubTask();
    }
}

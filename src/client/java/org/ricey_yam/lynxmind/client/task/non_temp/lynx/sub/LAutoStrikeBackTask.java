package org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import org.ricey_yam.lynxmind.client.event.LynxMindEndTickEventManager;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTask;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.LTaskType;
import org.ricey_yam.lynxmind.client.task.temp.baritone.BKillauraTask;
import org.ricey_yam.lynxmind.client.task.temp.baritone.BTaskType;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class LAutoStrikeBackTask extends LTask {
    private float attackRange;
    private int strikeBoxSize;
    private int checkTickDelay;
    private List<UUID> additionalStrikeBaCKTarget = new ArrayList<>();
    public LAutoStrikeBackTask(int strikeBoxSize,int checkTickDelay) {
        setTaskType(LTaskType.AUTO_STRIKE_BACK);
        attackRange = 3;
        additionalStrikeBaCKTarget.clear();
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
            var entities = EntityUtils.scanAllEntity(getPlayer(),LivingEntity.class,strikeBoxSize, e -> EntityUtils.isHostileToPlayer(e) || additionalStrikeBaCKTarget.contains(e.getUuid()));
            if(entities != null && !entities.isEmpty()){
                enableKillaura(entities);
            }
            else {
                disableKillaura();
            }
        }
    }

    @Override
    public void stop(String cancelReason) {
        this.currentTaskState = TaskState.FINISHED;
        checkTickDelay = 9999;
        tickTimer = 0;
        strikeBoxSize = 0;
        additionalStrikeBaCKTarget.clear();
        LynxMindEndTickEventManager.unregisterTask(BTaskType.KILLAURA,"自动还击已停止!");
        System.out.println("自动还击已停止!" + cancelReason);
    }

    @Override
    public void pause() {
        this.currentTaskState = TaskState.PAUSED;
        tickTimer = 0;
    }

    private void enableKillaura(List<LivingEntity> entities){
        BKillauraTask killauraBTask;
        if(LynxMindEndTickEventManager.isTaskActive(BTaskType.KILLAURA)){
            killauraBTask = (BKillauraTask) LynxMindEndTickEventManager.getTask(BTaskType.KILLAURA);
            if (killauraBTask != null) {
                List<UUID> attackTargetUUIDList = new ArrayList<>();
                for(var entity : entities){
                    attackTargetUUIDList.add(entity.getUuid());
                }
                attackTargetUUIDList.addAll(additionalStrikeBaCKTarget);

                killauraBTask.setAttackingTarget(attackTargetUUIDList);
                killauraBTask.setWeight(2);
            }
        }
        else{
            killauraBTask = new BKillauraTask(this.attackRange);
            LynxMindEndTickEventManager.registerTask(killauraBTask);
        }
    }

    private void disableKillaura(){
        if(LynxMindEndTickEventManager.isTaskActive(BTaskType.KILLAURA)){
            var killauraBTask = LynxMindEndTickEventManager.getTask(BTaskType.KILLAURA);
            if(!(killauraBTask instanceof BKillauraTask kbt) || kbt.getCurrentTaskState() == TaskState.PAUSED) return;
            kbt.setAttackingTarget(new ArrayList<>());
            kbt.setWeight(0);
        }
    }
}

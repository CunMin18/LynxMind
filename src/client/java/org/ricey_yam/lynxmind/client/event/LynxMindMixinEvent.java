package org.ricey_yam.lynxmind.client.event;

import net.minecraft.entity.LivingEntity;
import org.ricey_yam.lynxmind.client.ai.AIServiceManager;
import org.ricey_yam.lynxmind.client.ai.ChatManager;
import org.ricey_yam.lynxmind.client.ai.LynxJsonHandler;
import org.ricey_yam.lynxmind.client.ai.message.event.player.sub.PlayerPickupItemEvent;
import org.ricey_yam.lynxmind.client.task.non_temp.lynx.sub.LFunctionHubTask;
import org.ricey_yam.lynxmind.client.task.temp.action.*;
import org.ricey_yam.lynxmind.client.task.temp.action.AEntityCollectionTask;
import org.ricey_yam.lynxmind.client.task.temp.action.AMurderTask;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;

import java.util.Objects;

public class LynxMindMixinEvent {
    public static void onPlayerPickupItem(String itemID,int count){
        /// 发送捡起物品状态
        if(AIServiceManager.isTaskActive() && AIServiceManager.isServiceActive){
            var playerPickupItemEvent = new PlayerPickupItemEvent(itemID,count);
            var serialized = LynxJsonHandler.serialize(playerPickupItemEvent);
            Objects.requireNonNull(AIServiceManager.sendAndReceiveReplyAsync(serialized)).whenComplete((reply, throwable) -> ChatManager.handleAIReply(reply));
        }

        /// 更新ATask收集状态
        if(LynxMindEndTickEventManager.isTaskActive(ATaskType.BLOCK_COLLECTION)){
            var bCT = (ABlockCollectionTask) LynxMindEndTickEventManager.getTask(ATaskType.BLOCK_COLLECTION);
            if(bCT != null) bCT.onBlockDropCollected(itemID,count);
        }
        if(LynxMindEndTickEventManager.isTaskActive(ATaskType.ENTITY_COLLECTION)){
            var eCT = (AEntityCollectionTask) LynxMindEndTickEventManager.getTask(ATaskType.ENTITY_COLLECTION);
            if(eCT != null && eCT.getKillingQuotas() != null && !eCT.getKillingQuotas().isEmpty()) {
                for (int i = 0; i < eCT.getKillingQuotas().size(); i++) {
                    var quota = eCT.getKillingQuotas().get(i);
                    var items = quota.getNeeded_item();
                    for (int j = 0; j < items.size(); j++) {
                        var item = items.get(j);
                        if(item.getItem_name().equals(itemID)){
                            item.setCount(item.getCount() - count);
                            if(item.getCount() <= 0) {
                                items.remove(j);
                                if(items.isEmpty()){
                                    eCT.getKillingQuotas().remove(i);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void onPlayerKillEntity(LivingEntity killedEntity){
        var killedEntityID = EntityUtils.getEntityID(killedEntity);
        var killedEntityUUID = killedEntity.getUuid();
        var attackSubTask = Objects.requireNonNull(LFunctionHubTask.getActiveTask()).getAttackSubTask();

        /// 更新功能枢纽状态
        if(attackSubTask.getAttackTargetList() != null){
            attackSubTask.getAttackTargetList().remove(killedEntity.getUuid());
        }

        /// 更新ATask状态
        if(LynxMindEndTickEventManager.isTaskActive(ATaskType.ENTITY_COLLECTION)){
            var eCT = (AEntityCollectionTask) LynxMindEndTickEventManager.getTask(ATaskType.ENTITY_COLLECTION);
            if(eCT != null && eCT.getKillingQuotas() != null && !eCT.getKillingQuotas().isEmpty()) {
                eCT.transitionToFindingLoot();
            }
        }
        if(LynxMindEndTickEventManager.isTaskActive(ATaskType.MURDER)){
            var mCT = (AMurderTask) LynxMindEndTickEventManager.getTask(ATaskType.MURDER);
            if(mCT != null && mCT.getMurderTargetUUIDs() != null && !mCT.getMurderTargetUUIDs().isEmpty()) {
                mCT.getMurderTargetUUIDs().remove(killedEntityUUID);
            }
        }

    }
}

package org.ricey_yam.lynxmind.client.task.ui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.screen.slot.SlotActionType;
import org.ricey_yam.lynxmind.client.utils.game.InteractionUtils;

@Getter
@Setter
public class UPickupAllItemsFromSlotTask extends UTask{
    protected int slotId;
    public UPickupAllItemsFromSlotTask(int slotId) {
        this.taskType = UTaskType.PICKUP_ALL_ITEMS_FROM_SLOT;
        this.currentTaskState = TaskState.IDLE;
        this.slotId = slotId;
    }
    @Override
    public void start() {
        var success = InteractionUtils.clickContainerSlot(slotId,0, SlotActionType.PICKUP_ALL);
        if(success) {
            setResult(UTaskResult.SUCCESS);
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void stop(String cancelReason) {

    }
}

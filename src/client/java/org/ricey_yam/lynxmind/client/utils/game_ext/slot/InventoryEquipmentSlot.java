package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import net.minecraft.screen.slot.Slot;

/// LSlot ID Range: 0-4 (0-3:护甲 4:副手)
///
/// Slot ID Range: 5-9(仅玩家背包)
public class InventoryEquipmentSlot extends LSlot {
    public InventoryEquipmentSlot(){
        super();
        this.slotType = LSlotType.INVENTORY_EQUIPMENT;
    }
    public InventoryEquipmentSlot(int id, boolean inComplexContainer) {
        super(id,inComplexContainer);
        this.slotType = LSlotType.INVENTORY_EQUIPMENT;
    }
    @Override
    public Slot toSlot() {
        return SlotHelper.getSlot(id + 5);
    }

    @Override
    public LSlot toLSlot(Slot slot,boolean inComplexContainer) {
        this.inComplexContainer = inComplexContainer;
        this.id = slot.id - 5;
        return this;
    }
}

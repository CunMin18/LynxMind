package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import net.minecraft.screen.slot.Slot;

/// LSlot ID Range: 0-26
///
/// Slot ID Range: 10-36(任意容器)
public class InventoryInnerSlot extends LSlot {
    public InventoryInnerSlot(){
        super();
        this.slotType = LSlotType.INVENTORY_INNER;
    }
    public InventoryInnerSlot(int id, boolean inComplexContainer) {
        super(id,inComplexContainer);
        this.slotType = LSlotType.INVENTORY_INNER;
    }
    @Override
    public Slot toSlot() {
        return SlotHelper.getSlot(id + 10);
    }

    @Override
    public LSlot toLSlot(Slot slot,boolean inComplexContainer) {
        this.id = slot.id - 10;
        return this;
    }
}

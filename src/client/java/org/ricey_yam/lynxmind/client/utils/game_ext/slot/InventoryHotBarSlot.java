package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import net.minecraft.screen.slot.Slot;

/// LSlot ID Range: 0-8
///
/// Slot ID Range: 37-45(任何容器)
public class InventoryHotBarSlot extends LSlot {
    public InventoryHotBarSlot(){
        super();
        this.slotType = LSlotType.INVENTORY_HOTBAR;
    }
    public InventoryHotBarSlot(int id,boolean inComplexContainer) {
        super(id,inComplexContainer);
        this.slotType = LSlotType.INVENTORY_HOTBAR;
    }
    @Override
    public Slot toSlot() {
        return SlotHelper.getSlot(id + 37);
    }

    @Override
    public LSlot toLSlot(Slot slot,boolean inComplexContainer) {
        this.id = slot.id - 37;
        return this;
    }
}

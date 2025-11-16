package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import net.minecraft.screen.slot.Slot;

/// LSlot ID Range: 0-4 (0-3:输入 4:输出)
///
/// Slot ID Range: 0-4(仅玩家背包)
public class InventoryCraftingSlot extends LSlot {
    public InventoryCraftingSlot(){
        super();
        this.slotType = LSlotType.INVENTORY_CRAFTING;
    }
    public InventoryCraftingSlot(int id, boolean inComplexContainer) {
        super(id,inComplexContainer);
        this.slotType = LSlotType.INVENTORY_CRAFTING;
    }
    @Override
    public Slot toSlot() {
        return SlotHelper.getSlot(id);
    }

    @Override
    public LSlot toLSlot(Slot slot,boolean inComplexContainer) {
        this.inComplexContainer = inComplexContainer;
        this.id = slot.id;
        return this;
    }
}
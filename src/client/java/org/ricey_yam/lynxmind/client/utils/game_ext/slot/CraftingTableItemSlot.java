package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import net.minecraft.screen.slot.Slot;


/// LSlot ID Range: 0-9 (0-8:输入 9:输出)
///
/// Slot ID Range: 0-9 (仅工作台容器)
public class CraftingTableItemSlot extends LSlot{
    public CraftingTableItemSlot(){
        super();
        this.slotType = LSlotType.CRAFTING_TABLE;
    }

    public CraftingTableItemSlot(int id, boolean inComplexContainer) {
        super(id,inComplexContainer);
        this.slotType = LSlotType.CRAFTING_TABLE;
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

package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import net.minecraft.screen.slot.Slot;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ComplexContainerType;

/// LSlot ID Range: 0-8
///
/// Slot ID Range: 37-45(任何容器)
public class InventoryHotBarSlot extends LSlot {
    public InventoryHotBarSlot(){
        super();
        this.slotType = LSlotType.INVENTORY_HOTBAR;
    }
    public InventoryHotBarSlot(int id, ComplexContainerType complexContainerType) {
        super(id,complexContainerType);
        this.slotType = LSlotType.INVENTORY_HOTBAR;
    }
    @Override
    public Slot toSlot() {
        return SlotHelper.getSlot(id + 27 + SlotHelper.getOffsetFromLSlotToSlot(complexContainerType));
    }

    @Override
    public LSlot toLSlot(Slot slot,ComplexContainerType complexContainerType) {
        this.complexContainerType = complexContainerType;
        this.id = slot.id - 27 - SlotHelper.getOffsetFromLSlotToSlot(complexContainerType);
        return this;
    }
}

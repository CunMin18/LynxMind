package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import net.minecraft.screen.slot.Slot;

/// LSlot ID Range: -999
///
/// Slot ID Range: -999
public class CursorSlot extends LSlot {
    public static final int CURSOR_SLOT_INDEX = -999;
    public static final CursorSlot SLOT = new CursorSlot();

    public CursorSlot() {
        super(CURSOR_SLOT_INDEX,false);
        this.slotType = LSlotType.CURSOR;
    }

    @Override
    public Slot toSlot() {
        return SlotHelper.getSlot(CURSOR_SLOT_INDEX);
    }

    @Override
    public LSlot toLSlot(Slot slot,boolean inComplexContainer) {
        return SLOT;
    }
}

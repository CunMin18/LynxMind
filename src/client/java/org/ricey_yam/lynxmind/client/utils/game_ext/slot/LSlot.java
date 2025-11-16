package org.ricey_yam.lynxmind.client.utils.game_ext.slot;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.screen.slot.Slot;

@Getter
@Setter
/// 由于MC的Slot索引混乱 这里使用LSlot给Slot赋予一个特殊的ID(id)，便于引入Task
public abstract class LSlot {
    public static final int UNDEFINED_SLOT_INDEX = -999;
    public static final int ARMOR_HELMET_SLOT_INDEX = -3;
    public static final int ARMOR_CHESTPLATE_SLOT_INDEX = -4;
    public static final int ARMOR_LEGGINGS_SLOT_INDEX = -5;
    public static final int ARMOR_BOOTS_SLOT_INDEX = -6;

    protected LSlotType slotType = LSlotType.NONE;
    protected int id;
    protected boolean inComplexContainer;
    public LSlot(int id,boolean inComplexContainer) {
        this.id = id;
        this.inComplexContainer = inComplexContainer;
    }
    public LSlot(){
        this.id = UNDEFINED_SLOT_INDEX;
    }
    /// 获取原生的Slot ID
    public int getSlotId(){
        return toSlot().id;
    }

    /// 获取非原生Slot ID
    public int getLSlotId(){
        return this.id;
    }
    public abstract Slot toSlot();
    public abstract LSlot toLSlot(Slot slot,boolean inComplexContainer);
}

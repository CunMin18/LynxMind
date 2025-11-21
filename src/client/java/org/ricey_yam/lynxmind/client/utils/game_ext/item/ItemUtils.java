package org.ricey_yam.lynxmind.client.utils.game_ext.item;

import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemUtils {
    /// 获取物品ID
    public static String getItemID(ItemStack itemStack) {
        if(itemStack == null) return "";
        var item = itemStack.getItem();
        Identifier itemIdentifier = Registries.ITEM.getId(item);
        return itemIdentifier.toString();
    }

    public static float getItemAttackingDamage(ItemStack itemStack) {
        if(itemStack == null) return 1;
        var item = itemStack.getItem();
        if(item instanceof SwordItem swordItem){
            return swordItem.getAttackDamage();
        }
        if(item instanceof MiningToolItem miningToolItem){
            return miningToolItem.getAttackDamage();
        }
        return 1;
    }

}

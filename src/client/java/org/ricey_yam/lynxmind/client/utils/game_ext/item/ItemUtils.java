package org.ricey_yam.lynxmind.client.utils.game_ext.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.ricey_yam.lynxmind.client.ai.message.game_info.ui.SlotItemStack;
import org.ricey_yam.lynxmind.client.utils.game_ext.interaction.ComplexContainerType;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.LSlotType;
import org.ricey_yam.lynxmind.client.utils.game_ext.slot.SlotHelper;

import java.util.*;

public class ItemUtils {
    /// 获取物品ID
    public static String getItemName(ItemStack itemStack) {
        Item item = itemStack.getItem();
        Identifier itemIdentifier = Registries.ITEM.getId(item);
        return itemIdentifier.toString();
    }

}

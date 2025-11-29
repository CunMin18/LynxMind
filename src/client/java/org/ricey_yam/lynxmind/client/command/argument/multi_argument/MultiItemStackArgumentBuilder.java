package org.ricey_yam.lynxmind.client.command.argument.multi_argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemStackLite;
import org.ricey_yam.lynxmind.client.utils.game_ext.item.ItemUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiItemStackArgumentBuilder {
    public static ArgumentBuilder<ServerCommandSource, ?> build(CommandRegistryAccess registryAccess, Command<ServerCommandSource> executor,int limit){
        return MultiArgumentBuilder.build(registryAccess,executor,getMultiArgumentInfoList(registryAccess),limit);
    }

    public static List<ItemStackLite> getItemStackLiteList(CommandContext<ServerCommandSource> context){
        List<ItemStackLite> result = new ArrayList<>();

        var inputArgs = MultiArgumentBuilder.getInputArgs(context);

        Map<String, Integer> itemCountMap = new LinkedHashMap<>();
        for (int i = 0; i < inputArgs.size(); i++) {
            if (!ItemUtils.isValidItemId(inputArgs.get(i))) continue;

            var itemIdStr = inputArgs.get(i);

            if (i + 1 < inputArgs.size()) {
                int count;
                count = Integer.parseInt(inputArgs.get(i + 1));
                itemCountMap.put(itemIdStr, count);
                i++;
            }
        }

        for (var entry : itemCountMap.entrySet()) {
            result.add(new ItemStackLite(entry.getValue(), entry.getKey()));
        }

        return result;
    }

    private static List<MultiArgumentInfo> getMultiArgumentInfoList(CommandRegistryAccess registryAccess){
        var result = new ArrayList<MultiArgumentInfo>();
        result.add(new MultiArgumentInfo("item", ItemStackArgumentType.itemStack(registryAccess)));
        result.add(new MultiArgumentInfo("count", IntegerArgumentType.integer(1)));
        return result;
    }
}

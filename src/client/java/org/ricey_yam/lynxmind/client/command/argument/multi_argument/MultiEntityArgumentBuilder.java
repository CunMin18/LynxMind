package org.ricey_yam.lynxmind.client.command.argument.multi_argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import org.ricey_yam.lynxmind.client.utils.game_ext.entity.EntityUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiEntityArgumentBuilder {

    public static ArgumentBuilder<ServerCommandSource, ?> build(CommandRegistryAccess registryAccess, Command<ServerCommandSource> executor,int limit){
        return MultiArgumentBuilder.build(registryAccess,executor,getMultiArgumentInfoList(registryAccess),limit);
    }

    public static List<String> getEntityIDList(CommandContext<ServerCommandSource> context){
        List<String> result = new ArrayList<>();

        var inputArgs = MultiArgumentBuilder.getInputArgs(context);

        Map<String, Integer> itemCountMap = new LinkedHashMap<>();
        for (int i = 0; i < inputArgs.size(); i++) {
            if (!EntityUtils.isValidEntityID(inputArgs.get(i))) continue;

            String entityID = inputArgs.get(i);

            result.add(entityID);
        }

        return result;
    }
    private static List<MultiArgumentInfo> getMultiArgumentInfoList(CommandRegistryAccess registryAccess){
        var result = new ArrayList<MultiArgumentInfo>();
        result.add(new MultiArgumentInfo("entity", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE),SuggestionProviders.SUMMONABLE_ENTITIES));
        return result;
    }
}

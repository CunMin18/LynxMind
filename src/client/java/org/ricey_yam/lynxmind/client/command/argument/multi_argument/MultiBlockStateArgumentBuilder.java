package org.ricey_yam.lynxmind.client.command.argument.multi_argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.ricey_yam.lynxmind.client.utils.game_ext.block.BlockUtils;

import java.util.ArrayList;
import java.util.List;

public class MultiBlockStateArgumentBuilder {
    public static ArgumentBuilder<ServerCommandSource, ?> build(CommandRegistryAccess registryAccess, Command<ServerCommandSource> executor,int limit){
        return MultiArgumentBuilder.build(registryAccess,executor,getMultiArgumentInfoList(registryAccess),limit);
    }

    public static List<String> getBlockIDList(CommandContext<ServerCommandSource> context){
        List<String> result = new ArrayList<>();

        var inputArgs = MultiArgumentBuilder.getInputArgs(context);

        for (int i = 0; i < inputArgs.size(); i++) {
            if (!BlockUtils.isValidBlockID(inputArgs.get(i))) continue;

            String blockID = inputArgs.get(i);

            result.add(blockID);
        }

        return result;
    }
    private static List<MultiArgumentInfo> getMultiArgumentInfoList(CommandRegistryAccess registryAccess){
        var result = new ArrayList<MultiArgumentInfo>();
        result.add(new MultiArgumentInfo("block", BlockStateArgumentType.blockState(registryAccess)));
        return result;
    }
}

package org.ricey_yam.lynxmind.client.command.argument.multi_argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.ricey_yam.lynxmind.client.command.argument.ArgumentUtils;

import java.util.ArrayList;
import java.util.List;

public class MultiArgumentBuilder {
    protected static ArgumentBuilder<ServerCommandSource, ?> build(CommandRegistryAccess registryAccess, Command<ServerCommandSource> executor, List<MultiArgumentInfo> argumentInfos, int limit) {
        try {
            var root = getEmptyArgumentType();
            for (int i = 0; i < limit; i++) {
                root = getStackedArgumentType(registryAccess, executor, argumentInfos, limit - i,root);
            }
            return root;
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
    }

    protected static ArgumentBuilder<ServerCommandSource, ?> getStackedArgumentType(CommandRegistryAccess registryAccess, Command<ServerCommandSource> executor, List<MultiArgumentInfo> argumentInfos, int num,ArgumentBuilder<ServerCommandSource, ?> childNode) {
        var currentRoot = childNode != null ? childNode : getEmptyArgumentType();
        for (int j = argumentInfos.size() - 1; j >= 0; j--) {
            var info = argumentInfos.get(j).copy();
            info.setName(info.getName() + "_" + num);
            var currentNode = MultiArgumentInfo.build(info);
            currentRoot = ArgumentUtils.build(currentNode,currentRoot);
            if(j == argumentInfos.size() - 1) {
                currentRoot.executes(executor);
            }
        }
        return currentRoot;
    }

    protected static List<String> getInputArgs(CommandContext<ServerCommandSource> context){
        var inputArgs = new ArrayList<String>();
        for (var node : context.getNodes()) {
            if (node.getRange() != null && node.getRange().get(context.getInput()) != null && node.getNode() instanceof ArgumentCommandNode) {
                String input = node.getRange().get(context.getInput()).trim();
                if (!input.isEmpty()) {
                    inputArgs.add(input);
                }
            }
        }
        return inputArgs;
    }

    protected static ArgumentBuilder<ServerCommandSource, ?> getEmptyArgumentType(){
        return CommandManager.literal("");
    }
}

package org.ricey_yam.lynxmind.client.command.argument;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public class ArgumentUtils {
    public static ArgumentBuilder<ServerCommandSource, ?> build(ArgumentBuilder<ServerCommandSource, ?> parent, ArgumentBuilder<ServerCommandSource, ?> child){
        return parent.then(child);
    }
}

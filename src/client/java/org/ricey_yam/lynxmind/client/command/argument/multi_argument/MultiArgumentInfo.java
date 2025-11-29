package org.ricey_yam.lynxmind.client.command.argument.multi_argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@Getter
@Setter
public class MultiArgumentInfo {
    private String name;
    private ArgumentType<?> type;
    private SuggestionProvider<ServerCommandSource> suggestionProvider = null;

    public MultiArgumentInfo(String name, ArgumentType<?> type) {
        this.name = name;
        this.type = type;
    }
    public MultiArgumentInfo(String name, ArgumentType<?> type, SuggestionProvider<ServerCommandSource> suggestionProvider) {
        this(name,type);
        this.suggestionProvider = suggestionProvider;
    }
    public MultiArgumentInfo copy(){
        return new MultiArgumentInfo(name,type,suggestionProvider);
    }

    public static ArgumentBuilder<ServerCommandSource, ?> build(MultiArgumentInfo multiArgumentInfo){
        var built = CommandManager.argument(multiArgumentInfo.getName(),multiArgumentInfo.getType());
        return multiArgumentInfo.getSuggestionProvider() != null ? built.suggests(multiArgumentInfo.getSuggestionProvider()) : built;
    }
}

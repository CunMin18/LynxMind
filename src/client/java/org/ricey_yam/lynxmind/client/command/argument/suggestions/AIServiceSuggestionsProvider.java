package org.ricey_yam.lynxmind.client.command.argument.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class AIServiceSuggestionsProvider implements SuggestionProvider<ServerCommandSource> {
    @Getter
    private final static AIServiceSuggestionsProvider instance;

    static{
        instance = new AIServiceSuggestionsProvider();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
        builder.suggest("OPEN_AI");
        builder.suggest("VOLC_ENGINE");
        return builder.buildFuture();
    }
}

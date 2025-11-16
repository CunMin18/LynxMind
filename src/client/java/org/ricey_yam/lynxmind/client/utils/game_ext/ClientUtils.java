package org.ricey_yam.lynxmind.client.utils.game_ext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;

public class ClientUtils {
    public static ClientPlayerInteractionManager getController(){
        return MinecraftClient.getInstance().interactionManager;
    }
    public static GameOptions getOptions(){
        return MinecraftClient.getInstance().options;
    }
}

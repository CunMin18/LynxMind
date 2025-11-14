package org.ricey_yam.lynxmind.client.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommand {
    public static void registerCommands(){
        CommandRegistrationCallback.EVENT.register(LynxMindCommand::register);
    }
}

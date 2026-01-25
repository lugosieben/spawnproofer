package net.lugo.spawnproofer.registration;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.lugo.spawnproofer.command.SpawnprooferCommand;

public class Commands {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(SpawnprooferCommand::register);
    }
}

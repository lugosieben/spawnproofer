package net.lugo.spawnproofer;

import net.fabricmc.api.ModInitializer;

import net.lugo.spawnproofer.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registration.Commands;
import registration.KeyBindings;

public class Spawnproofer implements ModInitializer {
	public static final String MOD_ID = "spawnproofer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Spawnproofer (" + MOD_ID + ") initializing.");

        KeyBindings.registerKeybinds();
        Commands.registerCommands();

        ModConfig.HANDLER.load();

        LOGGER.info("Spawnproofer (" + MOD_ID + ") initialized.");
    }
}
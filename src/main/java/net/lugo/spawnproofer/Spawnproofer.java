package net.lugo.spawnproofer;

import net.fabricmc.api.ModInitializer;

import net.lugo.spawnproofer.config.ModConfig;
import net.lugo.spawnproofer.registration.Commands;
import net.lugo.spawnproofer.registration.KeyMappings;
import net.lugo.spawnproofer.registration.RenderingEvents;
import net.lugo.spawnproofer.util.IrisUtil;
import net.lugo.spawnproofer.util.RenderPipelines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spawnproofer implements ModInitializer {
	public static final String MOD_ID = "spawnproofer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Spawnproofer (" + MOD_ID + ") initializing.");

        KeyMappings.registerKeyMappings();
        Commands.registerCommands();

        RenderingEvents.register();

        if (IrisUtil.irisDetected()) {
            LOGGER.info("Iris detected.");
            LOGGER.info("Registering pipelines with Iris.");
            RenderPipelines.registerWithIris();
        }

        ModConfig.HANDLER.load();

        LOGGER.info("Spawnproofer (" + MOD_ID + ") initialized.");
	}
}
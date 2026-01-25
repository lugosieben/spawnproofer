package net.lugo.spawnproofer.registration;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.lugo.spawnproofer.OverlayManager;
import net.lugo.spawnproofer.Spawnproofer;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyMappings {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse(Spawnproofer.MOD_ID));
    private static final String BASE_KEY = "key." + Spawnproofer.MOD_ID;

    public static void registerKeyMappings() {
        registerOverlayKeyMapping();
    }

    private static void registerOverlayKeyMapping() {
        KeyMapping overlayKey = new KeyMapping(BASE_KEY + ".toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F6, CATEGORY);
        KeyBindingHelper.registerKeyBinding(overlayKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (overlayKey.consumeClick() && overlayKey.isDown()) {
                OverlayManager.toggle();
            }
        });
    }
}

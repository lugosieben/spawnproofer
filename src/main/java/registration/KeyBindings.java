package registration;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.lugo.spawnproofer.OverlayManager;
import net.lugo.spawnproofer.Spawnproofer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(Spawnproofer.MOD_ID));
    private static final String BASE_KEY = "key." + Spawnproofer.MOD_ID;

    public static void registerKeybinds() {
        registerOverlayKey();
    }

    private static void registerOverlayKey() {
        KeyBinding overlayKey = new KeyBinding(BASE_KEY + ".toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F6, CATEGORY);
        KeyBindingHelper.registerKeyBinding(overlayKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (overlayKey.wasPressed()) {
                OverlayManager.toggle();
            }
        });
    }
}

package net.lugo.spawnproofer.registration;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.lugo.spawnproofer.OverlayManager;

public class RenderingEvents {
    public static void register() {
        WorldRenderEvents.END_MAIN.register(context -> {
            OverlayManager.render(context);
            OverlayManager.draw();
        });
    }
}

package net.lugo.spawnproofer.registration;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.lugo.spawnproofer.OverlayManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public class RenderingEvents {
    public static void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
            final ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push("spawnproofer\u001erender");
            OverlayManager.render(context);
            profilerFiller.popPush("draw");
            OverlayManager.draw();
            profilerFiller.pop();
            profilerFiller.pop();
        });
    }
}

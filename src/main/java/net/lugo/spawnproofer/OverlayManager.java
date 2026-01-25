package net.lugo.spawnproofer;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.lugo.spawnproofer.config.ModConfig;
import net.lugo.spawnproofer.util.DistanceUtil;
import net.lugo.spawnproofer.util.HudMessage;
import net.lugo.spawnproofer.util.OverlayCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class OverlayManager {
    private static boolean activated = false;
    private static final Minecraft MC = Minecraft.getInstance();
    private static OverlayRenderer renderer;


    public static void toggle() {
        activated = !activated;
        MutableComponent message = Component.translatable("text.spawnproofer.message.toggle.on");
        if (!activated) message = Component.translatable("text.spawnproofer.message.toggle.off");
        HudMessage.show(message, ChatFormatting.GRAY);
    }

    public static void render(WorldRenderContext context) {
        if (MC.player == null || MC.level == null || !activated) return;

        renderWithCache(context);
    }

    private static OverlayRenderer getRenderer() {
        if (renderer == null) {
            renderer = new OverlayRenderer();
        }
        return renderer;
    }

    @SuppressWarnings("DataFlowIssue")
    private static void renderWithCache(WorldRenderContext context) {
        OverlayRenderer activeRenderer = getRenderer();
        activeRenderer.startBatch(context);

        int nearbySectionCount = 0;
        int playerChunkX = (int) Math.floor(MC.player.getX() / 16.0);
        int playerChunkZ = (int) Math.floor(MC.player.getZ() / 16.0);

        List<SectionPos> sectionsToRender = new ArrayList<>();
        BlockPos playerPos = MC.player.blockPosition();

        int effectiveChunkScanRange = Math.min(ModConfig.chunkScanRange, MC.options.getEffectiveRenderDistance());

        for (int dx = -effectiveChunkScanRange; dx <= effectiveChunkScanRange; dx++) {
            for (int dz = -effectiveChunkScanRange; dz <= effectiveChunkScanRange; dz++) {
                if (dx * dx + dz * dz > effectiveChunkScanRange * effectiveChunkScanRange) continue;
                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;
                for (int sectionY = MC.level.getMinSectionY(); sectionY <= MC.level.getMaxSectionY(); sectionY++) {
                    sectionsToRender.add(SectionPos.of(chunkX, sectionY, chunkZ));
                    if (DistanceUtil.getDistanceSquared(SectionPos.of(chunkX, sectionY, chunkZ), playerPos) < 256) {
                        nearbySectionCount++;
                    }
                }
            }
        }

        sectionsToRender.sort((a, b) -> {
            double distA = DistanceUtil.getDistanceSquared(a, playerPos);
            double distB = DistanceUtil.getDistanceSquared(b, playerPos);
            return Double.compare(distA, distB);
        });

        for (SectionPos sectionPos : sectionsToRender) {
            OverlayCache.queueForCompute(sectionPos);
        }

        OverlayCache.processQueue();

        for (SectionPos sectionPos : sectionsToRender) {
            OverlayCache.CacheSectionPosEntry entry = OverlayCache.get(sectionPos);
            if (entry == null || entry.blocks == null) continue;

            for (OverlayCache.CacheBlockPosEntry blockEntry : entry.blocks) {
                BlockPos blockPos = blockEntry.pos();
                int lightLevel = blockEntry.lightLevel();
                boolean isNearby = nearbySectionCount > 0;
                activeRenderer.addBlock(blockPos, lightLevel, isNearby);
            }
            nearbySectionCount--;
        }

        activeRenderer.endBatch();
    }

    public static void draw() {
        OverlayRenderer activeRenderer = getRenderer();
        activeRenderer.uploadThenDraw();
    }
}
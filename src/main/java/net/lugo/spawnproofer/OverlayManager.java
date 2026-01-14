package net.lugo.spawnproofer;

import net.lugo.spawnproofer.config.ModConfig;
import net.lugo.spawnproofer.util.DistanceUtil;
import net.lugo.spawnproofer.util.HudMessage;
import net.lugo.spawnproofer.util.OverlayCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.ArrayList;
import java.util.List;

public class OverlayManager {
    private static boolean activated = false;
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final OverlayRenderer renderer = new OverlayRenderer();

    public static void toggle() {
        activated = !activated;
        MutableText message = Text.translatable("text.spawnproofer.message.toggle.on");
        if (!activated) message = Text.translatable("text.spawnproofer.message.toggle.off");
        HudMessage.show(message, Formatting.DARK_AQUA);
    }

    public static void renderEnd() {
        if (MC.player == null || MC.world == null || MC.isPaused() || !activated) return;

        renderWithCache();
    }

    @SuppressWarnings("DataFlowIssue")
    private static void renderWithCache() {
        renderer.startBatch();

        int playerChunkX = (int) Math.floor(MC.player.getX() / 16.0);
        int playerChunkZ = (int) Math.floor(MC.player.getZ() / 16.0);

        List<ChunkSectionPos> sectionsToRender = new ArrayList<>();
        BlockPos playerPos = MC.player.getBlockPos();

        int effectiveChunkScanRange = Math.min(ModConfig.chunkScanRange, MC.options.getClampedViewDistance());

        for (int dx = -effectiveChunkScanRange; dx <= effectiveChunkScanRange; dx++) {
            for (int dz = -effectiveChunkScanRange; dz <= effectiveChunkScanRange; dz++) {
                if (dx * dx + dz * dz > effectiveChunkScanRange * effectiveChunkScanRange) continue;
                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;
                for (int sectionY = MC.world.getBottomSectionCoord(); sectionY <= MC.world.getTopSectionCoord(); sectionY++) {
                    sectionsToRender.add(ChunkSectionPos.from(chunkX, sectionY, chunkZ));
                }
            }
        }

        sectionsToRender.sort((a, b) -> {
            double distA = DistanceUtil.getDistanceSquared(a, playerPos);
            double distB = DistanceUtil.getDistanceSquared(b, playerPos);
            return Double.compare(distA, distB);
        });

        for (ChunkSectionPos sectionPos : sectionsToRender) {
            OverlayCache.queueForCompute(sectionPos);
        }

        OverlayCache.processQueue();

        for (ChunkSectionPos sectionPos : sectionsToRender) {
            OverlayCache.CacheSectionPosEntry entry = OverlayCache.get(sectionPos);
            if (entry == null || entry.blocks == null) continue;

            for (OverlayCache.CacheBlockPosEntry blockEntry : entry.blocks) {
                BlockPos blockPos = blockEntry.pos();
                int lightLevel = blockEntry.lightLevel();
                renderer.addBlock(MC.gameRenderer.getCamera(), blockPos, lightLevel);
            }
        }

        renderer.endBatch();
    }
}
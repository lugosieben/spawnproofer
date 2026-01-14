package net.lugo.spawnproofer.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.lugo.spawnproofer.Spawnproofer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class RenderLayers {
    public static final RenderPipeline SPAWNPROOFER_OVERLAY_PIPELINE = RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
                    .withLocation(Identifier.of(Spawnproofer.MOD_ID, "pipeline/overlay"))
                    .withCull(true)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withDepthWrite(true)
                    .build();

    public static final Function<Identifier, RenderLayer> SPAWNPROOFER_OVERLAY_RENDERLAYER = Util.memoize((Identifier texture) -> RenderLayer.of(
        Spawnproofer.MOD_ID + "/overlay",
        RenderSetup.builder(
                        SPAWNPROOFER_OVERLAY_PIPELINE)
                .texture("Sampler0", texture)
                .crumbling().outlineMode(RenderSetup.OutlineMode.AFFECTS_OUTLINE).build()));

}
package net.lugo.spawnproofer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.lugo.spawnproofer.config.ModConfig;
import net.lugo.spawnproofer.util.ColorHelper;
import net.lugo.spawnproofer.util.RenderPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class OverlayRenderer {
    private PoseStack poseStack;

    public BufferBuilder buffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    public MappableRingBuffer vertexBuffer;

    private final Identifier textureId = Identifier.fromNamespaceAndPath(Spawnproofer.MOD_ID, "textures/overlay.png");
    private TextureSetup textureSetup;

    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

    private final Minecraft MC = Minecraft.getInstance();

    private boolean batchStarted = false;
    private boolean hasVertices = false;

    protected OverlayRenderer() {}

    public final void startBatch(WorldRenderContext context) {
        if (batchStarted) return;

        if (this.textureSetup == null) {
            GpuTextureView gpuTextureView = MC.getTextureManager().getTexture(textureId).getTextureView();
            GpuSampler gpuSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
            this.textureSetup = TextureSetup.singleTexture(gpuTextureView, gpuSampler);
        }

        this.poseStack = context.matrices();
        Vec3 camPos = context.worldState().cameraRenderState.pos;

        this.getPoseStack().pushPose();
        this.getPoseStack().translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

        hasVertices = false;
        if (this.buffer == null) {
            this.buffer = new BufferBuilder(allocator, RenderPipelines.OVERLAY_PIPELINE.getVertexFormatMode(),  RenderPipelines.OVERLAY_PIPELINE.getVertexFormat());
        }

        batchStarted = true;
    }

    public final void addBlock(BlockPos pos, int lightLevel, boolean isNearby) {
        if (!batchStarted) return;

        if (ModConfig.hideGreen && lightLevel >= ModConfig.lightLevelThreshold) return;

        this.getPoseStack().pushPose();
        this.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

        Matrix4f positionMatrix = this.getPoseStack().last().pose();

        float[] colorFloats = ColorHelper.getOverlayColorFloats(lightLevel);
        float rf = colorFloats[0];
        float gf = colorFloats[1];
        float bf = colorFloats[2];

        if (!isNearby) {
            getPoseStack().translate(0, 3E-2, 0);
        } else {
            getPoseStack().translate(0, 5E-3, 0);
        }


        VertexConsumer vc = this.buffer;
        vc.addVertex(positionMatrix, 0, 1, 0).setColor(rf, gf, bf, 1f).setUv(0f, 0f).setUv2(0, 0).setNormal(1f, 0f, 1f);
        vc.addVertex(positionMatrix, 0, 1, 1).setColor(rf, gf, bf, 1f).setUv(0f, 1f).setUv2(0, 0).setNormal(1f, 0f, 1f);
        vc.addVertex(positionMatrix, 1, 1, 1).setColor(rf, gf, bf, 1f).setUv(1f, 1f).setUv2(0, 0).setNormal(1f, 0f, -1f);
        vc.addVertex(positionMatrix, 1, 1, 0).setColor(rf, gf, bf, 1f).setUv(1f, 0f).setUv2(0, 0).setNormal(1f, 0f, -1f);

        hasVertices = true;
        this.getPoseStack().popPose();
    }

    public final void endBatch() {
        if (!this.batchStarted) return;
        this.getPoseStack().popPose();
        this.batchStarted = false;
    }

    public final void uploadThenDraw() {
        if (!this.hasVertices || this.buffer == null) {
            this.buffer = null;
            return;
        }

        MeshData builtBuffer = this.buffer.buildOrThrow();
        MeshData.DrawState drawParams = builtBuffer.drawState();
        VertexFormat format = drawParams.format();

        GpuBuffer vertices = this.upload(builtBuffer, drawParams, format);
        this.draw(builtBuffer, drawParams, vertices);

        this.vertexBuffer.rotate();
        this.buffer = null;
    }

    private GpuBuffer upload(MeshData builtBuffer, MeshData.DrawState drawParams, VertexFormat format) {
        int vertexBufferSize = drawParams.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            vertexBuffer = new MappableRingBuffer(() -> Spawnproofer.MOD_ID + " overlay pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return vertexBuffer.currentBuffer();
    }

    private void draw(MeshData builtBuffer, MeshData.DrawState drawParams, GpuBuffer vertices) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (MC.getMainRenderTarget().getColorTextureView() == null) {
            return;
        }

        if (RenderPipelines.OVERLAY_PIPELINE.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
            indices = RenderPipelines.OVERLAY_PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(Objects.requireNonNull(builtBuffer.indexBuffer()));
            indexType = builtBuffer.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(RenderPipelines.OVERLAY_PIPELINE.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParams.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> Spawnproofer.MOD_ID + " overlay pipeline rendering", MC.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), MC.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelines.OVERLAY_PIPELINE);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.bindTexture("Sampler0", this.textureSetup.texure0(), this.textureSetup.sampler0());

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            renderPass.drawIndexed(0, 0, drawParams.indexCount(), 1);
        }

        builtBuffer.close();
    }

    protected PoseStack getPoseStack() {
        return this.poseStack;
    }
}

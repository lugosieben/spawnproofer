package net.lugo.spawnproofer;

import net.lugo.spawnproofer.util.RenderLayers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class OverlayRenderer {
    private static final Identifier CROSS_TEXTURE = Identifier.of(Spawnproofer.MOD_ID, "textures/cross.png");
    private final RenderLayer renderLayer;

    private final VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(new BufferAllocator(8192));
    private final MatrixStack matrixStack = new MatrixStack();

    protected VertexConsumer vertexConsumer;
    private boolean batchStarted = false;

    public OverlayRenderer() {
        this.renderLayer = RenderLayers.SPAWNPROOFER_OVERLAY_RENDERLAYER.apply(CROSS_TEXTURE);
    }

    public final void startBatch() {
        if (batchStarted) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getTextureManager() == null) {
            return;
        }

        vertexConsumer = vcp.getBuffer(renderLayer);

        batchStarted = true;
        onStartBatch();
    }

    public final void addBlock(Camera camera, BlockPos pos, int lightLevel) {
        if (!batchStarted) return;

        getMatrixStack().push();
        getMatrixStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        getMatrixStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180F));
        Vec3d transformedPos = Vec3d.of(pos).subtract(camera.getCameraPos());
        getMatrixStack().translate(transformedPos.x, transformedPos.y, transformedPos.z);

        Matrix4f positionMatrix = getMatrixStack().peek().getPositionMatrix();

        float rf = 0.0f;
        float gf = 1.0f;
        float bf = 0.0f;
        if (lightLevel == 0) {
            rf = 1.0f;
            gf = 0.0f;
        }

        onAddBlock(positionMatrix, rf, gf, bf, lightLevel, pos);

        getMatrixStack().pop();
    }

    public final void endBatch() {
        if (!batchStarted) return;
        onEndBatch();
        vcp.draw();
        batchStarted = false;
    }

    protected void onStartBatch() {}

    protected void onAddBlock(Matrix4f positionMatrix, float rf, float gf, float bf, int lightLevel, BlockPos pos) {
        getMatrixStack().translate(0, 1E-3, 0);

        VertexConsumer vc = this.vertexConsumer;
        vc.vertex(positionMatrix, 0, 1, 0).color(rf, gf, bf, 1f).texture(0f, 0f).light(0, 0);
        vc.vertex(positionMatrix, 0, 1, 1).color(rf, gf, bf, 1f).texture(0f, 1f).light(0, 0);
        vc.vertex(positionMatrix, 1, 1, 1).color(rf, gf, bf, 1f).texture(1f, 1f).light(0, 0);
        vc.vertex(positionMatrix, 1, 1, 0).color(rf, gf, bf, 1f).texture(1f, 0f).light(0, 0);
    }

    protected void onEndBatch() {}

    protected MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
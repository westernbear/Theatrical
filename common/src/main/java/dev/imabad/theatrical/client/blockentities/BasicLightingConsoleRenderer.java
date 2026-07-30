package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class BasicLightingConsoleRenderer
        implements BlockEntityRenderer<BasicLightingDeskBlockEntity, BasicLightingConsoleRenderer.State> {
    public BasicLightingConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BasicLightingDeskBlockEntity blockEntity, State state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, breakProgress);
        state.faders = blockEntity.getFaders().clone();
        state.grandMaster = blockEntity.getGrandMaster();
        state.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        Direction direction = state.facing.getAxis() == Direction.Axis.X ? state.facing.getOpposite() : state.facing;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(direction.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, builder) -> {
            for (int i = 0; i < state.faders.length; i++) {
                addTrack(builder, pose, faderX(i), faderBaseY(i));
            }
            addTrack(builder, pose, 14.5, 5.4);
        });
        submitNodeCollector.submitCustomGeometry(poseStack, TheatricalRenderTypes.FADER, (pose, builder) -> {
            for (int i = 0; i < state.faders.length; i++) {
                addFader(builder, pose, faderX(i), faderBaseY(i), Byte.toUnsignedInt(state.faders[i]));
            }
            addFader(builder, pose, 14.5, 5.4, Byte.toUnsignedInt(state.grandMaster));
        });
        poseStack.popPose();
    }

    private static double faderX(int index) {
        return 1.5 + (index % 6) * 1.2;
    }

    private static double faderBaseY(int index) {
        return 5.4 + (index / 6) * 7;
    }

    private static void addTrack(VertexConsumer builder, PoseStack.Pose pose, double x, double z) {
        builder.addVertex(pose, (float) (x / 16), 3 / 16f, (float) (z / 16))
                .setColor(0, 0, 0, 255).setNormal(pose, 0, 1, 0).setLineWidth(1);
        builder.addVertex(pose, (float) (x / 16), 3 / 16f, (float) ((z - 3) / 16))
                .setColor(0, 0, 0, 255).setNormal(pose, 0, 1, 0).setLineWidth(1);
    }

    private static void addFader(VertexConsumer builder, PoseStack.Pose pose, double x, double baseZ, int value) {
        float height = 0.4f / 16;
        float width = 0.6f / 16;
        float minX = (float) (x / 16) - width / 2;
        float minY = 3 / 16f;
        float minZ = (float) ((baseZ - value / 255f * 3) / 16);
        float maxX = minX + width;
        float maxY = minY + height;
        float maxZ = minZ + width;

        quad(builder, pose, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ);
        quad(builder, pose, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
        quad(builder, pose, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ);
        quad(builder, pose, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
        quad(builder, pose, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ);
        quad(builder, pose, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
    }

    private static void quad(VertexConsumer builder, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4) {
        builder.addVertex(pose, x1, y1, z1).setColor(0, 0, 0, 255);
        builder.addVertex(pose, x2, y2, z2).setColor(0, 0, 0, 255);
        builder.addVertex(pose, x3, y3, z3).setColor(0, 0, 0, 255);
        builder.addVertex(pose, x4, y4, z4).setColor(0, 0, 0, 255);
    }

    public static final class State extends BlockEntityRenderState {
        byte[] faders = new byte[0];
        byte grandMaster;
        Direction facing = Direction.NORTH;
    }
}

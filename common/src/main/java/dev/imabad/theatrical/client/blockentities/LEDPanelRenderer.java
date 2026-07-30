package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.imabad.theatrical.blockentities.light.LEDPanelBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.client.BakedModelCache;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public final class LEDPanelRenderer extends FixtureRenderer<LEDPanelBlockEntity> {
    public LEDPanelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void submitModels(FixtureRenderState state, PoseStack poseStack,
                                SubmitNodeCollector submitNodeCollector) {
        preparePoseStack(state, poseStack);
        submitModel(state, poseStack, submitNodeCollector,
                BakedModelCache.get(state.fixture.getStaticModel()));
    }

    @Override
    protected void preparePoseStack(FixtureRenderState state, PoseStack poseStack) {
        Direction facing = state.blockState.getValue(HangableBlock.FACING);

        poseStack.translate(0.5F, 0, 0.5F);
        if (state.hanging) {
            Direction hangDirection = state.blockState.getValue(HangableBlock.HANG_DIRECTION);
            poseStack.translate(0, 0.5, 0);
            if (hangDirection.getAxis() == Direction.Axis.Z) {
                poseStack.mulPose(com.mojang.math.Axis.XN.rotationDegrees(180));
            } else if (hangDirection == Direction.UP) {
                rotateVertical(poseStack, facing, true);
            } else if (hangDirection == Direction.DOWN) {
                rotateVertical(poseStack, facing, false);
            }
            poseStack.translate(0, -0.5, 0);
        }
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(-0.5F, 0, -0.5F);
        if (state.hanging) {
            float[] transform = state.supportingStructure == null
                    ? new float[]{0, 0.19f, 0}
                    : state.fixture.getTransforms(state.blockState, state.supportingStructure);
            poseStack.translate(transform[0], transform[1], transform[2]);
        }
    }

    @Override
    protected void submitEmitter(FixtureRenderState state, PoseStack poseStack,
                                 SubmitNodeCollector submitNodeCollector) {
        int color = state.color;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        float intensity = state.prevIntensity + (state.intensity - state.prevIntensity) * state.partialTick;
        int alpha = (int) intensity;
        poseStack.translate(0, 0, -0.01f);
        submitNodeCollector.submitCustomGeometry(poseStack, TheatricalRenderTypes.BEAM,
                (pose, builder) -> {
                    addVertex(builder, pose, r, g, b, alpha, 0, 1, 0);
                    addVertex(builder, pose, r, g, b, alpha, 1, 1, 0);
                    addVertex(builder, pose, r, g, b, alpha, 1, 0, 0);
                    addVertex(builder, pose, r, g, b, alpha, 0, 0, 0);
                });
    }

    private static void rotateVertical(PoseStack poseStack, Direction facing, boolean up) {
        switch (facing) {
            case NORTH -> poseStack.mulPose((up ? com.mojang.math.Axis.XP : com.mojang.math.Axis.XN)
                    .rotationDegrees(90));
            case SOUTH -> poseStack.mulPose((up ? com.mojang.math.Axis.XN : com.mojang.math.Axis.XP)
                    .rotationDegrees(90));
            case WEST -> {
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                poseStack.mulPose((up ? com.mojang.math.Axis.ZP : com.mojang.math.Axis.YN).rotationDegrees(90));
            }
            case EAST -> {
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                poseStack.mulPose((up ? com.mojang.math.Axis.ZN : com.mojang.math.Axis.YP).rotationDegrees(90));
            }
        }
    }
}

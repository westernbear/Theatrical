package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.client.BakedModelCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public final class FresnelRenderer extends FixtureRenderer<FresnelBlockEntity> {
    public FresnelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void submitModels(FixtureRenderState state, PoseStack poseStack,
                                SubmitNodeCollector submitNodeCollector) {
        applyBasePose(state, poseStack);
        submitModel(state, poseStack, submitNodeCollector,
                BakedModelCache.get(state.fixture.getStaticModel()));
        applyPan(state, poseStack);
        submitModel(state, poseStack, submitNodeCollector,
                BakedModelCache.get(state.fixture.getPanModel()));
        applyTilt(state, poseStack);
        submitModel(state, poseStack, submitNodeCollector,
                BakedModelCache.get(state.fixture.getTiltModel()));
    }

    @Override
    protected void preparePoseStack(FixtureRenderState state, PoseStack poseStack) {
        applyBasePose(state, poseStack);
        applyPan(state, poseStack);
        applyTilt(state, poseStack);
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
        poseStack.translate(0, -0.25f, 0.05f);
        submitNodeCollector.submitCustomGeometry(poseStack, dev.imabad.theatrical.client.TheatricalRenderTypes.BEAM,
                (pose, builder) -> {
                    addVertex(builder, pose, r, g, b, alpha, 0.34375f, 0.65625f, 0);
                    addVertex(builder, pose, r, g, b, alpha, 0.65625f, 0.65625f, 0);
                    addVertex(builder, pose, r, g, b, alpha, 0.65625f, 0.34375f, 0);
                    addVertex(builder, pose, r, g, b, alpha, 0.34375f, 0.34375f, 0);
                });
    }

    private void applyBasePose(FixtureRenderState state, PoseStack poseStack) {
        poseStack.translate(0.5F, 0, 0.5F);
        if (state.hanging) {
            Direction hangDirection = state.blockState.getValue(HangableBlock.HANG_DIRECTION);
            poseStack.translate(0, 0.5, 0);
            if (hangDirection.getAxis() == Direction.Axis.Z) {
                poseStack.mulPose((hangDirection == Direction.SOUTH
                        ? com.mojang.math.Axis.XP : com.mojang.math.Axis.XN).rotationDegrees(90));
            } else if (hangDirection.getAxis() == Direction.Axis.X) {
                poseStack.mulPose(com.mojang.math.Axis.ZN.rotationDegrees(
                        hangDirection == Direction.EAST ? 90 : -90));
            }
            poseStack.translate(0, -0.5, 0);
        }
        Direction facing = state.blockState.getValue(HangableBlock.FACING);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(
                facing.getAxis() == Direction.Axis.X ? facing.toYRot() : facing.getOpposite().toYRot()));
        poseStack.translate(-0.5F, 0, -0.5F);
        if (state.hanging) {
            float[] transform = state.supportingStructure == null
                    ? new float[]{0, 0.19f, 0}
                    : state.fixture.getTransforms(state.blockState, state.supportingStructure);
            poseStack.translate(transform[0], transform[1], transform[2]);
        }
    }

    private void applyPan(FixtureRenderState state, PoseStack poseStack) {
        float[] pivot = state.fixture.getPanRotationPosition();
        poseStack.translate(pivot[0], pivot[1], pivot[2]);
        float pan = state.prevPan + (state.pan - state.prevPan) * state.partialTick;
        poseStack.mulPose(com.mojang.math.Axis.YN.rotationDegrees(pan));
        poseStack.translate(-pivot[0], -pivot[1], -pivot[2]);
    }

    private void applyTilt(FixtureRenderState state, PoseStack poseStack) {
        float[] pivot = state.fixture.getTiltRotationPosition();
        poseStack.translate(pivot[0], pivot[1], pivot[2]);
        float tilt = state.prevTilt + (state.tilt - state.prevTilt) * state.partialTick;
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(tilt));
        poseStack.translate(-pivot[0], -pivot[1], -pivot[2]);
    }
}

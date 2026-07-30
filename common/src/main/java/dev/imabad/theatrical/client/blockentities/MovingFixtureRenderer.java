package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.client.BakedModelCache;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

abstract class MovingFixtureRenderer<T extends BaseLightBlockEntity> extends FixtureRenderer<T> {
    protected MovingFixtureRenderer(BlockEntityRendererProvider.Context context) {
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

    protected final void submitEmitterQuad(FixtureRenderState state, PoseStack poseStack,
                                           SubmitNodeCollector submitNodeCollector,
                                           float x, float y, float z, float min, float max) {
        int color = state.color;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        float intensity = state.prevIntensity
                + (state.intensity - state.prevIntensity) * state.partialTick;
        int alpha = (int) intensity;
        poseStack.translate(x, y, z);
        submitNodeCollector.submitCustomGeometry(poseStack, TheatricalRenderTypes.BEAM,
                (pose, builder) -> {
                    addVertex(builder, pose, r, g, b, alpha, min, max, 0);
                    addVertex(builder, pose, r, g, b, alpha, max, max, 0);
                    addVertex(builder, pose, r, g, b, alpha, max, min, 0);
                    addVertex(builder, pose, r, g, b, alpha, min, min, 0);
                });
    }

    private void applyBasePose(FixtureRenderState state, PoseStack poseStack) {
        poseStack.translate(0.5F, 0, 0.5F);
        if (state.hanging) {
            Direction hangDirection = state.blockState.getValue(HangableBlock.HANG_DIRECTION);
            poseStack.translate(0, 0.5, 0);
            if (hangDirection.getAxis() == Direction.Axis.Z) {
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(hangDirection == Direction.SOUTH ? -90 : 90));
            } else if (hangDirection.getAxis() == Direction.Axis.X) {
                poseStack.mulPose(com.mojang.math.Axis.ZN.rotationDegrees(hangDirection == Direction.EAST ? -90 : 90));
            }
            poseStack.translate(0, -0.5, 0);
        }
        Direction facing = state.blockState.getValue(HangableBlock.FACING);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(-0.5F, 0, -0.5F);
        if (state.hanging) {
            float[] transform = state.supportingStructure == null
                    ? new float[]{0, 0.19f, 0}
                    : state.fixture.getTransforms(state.blockState, state.supportingStructure);
            poseStack.translate(transform[0], transform[1] - 0.08f, transform[2]);
        }
        if (state.upsideDown) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        }
    }

    private void applyPan(FixtureRenderState state, PoseStack poseStack) {
        float[] pivot = state.fixture.getPanRotationPosition();
        poseStack.translate(pivot[0], pivot[1], pivot[2]);
        float pan = state.prevPan + (state.pan - state.prevPan) * state.partialTick;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(pan));
        poseStack.translate(-pivot[0], -pivot[1], -pivot[2]);
    }

    private void applyTilt(FixtureRenderState state, PoseStack poseStack) {
        float[] pivot = state.fixture.getTiltRotationPosition();
        poseStack.translate(pivot[0], pivot[1], pivot[2]);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(state.upsideDown ? -180 : 180));
        float tilt = state.prevTilt + (state.tilt - state.prevTilt) * state.partialTick;
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(tilt));
        poseStack.translate(-pivot[0], -pivot[1], -pivot[2]);
    }
}

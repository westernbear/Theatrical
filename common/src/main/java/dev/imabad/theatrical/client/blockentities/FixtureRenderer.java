package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.config.TheatricalConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class FixtureRenderer<T extends BaseLightBlockEntity>
        implements BlockEntityRenderer<T, FixtureRenderer.FixtureRenderState> {
    protected FixtureRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FixtureRenderState createRenderState() {
        return new FixtureRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, FixtureRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, breakProgress);
        state.fixture = blockEntity.getFixture();
        state.blockState = blockEntity.getBlockState();
        state.supportingStructure = blockEntity.getSupportingStructure().orElse(null);
        state.hanging = ((HangableBlock) state.blockState.getBlock())
                .isHanging(blockEntity.getLevel(), blockEntity.getBlockPos());
        state.upsideDown = blockEntity.isUpsideDown();
        state.partialTick = partialTick;
        state.intensity = (int) blockEntity.getIntensity();
        state.prevIntensity = blockEntity.getPrevIntensity();
        state.color = blockEntity.getColour();
        state.pan = blockEntity.getPan();
        state.prevPan = blockEntity.getPrevPan();
        state.tilt = blockEntity.getTilt();
        state.prevTilt = blockEntity.getPrevTilt();
    }

    @Override
    public void submit(FixtureRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.fixture == null || state.blockState == null) {
            return;
        }

        poseStack.pushPose();
        submitModels(state, poseStack, submitNodeCollector);
        poseStack.popPose();

        if (state.intensity > 0) {
            poseStack.pushPose();
            preparePoseStack(state, poseStack);
            submitEmitter(state, poseStack, submitNodeCollector);
            poseStack.popPose();
        }
    }

    protected abstract void submitModels(FixtureRenderState state, PoseStack poseStack,
                                         SubmitNodeCollector submitNodeCollector);

    protected abstract void preparePoseStack(FixtureRenderState state, PoseStack poseStack);

    protected void submitEmitter(FixtureRenderState state, PoseStack poseStack,
                                 SubmitNodeCollector submitNodeCollector) {
    }

    protected void submitModel(FixtureRenderState state, PoseStack poseStack,
                               SubmitNodeCollector submitNodeCollector, BlockStateModel model) {
        if (model == null) {
            return;
        }
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(state.blockPos.asLong()), parts);
        submitNodeCollector.submitBlockModel(
                poseStack,
                net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(),
                parts,
                BlockModelRenderState.EMPTY_TINTS,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1
        );
    }

    protected static void addVertex(VertexConsumer builder, PoseStack.Pose pose,
                                    int r, int g, int b, int a, float x, float y, float z) {
        builder.addVertex(pose, x, y, z).setColor(r, g, b, a);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return TheatricalConfig.INSTANCE.CLIENT.renderDistance;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return true;
    }

    public static final class FixtureRenderState extends BlockEntityRenderState {
        Fixture fixture;
        BlockState blockState;
        @Nullable BlockState supportingStructure;
        boolean hanging;
        boolean upsideDown;
        float partialTick;
        int intensity;
        int prevIntensity;
        int color;
        int pan;
        int prevPan;
        int tilt;
        int prevTilt;
    }
}

package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class MovingLightRenderer extends MovingFixtureRenderer<MovingLightBlockEntity> {
    public MovingLightRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void submitEmitter(FixtureRenderState state, PoseStack poseStack,
                                 SubmitNodeCollector submitNodeCollector) {
        submitEmitterQuad(state, poseStack, submitNodeCollector, 0, 0, 0.123f, 0.375f, 0.625f);
    }
}

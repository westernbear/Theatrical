package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.imabad.theatrical.blockentities.light.MovingWashBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class MovingWashRenderer extends MovingFixtureRenderer<MovingWashBlockEntity> {
    public MovingWashRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void submitEmitter(FixtureRenderState state, PoseStack poseStack,
                                 SubmitNodeCollector submitNodeCollector) {
        submitEmitterQuad(state, poseStack, submitNodeCollector, 0.5f, 0.87f, 0.29f, -0.3125f, 0.3125f);
    }
}

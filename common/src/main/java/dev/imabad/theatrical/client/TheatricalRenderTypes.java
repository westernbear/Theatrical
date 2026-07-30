package dev.imabad.theatrical.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.architectury.platform.Platform;
import dev.imabad.theatrical.Theatrical;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public final class TheatricalRenderTypes {
    private static final RenderPipeline FADER_PIPELINE = RenderPipelines.register(basePipeline("fader")
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withCull(true)
            .build());
    private static final RenderPipeline BEAM_PIPELINE = RenderPipelines.register(basePipeline("beam")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false))
            .withCull(false)
            .build());

    static {
        if (Platform.isModLoaded("iris")) {
            IrisCompat.register();
        }
    }

    public static final RenderType FIXTURE_MODEL = RenderType.create("theatrical_fixture_model",
            RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS, () -> RenderSystem.getSamplerCache()
                            .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                                    FilterMode.LINEAR, FilterMode.NEAREST, true))
                    .useLightmap()
                    .affectsCrumbling()
                    .createRenderSetup());
    public static final RenderType FADER = RenderType.create("theatrical_fader",
            RenderSetup.builder(FADER_PIPELINE).createRenderSetup());
    public static final RenderType BEAM = RenderType.create("theatrical_beam",
            RenderSetup.builder(BEAM_PIPELINE).sortOnUpload().createRenderSetup());

    private TheatricalRenderTypes() {
    }

    private static RenderPipeline.Builder basePipeline(String name) {
        return RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, "pipeline/" + name))
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS);
    }

    private static final class IrisCompat {
        private static void register() {
            IrisApi api = IrisApi.getInstance();
            api.assignPipeline(FADER_PIPELINE, IrisProgram.BASIC);
            api.assignPipeline(BEAM_PIPELINE, IrisProgram.BASIC);
        }
    }
}

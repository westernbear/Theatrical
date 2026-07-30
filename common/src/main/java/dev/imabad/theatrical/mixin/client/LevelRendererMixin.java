package dev.imabad.theatrical.mixin.client;

import dev.imabad.theatrical.lighting.LightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/**
 * This is heavily inspired by the system implemented in Ars-Nouvaeu found <a href="https://github.com/baileyholl/Ars-Nouveau/blob/main/src/main/java/com/hollingsworth/arsnouveau/common/light/">here</a>
 * This code is taken from LambDynamicLights, an MIT fabric mod: <a href="https://github.com/LambdAurora/LambDynamicLights">Github Link</a>
 *
 */
@Mixin(LightCoordsUtil.class)
public abstract class LevelRendererMixin {

    @Inject(
            method = "getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At("TAIL"),
            cancellable = true
    )
    private static void onGetLightmapCoordinates(LightCoordsUtil.BrightnessGetter brightnessGetter,
                                                 BlockAndLightGetter world, BlockState state, BlockPos pos,
                                                 CallbackInfoReturnable<Integer> cir) {
        if (!LightManager.shouldUpdateDynamicLight())
            return; // Do not touch to the value.
        if (!world.getBlockState(pos).isSolidRender())
            cir.setReturnValue(LightManager.getLightmapWithDynamicLight(pos, cir.getReturnValue()));
    }
}

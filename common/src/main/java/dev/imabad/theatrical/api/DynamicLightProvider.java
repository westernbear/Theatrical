package dev.imabad.theatrical.api;

import dev.imabad.theatrical.lighting.LightManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public interface DynamicLightProvider {

    BlockPos getOwnerPos();
    Vector3f getLightPos();
    Level getLightWorld();
    default boolean isLightEnabled() {
        return LightManager.containsLightSource(this);
    }
    default void setLightEnabled(boolean enabled) {
        if(enabled){
            LightManager.addLightSource(this);
        } else {
            LightManager.removeLightSource(this);
        }
    }
    int getLightLuminance();
    boolean shouldUpdateLight();
    boolean updateDynamicLight(LevelRenderer renderer);
    void scheduleTrackedChunksRebuild(LevelRenderer renderer);
    int getLightColour();
    float getLightSpread();
}

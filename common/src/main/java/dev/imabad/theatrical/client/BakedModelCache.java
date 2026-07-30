package dev.imabad.theatrical.client;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BakedModelCache {
    private static final Map<Identifier, ExtraModelKey<BlockStateModel>> MODELS = new LinkedHashMap<>();

    private BakedModelCache() {
    }

    public static void init() {
        for (Fixture fixture : Fixtures.FIXTURES) {
            add(fixture.getStaticModel());
            add(fixture.getPanModel());
            add(fixture.getTiltModel());
        }
        ModelLoadingPlugin.register(context -> MODELS.forEach((id, key) ->
                context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(id))));
    }

    private static void add(Identifier id) {
        if (id != null) {
            MODELS.computeIfAbsent(id, key -> ExtraModelKey.create(key::toString));
        }
    }

    public static BlockStateModel get(Identifier id) {
        ExtraModelKey<BlockStateModel> key = MODELS.get(id);
        return key == null ? null : ((FabricModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
    }
}

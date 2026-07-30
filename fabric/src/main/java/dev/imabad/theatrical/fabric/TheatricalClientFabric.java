package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.client.gui.screen.ArtNetConfigurationScreen;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.artnet.RequestNetworks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

public class TheatricalClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TheatricalClient.init();
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (stack.is(Items.CONFIGURATION_CARD.get())) {
                lines.add(Component.translatable("item.configurationcard.description.1"));
                lines.add(Component.translatable("item.configurationcard.description.2"));
            }
        });
        LevelRenderEvents.START_MAIN.register(this::renderWorldStartFabric);
        LevelExtractionEvents.END_EXTRACTION.register(context ->
                TheatricalClient.collectDebugGizmos(context.level(), context.levelRenderer()));
        ScreenEvents.AFTER_INIT.register((minecraft, screen, width, height) -> {
            if (screen instanceof OptionsScreen && minecraft.level != null) {
                new RequestNetworks().sendToServer();
                Screens.getWidgets(screen).add(Button.builder(Component.translatable("button.artnetconfig"),
                                button -> minecraft.setScreenAndShow(new ArtNetConfigurationScreen(screen)))
                        .bounds(width - 155, 6, 150, 20)
                        .build());
            }
        });
    }

    private void renderWorldStartFabric(LevelTerrainRenderContext context) {
        TheatricalClient.renderWorldLastAfterTripwire(context.levelRenderer());
    }
}

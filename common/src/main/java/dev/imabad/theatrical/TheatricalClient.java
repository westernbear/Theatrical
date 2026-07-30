package dev.imabad.theatrical;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.client.BakedModelCache;
import dev.imabad.theatrical.client.blockentities.BasicLightingConsoleRenderer;
import dev.imabad.theatrical.client.blockentities.FresnelRenderer;
import dev.imabad.theatrical.client.blockentities.LEDPanelRenderer;
import dev.imabad.theatrical.client.blockentities.MovingLightRenderer;
import dev.imabad.theatrical.client.blockentities.MovingWashRenderer;
import dev.imabad.theatrical.client.dmx.ArtNetManager;
import dev.imabad.theatrical.client.dmx.ArtNetToNetworkClientData;
import dev.imabad.theatrical.client.dmx.TheatricalArtNetClient;
import dev.imabad.theatrical.client.gui.screen.BasicLightingDeskScreen;
import dev.imabad.theatrical.client.gui.screen.GenericManualPanTiltScreen;
import dev.imabad.theatrical.client.gui.screen.GenericDMXConfigurationScreen;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.config.UniverseConfig;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.lighting.LightManager;
import dev.imabad.theatrical.net.OpenScreen;
import dev.imabad.theatrical.net.artnet.ListConsumers;
import dev.imabad.theatrical.net.artnet.NotifyConsumerChange;
import dev.imabad.theatrical.net.artnet.RequestNetworks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TheatricalClient {

    public static final Set<BlockPos> DEBUG_BLOCKS = new HashSet<>();
    private static ArtNetManager artNetManager;
    public static void init() {
        BakedModelCache.init();
        BlockEntityRendererRegistry.register(BlockEntities.MOVING_LIGHT.get(), MovingLightRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.MOVING_WASH.get(), MovingWashRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.LED_FRESNEL.get(), FresnelRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.LED_PANEL.get(), LEDPanelRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.BASIC_LIGHTING_DESK.get(), BasicLightingConsoleRenderer::new);
        artNetManager = new ArtNetManager();
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((event) -> {
            new RequestNetworks().sendToServer();
            if(TheatricalConfig.INSTANCE.CLIENT.artnetEnabled){
                artNetManager.getClient();
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((event) -> {
            onWorldClose();
        });
    }

    public static ArtNetManager getArtNetManager(){
        return artNetManager;
    }

    public static void onWorldClose(){
        artNetManager.shutdownAll();
        ArtNetToNetworkClientData.unload();
        DEBUG_BLOCKS.clear();
    }

    public static void renderWorldLastAfterTripwire(LevelRenderer levelRenderer){
        LightManager.updateAll(levelRenderer);
    }

    public static void collectDebugGizmos(ClientLevel level, LevelRenderer levelRenderer) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!dev.architectury.platform.Platform.isDevelopmentEnvironment()
                || !minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }

        try (var ignored = levelRenderer.collectPerFrameRenderThreadGizmos()) {
            for (BlockPos pos : DEBUG_BLOCKS) {
                Gizmos.cuboid(pos, GizmoStyle.stroke(0xFFFFFFFF));
                if (!(level.getBlockEntity(pos) instanceof BaseLightBlockEntity light)) {
                    continue;
                }

                Vec3 origin = Vec3.atCenterOf(pos);
                Gizmos.line(origin, origin.add(BaseLightBlockEntity.rayTraceDir(light).scale(25)), 0xFFFFFFFF);
                Gizmos.billboardTextOverBlock(String.format("Tilt: %.1f  Pan: %.1f", light.getTilt(), light.getPan()),
                        pos, 0, 0xFFFFFFFF, 0.025f);
                BlockState state = level.getBlockState(pos);
                Gizmos.billboardTextOverBlock("Facing: " + state.getValue(HangableBlock.FACING),
                        pos, 1, 0xFFFFFFFF, 0.025f);
            }
        }
    }

    public static Color getRandomColor(UUID id) {

        byte[] bytes = UUID2Bytes(id);

        int r= Math.abs(bytes[0]);
        int g = Math.abs(bytes[1]);
        int b = Math.abs(bytes[2]);

        return new Color(r, g, b);
    }

    public static byte[] UUID2Bytes(UUID uuid) {

        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();
        return ByteBuffer.allocate(16).putLong(hi).putLong(lo).array();
    }

    public static void handleConsumerChange(NotifyConsumerChange notifyConsumerChange){
        if(TheatricalConfig.INSTANCE.CLIENT.artnetEnabled){
            TheatricalArtNetClient artNetClient = getArtNetManager().getClient();
            if(TheatricalConfig.INSTANCE.CLIENT.universes.containsKey(notifyConsumerChange.getUniverse())){
                UniverseConfig universeConfig = TheatricalConfig.INSTANCE.CLIENT.universes.get(notifyConsumerChange.getUniverse());
                DMXDevice dmxDevice = notifyConsumerChange.getDmxDevice();
                if(notifyConsumerChange.getChangeType() == NotifyConsumerChange.ChangeType.ADD){
                    artNetClient.addDevice((short) universeConfig.subnet,(short)  universeConfig.universe, dmxDevice.getDeviceId(), dmxDevice);
                } else if(notifyConsumerChange.getChangeType() == NotifyConsumerChange.ChangeType.UPDATE) {
                    artNetClient.updateDevice((short) universeConfig.subnet,(short)  universeConfig.universe, dmxDevice.getDeviceId(), dmxDevice);
                } else {
                    artNetClient.removeDevice((short) universeConfig.subnet,(short)  universeConfig.universe, dmxDevice.getDeviceId());
                }
            }
        }
    }

    public static void handleListConsumers(ListConsumers listConsumers){
        if(TheatricalConfig.INSTANCE.CLIENT.artnetEnabled) {
            TheatricalArtNetClient artNetClient = getArtNetManager().getClient();
            if(TheatricalConfig.INSTANCE.CLIENT.universes.containsKey(listConsumers.getUniverse())) {
                UniverseConfig universeConfig = TheatricalConfig.INSTANCE.CLIENT.universes.get(listConsumers.getUniverse());
                for (DMXDevice dmxDevice : listConsumers.getDmxDevices()) {
                    artNetClient.addDevice((short) universeConfig.subnet, (short) universeConfig.universe, dmxDevice.getDeviceId(), dmxDevice);
                }
            }
        }
    }

    public static void handleOpenScreen(OpenScreen openScreen){
        switch (openScreen.getScreen()){
            case GENERIC_DMX -> {
                if(Minecraft.getInstance().level.getBlockEntity(openScreen.getPos()) instanceof DMXConsumer dmxConsumer){
                    Minecraft.getInstance().setScreenAndShow(new GenericDMXConfigurationScreen<>(dmxConsumer, openScreen.getPos(), dmxConsumer.getTranslationKey()));
                }
            }
            case GENERIC_PAN_TILT -> {
                if(Minecraft.getInstance().level.getBlockEntity(openScreen.getPos()) instanceof BaseDMXConsumerLightBlockEntity be) {
                    Minecraft.getInstance().setScreenAndShow(new GenericManualPanTiltScreen(be, be.getBlockState().getBlock().getDescriptionId()));
                }
            }
            case BASIC_LIGHTING_DESK -> {
                if(Minecraft.getInstance().level.getBlockEntity(openScreen.getPos()) instanceof BasicLightingDeskBlockEntity bse) {
                    Minecraft.getInstance().setScreenAndShow(new BasicLightingDeskScreen(bse));
                }
            }
        }
    }
}

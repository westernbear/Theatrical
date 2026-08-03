package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.client.gui.screen.ArtNetConfigurationScreen;
import dev.imabad.theatrical.client.gui.screen.BasicLightingDeskScreen;
import dev.imabad.theatrical.client.gui.screen.GenericManualPanTiltScreen;
import dev.imabad.theatrical.client.gui.widgets.BasicSlider;
import dev.imabad.theatrical.client.gui.widgets.FaderWidget;
import io.github.westernbear.lumina.api.LuminaLights;
import io.github.westernbear.lumina.light.LightCaster;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class TheatricalClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
                .create()) {
            BlockPos origin = singleplayer.getServer().computeOnServer(server ->
                    singleplayer.getConnection().getServerPlayer().blockPosition().east(4));
            BlockPos manualFresnel = origin.west();
            BlockPos artNetInterface = origin.west().north(2);
            BlockPos lightingDesk = origin.north(2);

            singleplayer.getServer().runOnServer(server -> {
                var level = singleplayer.getConnection().getServerLevel();
                placeAndLight(level, origin, Blocks.MOVING_LIGHT_BLOCK.get(), dmx(255, 255, 64, 32, 128, 128, 128));
                placeAndLight(level, origin.south(2), Blocks.MOVING_WASH_BLOCK.get(), dmx(255, 32, 64, 255, 128, 128, 128));
                placeAndLight(level, origin.south(4), Blocks.LED_FRESNEL.get(), dmx(255, 255, 180, 64));
                placeAndLight(level, origin.south(6), Blocks.LED_PANEL.get(), dmx(255, 64, 180, 255));
                placeAndLight(level, manualFresnel, Blocks.LED_FRESNEL.get(), dmx(255, 255, 255, 255));
                level.setBlock(artNetInterface, Blocks.ART_NET_INTERFACE.get().defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(lightingDesk, Blocks.BASIC_LIGHTING_DESK.get().defaultBlockState(), Block.UPDATE_ALL);
            });

            context.waitTick();
            singleplayer.getConnection().waitForClientboundPackets();
            singleplayer.getConnection().waitForChunksRender();
            context.waitFor(client -> isLit(client.level.getBlockEntity(origin))
                    && isLit(client.level.getBlockEntity(origin.south(2)))
                    && isLit(client.level.getBlockEntity(origin.south(4)))
                    && isLit(client.level.getBlockEntity(origin.south(6)))
                    && hasLuminaSpotlights(client.level, origin, manualFresnel)
                    && spotlightMatches(client.level, LuminaLights.clientLights(client.level), origin,
                    255, 255, 64, 32)
                    && spotlightMatches(client.level, LuminaLights.clientLights(client.level), origin.south(2),
                    255, 32, 64, 255)
                    && spotlightMatches(client.level, LuminaLights.clientLights(client.level), origin.south(4),
                    255, 255, 180, 64)
                    && spotlightMatches(client.level, LuminaLights.clientLights(client.level), manualFresnel,
                    255, 255, 255, 255)
                    && client.level.getBlockState(artNetInterface).is(Blocks.ART_NET_INTERFACE.get())
                    && client.level.getBlockState(lightingDesk).is(Blocks.BASIC_LIGHTING_DESK.get()));
            context.getInput().lookAt(origin.south(3).above());
            context.waitTicks(20);
            context.takeScreenshot("theatrical_lit_fixtures");

            LightCaster manualBefore = serverSpotlight(singleplayer, manualFresnel);
            context.getInput().lookAt(manualFresnel);
            context.waitTick();
            context.getInput().pressKey(options -> options.keyUse);
            context.waitForScreen(GenericManualPanTiltScreen.class);
            dragSlider(context, 0, 0.8);
            dragSlider(context, 1, 0.2);
            clickWidget(context, Button.class, 0);
            singleplayer.getConnection().waitForServerboundPackets();
            singleplayer.getServer().waitFor(server -> {
                ServerLevel level = singleplayer.getConnection().getServerLevel();
                if (!(level.getBlockEntity(manualFresnel) instanceof BaseLightBlockEntity fixture)
                        || fixture.getPan() == 0 || fixture.getTilt() == 0) {
                    return false;
                }
                return findSpotlight(LuminaLights.all(level), manualFresnel)
                        .filter(light -> light.getId().equals(manualBefore.getId()))
                        .filter(light -> changedTransform(light, manualBefore))
                        .isPresent();
            });
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitFor(client -> client.level.getBlockEntity(manualFresnel) instanceof BaseLightBlockEntity fixture
                    && fixture.getPan() != 0
                    && fixture.getTilt() != 0
                    && spotlightMatches(client.level, LuminaLights.clientLights(client.level), manualFresnel,
                    255, 255, 255, 255)
                    && findSpotlight(LuminaLights.clientLights(client.level), manualFresnel)
                    .filter(light -> light.getId().equals(manualBefore.getId()))
                    .filter(light -> changedTransform(light, manualBefore))
                    .isPresent());
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitForScreen(null);

            LightCaster movingBefore = serverSpotlight(singleplayer, origin);
            LightCaster washBefore = serverSpotlight(singleplayer, origin.south(2));
            singleplayer.getServer().runOnServer(server -> {
                ServerLevel level = singleplayer.getConnection().getServerLevel();
                ((BaseDMXConsumerLightBlockEntity) level.getBlockEntity(origin))
                        .consume(dmx(128, 32, 128, 255, 255, 64, 192));
                ((BaseDMXConsumerLightBlockEntity) level.getBlockEntity(origin.south(2)))
                        .consume(dmx(160, 220, 48, 96, 32, 210, 80));
            });
            singleplayer.getServer().waitFor(server -> {
                ServerLevel level = singleplayer.getConnection().getServerLevel();
                return spotlightMatches(level, LuminaLights.all(level), origin, 128, 32, 128, 255)
                        && findSpotlight(LuminaLights.all(level), origin)
                        .filter(light -> light.getId().equals(movingBefore.getId()))
                        .filter(light -> changedTransform(light, movingBefore))
                        .filter(light -> light.getOuterAngle() != movingBefore.getOuterAngle())
                        .isPresent()
                        && spotlightMatches(level, LuminaLights.all(level), origin.south(2), 160, 220, 48, 96)
                        && findSpotlight(LuminaLights.all(level), origin.south(2))
                        .filter(light -> light.getId().equals(washBefore.getId()))
                        .filter(light -> changedTransform(light, washBefore))
                        .isPresent();
            });
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitFor(client -> spotlightMatches(client.level, LuminaLights.clientLights(client.level), origin,
                    128, 32, 128, 255)
                    && spotlightMatches(client.level, LuminaLights.clientLights(client.level), origin.south(2),
                    160, 220, 48, 96));
            context.takeScreenshot("theatrical_moved_spotlights");

            singleplayer.getServer().runOnServer(server ->
                    ((BaseDMXConsumerLightBlockEntity) singleplayer.getConnection().getServerLevel()
                            .getBlockEntity(origin.south(2))).consume(dmx(0, 220, 48, 96, 32, 210, 80)));
            singleplayer.getServer().waitFor(server ->
                    findSpotlight(LuminaLights.all(singleplayer.getConnection().getServerLevel()), origin.south(2)).isEmpty());
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitFor(client -> findSpotlight(LuminaLights.clientLights(client.level), origin.south(2)).isEmpty());

            singleplayer.getServer().runOnServer(server ->
                    ((BaseDMXConsumerLightBlockEntity) singleplayer.getConnection().getServerLevel()
                            .getBlockEntity(origin.south(2))).consume(dmx(96, 220, 48, 96, 32, 210, 80)));
            singleplayer.getServer().waitFor(server -> spotlightMatches(singleplayer.getConnection().getServerLevel(),
                    LuminaLights.all(singleplayer.getConnection().getServerLevel()), origin.south(2), 96, 220, 48, 96));
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitFor(client -> spotlightMatches(client.level, LuminaLights.clientLights(client.level),
                    origin.south(2), 96, 220, 48, 96));

            singleplayer.getServer().runOnServer(server ->
                    singleplayer.getConnection().getServerLevel().removeBlock(manualFresnel, false));
            singleplayer.getServer().waitFor(server ->
                    findSpotlight(LuminaLights.all(singleplayer.getConnection().getServerLevel()), manualFresnel).isEmpty());
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitFor(client -> findSpotlight(LuminaLights.clientLights(client.level), manualFresnel).isEmpty());

            context.getInput().lookAt(artNetInterface);
            context.waitTick();
            context.getInput().pressKey(options -> options.keyUse);
            context.waitForScreen(ArtNetConfigurationScreen.class);
            context.setScreen(() -> null);

            context.setScreen(() -> {
                if (!(Minecraft.getInstance().level.getBlockEntity(lightingDesk) instanceof BasicLightingDeskBlockEntity desk)) {
                    throw new AssertionError("Missing lighting desk block entity at " + lightingDesk);
                }
                return new BasicLightingDeskScreen(desk);
            });
            context.waitForScreen(BasicLightingDeskScreen.class);
            context.waitFor(client -> widgetCount(client.gui.screen(), FaderWidget.class) == 13);
            context.takeScreenshot("theatrical_basic_lighting_desk");
            context.setScreen(() -> null);
        }
    }

    private static void placeAndLight(ServerLevel level, BlockPos pos, Block block, byte[] dmx) {
        level.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(pos) instanceof BaseDMXConsumerLightBlockEntity light)) {
            throw new AssertionError("Missing light block entity at " + pos);
        }
        light.consume(dmx);
    }

    private static boolean isLit(net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        return blockEntity instanceof BaseLightBlockEntity light
                && light.getIntensity() > 0
                && light.getColour() != 0;
    }

    private static boolean hasLuminaSpotlights(Level level, BlockPos origin, BlockPos manualFresnel) {
        Set<BlockPos> positions = LuminaLights.clientLights(level).stream()
                .filter(light -> light.getKind() == LightCaster.Kind.BLOCK)
                .filter(light -> light.getVolumetric().enabled())
                .filter(light -> light.getInnerAngle() > 0 && light.getOuterAngle() > light.getInnerAngle())
                .map(LightCaster::getBlockPos)
                .collect(Collectors.toSet());
        return positions.contains(origin)
                && positions.contains(origin.south(2))
                && positions.contains(origin.south(4))
                && positions.contains(manualFresnel)
                && !positions.contains(origin.south(6));
    }

    private static boolean spotlightMatches(Level level, List<LightCaster> lights, BlockPos pos,
                                             int intensity, int red, int green, int blue) {
        if (!(level.getBlockEntity(pos) instanceof BaseLightBlockEntity fixture)
                || fixture.getIntensity() != intensity
                || fixture.getRed() != red
                || fixture.getGreen() != green
                || fixture.getBlue() != blue) {
            return false;
        }
        var direction = BaseLightBlockEntity.rayTraceDir(fixture).normalize();
        float outerAngle = (float) Math.toDegrees(Math.atan2(fixture.getLightSpread(), fixture.getMaxLightDistance()));
        float distance = Mth.clamp((float) fixture.getDistance(), 0.0F, 200.0F);
        return findSpotlight(lights, pos).filter(light -> light.isEnabled()
                        && light.getKind() == LightCaster.Kind.BLOCK
                        && pos.equals(light.getBlockPos())
                        && light.getPersistence() == LightCaster.Persistence.TEMPORARY
                        && light.getVolumetric().enabled()
                        && !light.getShadow().enabled()
                        && !light.getFlare().enabled()
                        && close(light.getDirection().x(), direction.x)
                        && close(light.getDirection().y(), direction.y)
                        && close(light.getDirection().z(), direction.z)
                        && close(light.getPosition().x(), 0.5 + direction.x * 0.25)
                        && close(light.getPosition().y(), 0.5 + direction.y * 0.25)
                        && close(light.getPosition().z(), 0.5 + direction.z * 0.25)
                        && close(light.getColor().x(), red / 255.0)
                        && close(light.getColor().y(), green / 255.0)
                        && close(light.getColor().z(), blue / 255.0)
                        && close(light.getIntensity(), intensity)
                        && close(light.getDistance(), distance)
                        && close(light.getInnerAngle(), outerAngle * 0.75)
                        && close(light.getOuterAngle(), outerAngle)
                        && resolvedPositionMatches(level, light, pos))
                .isPresent();
    }

    private static boolean resolvedPositionMatches(Level level, LightCaster light, BlockPos pos) {
        return light.resolve(level, 1.0F).filter(resolved ->
                close(resolved.position().x(), pos.getX() + light.getPosition().x())
                        && close(resolved.position().y(), pos.getY() + light.getPosition().y())
                        && close(resolved.position().z(), pos.getZ() + light.getPosition().z()))
                .isPresent();
    }

    private static LightCaster serverSpotlight(TestSingleplayerContext singleplayer, BlockPos pos) {
        return singleplayer.getServer().computeOnServer(server ->
                findSpotlight(LuminaLights.all(singleplayer.getConnection().getServerLevel()), pos)
                        .orElseThrow(() -> new AssertionError("Missing Lumina spotlight at " + pos)));
    }

    private static Optional<LightCaster> findSpotlight(List<LightCaster> lights, BlockPos pos) {
        return lights.stream()
                .filter(light -> light.getKind() == LightCaster.Kind.BLOCK)
                .filter(light -> pos.equals(light.getBlockPos()))
                .findFirst();
    }

    private static boolean changedTransform(LightCaster light, LightCaster before) {
        return !light.getDirection().equals(before.getDirection())
                && !light.getPosition().equals(before.getPosition());
    }

    private static void dragSlider(ClientGameTestContext context, int index, double targetFraction) {
        context.waitFor(client -> widgetCount(client.gui.screen(), BasicSlider.class) > index);
        double[] positions = context.computeOnClient(client -> {
            BasicSlider slider = widget(client.gui.screen(), BasicSlider.class, index);
            double scaleX = client.getWindow().getScreenWidth() / (double) client.getWindow().getGuiScaledWidth();
            double scaleY = client.getWindow().getScreenHeight() / (double) client.getWindow().getGuiScaledHeight();
            return new double[]{
                    (slider.getX() + slider.getWidth() / 2.0) * scaleX,
                    (slider.getX() + slider.getWidth() * targetFraction) * scaleX,
                    (slider.getY() + slider.getHeight() / 2.0) * scaleY
            };
        });
        context.getInput().setCursorPos(positions[0], positions[2]);
        context.getInput().holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
        context.getInput().setCursorPos(positions[1], positions[2]);
        context.waitTick();
        context.getInput().releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
    }

    private static void clickWidget(ClientGameTestContext context, Class<? extends AbstractWidget> type, int index) {
        context.waitFor(client -> widgetCount(client.gui.screen(), type) > index);
        double[] position = context.computeOnClient(client -> {
            AbstractWidget widget = widget(client.gui.screen(), type, index);
            double scaleX = client.getWindow().getScreenWidth() / (double) client.getWindow().getGuiScaledWidth();
            double scaleY = client.getWindow().getScreenHeight() / (double) client.getWindow().getGuiScaledHeight();
            return new double[]{
                    (widget.getX() + widget.getWidth() / 2.0) * scaleX,
                    (widget.getY() + widget.getHeight() / 2.0) * scaleY
            };
        });
        context.getInput().setCursorPos(position[0], position[1]);
        context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        context.waitTick();
    }

    private static int widgetCount(Screen screen, Class<? extends AbstractWidget> type) {
        return screen == null ? 0 : (int) screen.children().stream().filter(type::isInstance).count();
    }

    private static <T extends AbstractWidget> T widget(Screen screen, Class<T> type, int index) {
        List<T> widgets = screen.children().stream().filter(type::isInstance).map(type::cast).toList();
        if (index >= widgets.size()) {
            throw new AssertionError("Missing " + type.getSimpleName() + " at index " + index);
        }
        return widgets.get(index);
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.0001;
    }

    private static byte[] dmx(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }
}

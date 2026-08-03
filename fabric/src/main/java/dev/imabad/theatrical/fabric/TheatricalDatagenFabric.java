package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.control.BasicLightingDeskBlock;
import dev.imabad.theatrical.blocks.rigging.TankTrapBlock;
import dev.imabad.theatrical.items.Items;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public final class TheatricalDatagenFabric implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(Lang::new);
        pack.addProvider(Models::new);
    }

    public static final class Models extends FabricModelProvider {
        public Models(FabricPackOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators generators) {
            generators.createTrivialCube(Blocks.ART_NET_INTERFACE.get());
            generators.createTrivialCube(Blocks.REDSTONE_INTERFACE.get());
            generators.createAxisAlignedPillarBlockCustomModel(Blocks.TRUSS_BLOCK.get(),
                    BlockModelGenerators.plainVariant(id("block/truss")));
            generators.blockStateOutput.accept(MultiVariantGenerator
                    .dispatch(Blocks.BASIC_LIGHTING_DESK.get(),
                            BlockModelGenerators.plainVariant(id("block/basic_lighting_desk")))
                    .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
            generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.TANK_TRAP.get())
                    .with(PropertyDispatch.initial(TankTrapBlock.HAS_PIPE)
                            .select(true, BlockModelGenerators.plainVariant(id("block/tank_trap_with_pipe")))
                            .select(false, BlockModelGenerators.plainVariant(id("block/tank_trap")))));
        }

        @Override
        public void generateItemModels(ItemModelGenerators generators) {
            generators.generateFlatItem(Items.CONFIGURATION_CARD.get(), ModelTemplates.FLAT_ITEM);
            parent(generators, Blocks.LED_FRESNEL.get(), "block/fresnel/fresnel_whole");
            parent(generators, Blocks.PIPE_BLOCK.get(), "block/vertical_pipe");
            parent(generators, Blocks.MOVING_LIGHT_BLOCK.get(), "block/moving_light/moving_head_whole");
            parent(generators, Blocks.MOVING_WASH_BLOCK.get(), "block/moving_wash/moving_wash_whole");
            parent(generators, Blocks.LED_PANEL.get(), "block/led_panel");
            generators.generateFlatItem(Items.FIXTURE_FOCUSER.get(), ModelTemplates.FLAT_ITEM);
        }

        private static void parent(ItemModelGenerators generators, Block block, String parent) {
            generators.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(id(parent)));
        }
    }

    public static final class Lang extends FabricLanguageProvider {
        protected Lang(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, "en_us", registries);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder translations) {
            translations.add(Blocks.ART_NET_INTERFACE.get(), "ArtNet Interface");
            translations.add(Blocks.MOVING_LIGHT_BLOCK.get(), "Moving Light");
            translations.add(Blocks.MOVING_WASH_BLOCK.get(), "Moving Wash");
            translations.add(Blocks.PIPE_BLOCK.get(), "Rigging Pipe");
            translations.add(Blocks.LED_FRESNEL.get(), "LED Fresnel");
            translations.add(Blocks.TRUSS_BLOCK.get(), "MT100 Truss");
            translations.add(Blocks.REDSTONE_INTERFACE.get(), "Redstone Interface");
            translations.add(Blocks.TANK_TRAP.get(), "Tank Trap");
            translations.add(Blocks.LED_PANEL.get(), "LED Panel");
            translations.add(Blocks.BASIC_LIGHTING_DESK.get(), "Basic Lighting Desk");
            translations.add(Items.CONFIGURATION_CARD.get(), "Configuration Card");
            translations.add(Items.FIXTURE_FOCUSER.get(), "Fixture Focuser");
            translations.add("itemGroup.theatrical", "Theatrical");
            translations.add("artneti.dmxUniverse", "Network Universe");
            translations.add("artneti.ipAddress", "IP Address");
            translations.add("artneti.save", "Save");
            translations.add("artneti.notConnected", "No data received");
            translations.add("artneti.notAuthorized", "You're not authorized!");
            translations.add("artneti.lastReceived", "Data received %d second(s) ago");
            translations.add("fixture.dmxStart", "Start address");
            translations.add("fixture.pan", "Pan");
            translations.add("fixture.tilt", "Tilt");
            translations.add("screen.movinglight", "Moving Light");
            translations.add("button.artnetconfig", "ArtNet Config");
            translations.add("screen.artnetconfig.enabled", "ArtNet Enabled: %s");
            translations.add("ui.control.step", "Step - %s");
            translations.add("ui.control.modes.run", "Run Mode");
            translations.add("ui.control.modes.program", "Program Mode");
            translations.add("ui.control.cues", "Cues");
            translations.add("ui.control.cue", "Cue - %s");
            translations.add("ui.control.fadeIn", "Fade in");
            translations.add("ui.control.fadeOut", "Fade out");
            translations.add("ui.control.fader", "DMX channel %s: %s");
            translations.add("ui.control.grandMaster", "Grand master: %s");
            translations.add("commands.network.notfound", "Network not found.");
            translations.add("commands.networks", "There are %s network(s): %s.");
            translations.add("commands.network.members", "There are %s network member(s): %s.");
            translations.add("commands.network.members.add.success", "Added %s to the network.");
            translations.add("commands.network.members.add.failed", "Player already member of network.");
            translations.add("commands.network.members.remove.success", "Removed %s from the network.");
            translations.add("commands.network", "%s (%s) has %s member(s)");
            translations.add("commands.network.invalid", "Unknown network mode: %s");
            translations.add("commands.network.role.invalid", "Unknown member role: %s");
            translations.add("commands.network.created", "Network created");
            translations.add("commands.network.deleted", "Network deleted");
            translations.add("commands.network.updated", "Network updated");
            translations.add("screen.configurationcard.autoincrement", "Address Auto Increment");
            translations.add("screen.configurationcard", "Configuration Card");
            translations.add("screen.artnetconfig.network", "Network");
            translations.add("screen.artnetconfig.entry", "Subnet: %s Universe: %s");
            translations.add("item.configurationcard.success", "Configured device to %s network, universe %s and address %s - next address is %s.");
            translations.add("screen.artnetconfig.entry.subnet", "Subnet: %s");
            translations.add("screen.artnetconfig.entry.universe", "Universe: %s");
            translations.add("screen.artnetconfig.subnet", "Art-Net Subnet");
            translations.add("screen.artnetconfig.universe", "Art-Net Universe");
            translations.add("screen.artnetconfig.networkUniverse", "Network Universe");
            translations.add("screen.artnetconfig.networkEnabled", "Enabled");
            translations.add("item.configurationcard.description.1", "Shift + Right Click for settings");
            translations.add("item.configurationcard.description.2", "Right click on fixture to apply");
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, path);
    }
}

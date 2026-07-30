package dev.imabad.theatrical.items;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blocks.Blocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class Items {
    public static final DeferredRegister<Item> ITEMS = TheatricalRegistry.get(Registries.ITEM);

    // Blocks
    public static final RegistrySupplier<Item> MOVING_LIGHT = ITEMS.register(
        "moving_light",
        () -> new BlockItem(Blocks.MOVING_LIGHT_BLOCK.get(), properties("moving_light").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> PIPE = ITEMS.register(
        "pipe",
        () -> new BlockItem(Blocks.PIPE_BLOCK.get(), properties("pipe").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> ART_NET_INTERFACE = ITEMS.register(
        "artnet_interface",
        () -> new BlockItem(Blocks.ART_NET_INTERFACE.get(), properties("artnet_interface").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> LED_FRESNEL = ITEMS.register(
            "led_fresnel",
            () -> new BlockItem(Blocks.LED_FRESNEL.get(), properties("led_fresnel").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> TRUSS = ITEMS.register(
            "truss",
            () -> new BlockItem(Blocks.TRUSS_BLOCK.get(), properties("truss").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> REDSTONE_INTERFACE = ITEMS.register(
            "redstone_interface",
            () -> new BlockItem(Blocks.REDSTONE_INTERFACE.get(), properties("redstone_interface").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> TANK_TRAP = ITEMS.register(
            "tank_trap",
            () -> new BlockItem(Blocks.TANK_TRAP.get(), properties("tank_trap").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> LED_PANEL = ITEMS.register(
            "led_panel",
            () -> new BlockItem(Blocks.LED_PANEL.get(), properties("led_panel").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> BASIC_LIGHTING_DESK = ITEMS.register(
            "basic_lighting_desk",
            () -> new BlockItem(Blocks.BASIC_LIGHTING_DESK.get(), properties("basic_lighting_desk").useBlockDescriptionPrefix())
    );
    public static final RegistrySupplier<Item> MOVING_WASH = ITEMS.register(
        "moving_wash",
        () -> new BlockItem(Blocks.MOVING_WASH_BLOCK.get(), properties("moving_wash").useBlockDescriptionPrefix())
    );

    // Items
    public static final RegistrySupplier<Item> CONFIGURATION_CARD = ITEMS.register(
            "configuration_card",
            () -> new ConfigurationCard(properties("configuration_card"))
    );
    public static final RegistrySupplier<Item> FIXTURE_FOCUSER = ITEMS.register(
            "fixture_focuser",
            () -> new FixtureFocuser(properties("fixture_focuser"))
    );

    private static Item.Properties properties(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, name)));
    }
}

package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.client.gui.widgets.BetterCheckbox;
import dev.imabad.theatrical.client.gui.widgets.BetterStringWidget;
import dev.imabad.theatrical.client.gui.widgets.LabeledEditBox;
import dev.imabad.theatrical.net.ConfigureConfigurationCard;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationCardScreen extends Screen {
    private final Identifier GUI = Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, "textures/gui/blank.png");
    protected final int imageWidth;
    protected final int imageHeight;
    protected int xCenter;
    protected int yCenter;
    protected GridLayout layout;
    private LabeledEditBox dmxAddress, dmxUniverse;
    private Checkbox autoIncrement;
    private BetterCheckbox enableUniverse, enableAddress;
    private UUID networkId = UUIDUtil.NULL;
    private final CompoundTag itemData;
    public ConfigurationCardScreen(CompoundTag itemData) {
        super(Component.translatable("screen.configurationcard"));
        this.imageWidth = 176;
        this.imageHeight = 126;
        this.itemData = itemData;
        this.networkId = itemData.read("network", net.minecraft.core.UUIDUtil.CODEC).orElse(UUIDUtil.NULL);
    }
    @Override
    protected void init() {
        super.init();
        layout = new GridLayout();
        layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
        layout.addChild(new BetterStringWidget(Component.translatable("screen.configurationcard"), this.font).setColor(4210752).setShadow(false), 1, 1, 1, 4);
        this.dmxUniverse = new LabeledEditBox(this.font, xCenter, yCenter, 50, 10, Component.translatable("artneti.dmxUniverse"));
        this.dmxUniverse.setValue(Integer.toString(itemData.getIntOr("dmxUniverse", 0)));
        layout.addChild(dmxUniverse, 2, 1, 1, 4,  LayoutSettings.defaults().alignHorizontallyCenter().alignVerticallyMiddle().padding(10));
        enableUniverse = new BetterCheckbox(xCenter, yCenter, 10, 10, Component.translatable("artneti.dmxUniverse.enable"), itemData.getBooleanOr("universeEnabled", false));
        dmxUniverse.active = enableUniverse.selected();
        enableUniverse.setOnChange(aBoolean -> {
            dmxUniverse.active = aBoolean;
        });
        layout.addChild(enableUniverse, 2, 2, 1, 1,  LayoutSettings.defaults().alignHorizontallyLeft().alignVerticallyMiddle());
        layout.addChild(CycleButton.builder((UUID networkId) ->
        {
            if (TheatricalClient.getArtNetManager().getKnownNetworks().containsKey(networkId)) {
                return Component.literal(TheatricalClient.getArtNetManager().getKnownNetworks().get(networkId));
            }
            return Component.literal("Unknown");
        }, networkId).withValues(CycleButton.ValueListSupplier.create(Stream.concat(Stream.of(UUIDUtil.NULL),
                        TheatricalClient.getArtNetManager().getKnownNetworks().keySet().stream()).collect(Collectors.toList())))
                .displayOnlyValue()
                .create(xCenter, yCenter, 150, 20,
                        Component.translatable("screen.artnetconfig.network"), (obj, val) -> {
                            this.networkId = val;
                        }), 3, 1, 1, 4);

        this.dmxAddress = new LabeledEditBox(this.font, xCenter, yCenter, 50, 10, Component.translatable("fixture.dmxStart"));
        this.dmxAddress.setValue(Integer.toString(itemData.getIntOr("dmxAddress", 0)));
        layout.addChild(dmxAddress, 4, 1, 1, 4, LayoutSettings.defaults().alignHorizontallyCenter().alignVerticallyMiddle().padding(10));

        enableAddress = new BetterCheckbox(xCenter, yCenter, 10, 10, Component.translatable("artneti.dmxAddress.enable"), itemData.getBooleanOr("addressEnabled", false));
        dmxAddress.active = enableAddress.selected();
        enableAddress.setOnChange(aBoolean -> {
            dmxAddress.active = aBoolean;
        });
        layout.addChild(enableAddress, 4, 2, 1, 1,  LayoutSettings.defaults().alignHorizontallyLeft().alignVerticallyMiddle());
        this.autoIncrement = Checkbox.builder(Component.translatable("screen.configurationcard.autoincrement"), this.font)
                .pos(xCenter, yCenter)
                .maxWidth(150)
                .selected(itemData.getBooleanOr("autoIncrement", false))
                .build();

        layout.addChild(autoIncrement, 5, 1, 1, 4);
        layout.addChild(
                new Button.Builder(Component.translatable("artneti.save"), button -> this.update())
                        .pos(xCenter, yCenter)
                        .size(100, 20)
                        .build(),
                6, 1, 1, 4
        );
        refreshLayout();
        this.repositionElements();

    }
    protected void refreshLayout(){
        if(layout == null)
            return;
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    protected void repositionElements() {
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    protected void update(){
        try {
            int dmx = Integer.parseInt(this.dmxAddress.getValue());
            if (dmx > 512 || dmx < 0) {
                return;
            }
            int universe = Integer.parseInt(this.dmxUniverse.getValue());
            if (universe < 0) {
                return;
            }
            new ConfigureConfigurationCard(networkId, dmx, universe, autoIncrement.selected(), enableUniverse.selected(), enableAddress.selected()).sendToServer();
            Minecraft.getInstance().gui.setScreen(null);
        } catch(NumberFormatException ignored) {
            //We need a nicer way to show that this is invalid?
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        this.extractWindow(graphics);
    }

    private void extractWindow(GuiGraphicsExtractor graphics){
        int layoutHeight = 0;
        if(layout != null) {
            layoutHeight = layout.getHeight();
        }
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - layoutHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, imageWidth, layoutHeight, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

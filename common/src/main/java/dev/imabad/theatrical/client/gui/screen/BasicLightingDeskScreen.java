package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.client.gui.widgets.FaderWidget;
import dev.imabad.theatrical.net.*;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicLightingDeskScreen extends Screen {

    private final Identifier GUI = Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, "textures/gui/lighting_console.png");

    private final int imageWidth;
    private final int imageHeight;
    private int xCenter;
    private int yCenter;
    private final BasicLightingDeskBlockEntity be;
    private EditBox fadeInTime, fadeOutTime;
    private UUID networkId;
    public BasicLightingDeskScreen(BasicLightingDeskBlockEntity blockEntity) {
        super(Component.translatable("screen.basicLightingDesk"));
        this.imageWidth = 244;
        this.imageHeight = 126;
        this.be = blockEntity;
        this.networkId = be.getNetworkId();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        this.extractWindow(graphics);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.extractLabels(graphics);
    }

    private void extractWindow(GuiGraphicsExtractor graphics) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    private void extractLabels(GuiGraphicsExtractor graphics) {
        extractLabel(graphics, "ui.control.step", 20, 57, be.getCurrentStep());
        extractLabel(graphics, be.isRunMode() ? "ui.control.modes.run" : "ui.control.modes.program", 41, 90);
        extractLabel(graphics, "ui.control.cues", 100, 5);
        for(int key : be.getStoredSteps().keySet()){
            extractLabel(graphics, "ui.control.cue", 101, 15 + (10 * key), key);
        }
        extractLabel(graphics, "ui.control.fadeIn", 35, 10);
        extractLabel(graphics, "ui.control.fadeOut", 35, 33);
    }

    private void extractLabel(GuiGraphicsExtractor graphics, String translationKey, int offSetX, int offSetY, Object... replacements) {
        MutableComponent translatable = Component.translatable(translationKey, replacements);
        graphics.text(font, translatable, (xCenter + (this.imageWidth / 2) - (this.font.width(translatable.getString()) / 2)) + offSetX, yCenter + offSetY, 0xFF404040, false);
    }

    @Override
    protected void init() {
        super.init();
        xCenter = (this.width - this.imageWidth) / 2;
        yCenter = (this.height - this.imageHeight) / 2;
        byte[] faders = be.getFaders();
        for(int i = 0; i < faders.length; i++){
            int baseY = yCenter + 7;
            if(i >= 6){
                baseY += (i / 6) * 61;
            }
            int faderNumber = i - ((i / 6) * 6);
            this.addRenderableWidget(new FaderWidget(xCenter + 7 + (faderNumber * 20), baseY, i, Byte.toUnsignedInt(faders[i])));
        }
        this.addRenderableWidget(new FaderWidget(xCenter + 184, yCenter + 7, -1, Byte.toUnsignedInt(be.getGrandMaster())));
        this.addRenderableWidget(new Button.Builder(Component.literal("<-"), button -> this.moveStep(false))
                .pos(xCenter + 155, yCenter + 67)
                .size(15, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.literal("->"), button -> this.moveStep(true))
                .pos(xCenter + 170, yCenter + 67)
                .size(15, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.literal("Go"), button -> this.go())
                .pos(xCenter + 130, yCenter + 100)
                .size(20, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.literal("Mode"), button -> this.mode())
                .pos(xCenter + 155, yCenter + 100)
                .size(30, 20)
                .build());
        this.fadeInTime = new EditBox(this.font, xCenter + 147, yCenter + 20, 20, 10, Component.literal("0"));
        this.fadeOutTime = new EditBox(this.font, xCenter + 147, yCenter + 43, 20, 10, Component.literal("0"));
        this.fadeInTime.setValue(Integer.toString(be.getFadeInTicks()));
        this.fadeOutTime.setValue(Integer.toString(be.getFadeOutTicks()));
        this.addRenderableWidget(fadeInTime);
        this.addRenderableWidget(fadeOutTime);
        this.addRenderableWidget(CycleButton.builder((UUID networkId) ->
        {
            if (TheatricalClient.getArtNetManager().getKnownNetworks().containsKey(networkId)) {
                return Component.literal(TheatricalClient.getArtNetManager().getKnownNetworks().get(networkId));
            }
            return Component.literal("Unknown");
        }, networkId).withValues(CycleButton.ValueListSupplier.create(Stream.concat(Stream.of(UUIDUtil.NULL),
                        TheatricalClient.getArtNetManager().getKnownNetworks().keySet().stream()).collect(Collectors.toList())))
                .displayOnlyValue()
                .create(xCenter + 45, yCenter + 130, 150, 20,
                        Component.translatable("screen.artnetconfig.network"), (obj, val) -> {
                            this.networkId = val;
                            new UpdateNetworkId(be.getBlockPos(), networkId).sendToServer();
                        }));
    }

    private void moveStep(boolean forward){
        new ControlMoveStep(be.getBlockPos(), forward).sendToServer();
    }

    private void go(){
        new ControlGo(be.getBlockPos(), Integer.parseInt(fadeInTime.getValue()), Integer.parseInt(fadeOutTime.getValue())).sendToServer();
    }

    private void mode(){
        new ControlModeToggle(be.getBlockPos()).sendToServer();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        this.children().forEach(widget -> {
            if(widget instanceof FaderWidget fader) {
                if (fader.isMouseOver(event.x(), event.y()) && fader.isDragging()) {
                    int newVal = fader.updateValue(event.y());
                    new ControlUpdateFader(be.getBlockPos(), fader.getChannel(), newVal).sendToServer();
                }
            }
        });
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package dev.imabad.theatrical.client.gui.widgets;

import dev.imabad.theatrical.Theatrical;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class FaderWidget extends AbstractWidget {
    private static final Identifier background = Identifier.fromNamespaceAndPath(Theatrical.MOD_ID, "textures/gui/lighting_console.png");

    private final int channel;
    private int value;

    private boolean dragging = false;

    public FaderWidget(int x, int y, int channel, int value) {
        super(x, y, 10, 51, narration(channel, value));
        this.channel = channel;
        this.value = value;
    }

    private static Component narration(int channel, int value) {
        return channel < 0
                ? Component.translatable("ui.control.grandMaster", value)
                : Component.translatable("ui.control.fader", channel + 1, value);
    }

    public int getChannel() {
        return channel;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
        graphics.blit(RenderPipelines.GUI_TEXTURED, background, getX(), getY(), 0, 126, getWidth(), getHeight(), 10, 51, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, background, getX() + 1, (getY() + (height - 7)) - (int) ((this.value / 255f) * 50), 10, 126, 8, 11, 8, 11, 256, 256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.value = calculateNewValue(event.y());
        this.setMessage(narration(channel, value));
        this.dragging = true;
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        this.dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }
    public int calculateNewValue(double mouseY){
        return Mth.clamp((int) (((this.height - (mouseY - this.getY())) / this.height) * 255f), 0, 255);
    }

    public int updateValue(double mouseY){
        this.value = calculateNewValue(mouseY);
        this.setMessage(narration(channel, value));
        return value;
    }
}

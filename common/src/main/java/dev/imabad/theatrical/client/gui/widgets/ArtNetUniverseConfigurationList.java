package dev.imabad.theatrical.client.gui.widgets;

import dev.imabad.theatrical.client.gui.screen.ArtNetConfigurationScreen;
import dev.imabad.theatrical.config.UniverseConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class ArtNetUniverseConfigurationList extends ObjectSelectionList<ArtNetUniverseConfigurationList.Entry> implements LayoutElement {

    private final ArtNetConfigurationScreen parent;
    public ArtNetUniverseConfigurationList(Minecraft minecraft, ArtNetConfigurationScreen screen, int width, int height, Component title) {
        super(minecraft, width, height, 0, 30);
        this.parent = screen;
    }

    public void setEntries(Map<Integer, UniverseConfig> configs){
        this.clearEntries();
        configs.forEach((key, value) -> addEntry(new Entry(parent, key, value)));
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + this.getRowWidth() + 6;
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 10;
    }

    @Override
    public void setX(int x) {
        int offset = x - getX();
        super.setX(x);
        children().forEach(entry -> entry.setX(entry.getX() + offset));
    }

    @Override
    public void setY(int y) {
        int offset = y - getY();
        super.setY(y);
        children().forEach(entry -> entry.setY(entry.getY() + offset));
    }

    @Environment(EnvType.CLIENT)
    public static class Entry extends ObjectSelectionList.Entry<Entry> {

        private final ArtNetConfigurationScreen parent;
        private final UniverseConfig config;
        private final int networkUniverse;
        public Entry(ArtNetConfigurationScreen parent, int networkUniverse, UniverseConfig config) {
            this.parent = parent;
            this.config = config;
            this.networkUniverse = networkUniverse;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("screen.artnetconfig.entry.universe", networkUniverse);
        }

        public UniverseConfig getConfig() {
            return config;
        }

        public int getNetworkUniverse() {
            return networkUniverse;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.text(font, Component.translatable("screen.artnetconfig.entry.universe", networkUniverse), getContentX(), getContentY() + 1, 0xFFFFFFFF);
//            guiGraphics.drawString(font, Component.translatable("screen.artnetconfig.entry.subnet", config.getSubnet()),  left, top + 1, 16777215 );
//            guiGraphics.drawString(font, Component.translatable("screen.artnetconfig.entry.universe", config.getUniverse()),  left, top + 4 + font.lineHeight, 16777215 );
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            this.parent.setSelected(this);
            return super.mouseClicked(event, doubleClick);
        }
    }
}

package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.client.gui.screen.ConfigurationCardScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class ConfigurationCard extends Item {
    public ConfigurationCard(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if(player.isCrouching() && level.isClientSide()){
            CompoundTag cardData = player.getItemInHand(usedHand)
                    .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag();
            openUI(cardData);
            return InteractionResult.PASS;
        }
        return super.use(level, player, usedHand);
    }

    @Environment(EnvType.CLIENT)
    private static void openUI(CompoundTag data){
        Minecraft.getInstance().setScreenAndShow(new ConfigurationCardScreen(data));
    }
}

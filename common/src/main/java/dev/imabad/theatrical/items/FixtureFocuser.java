package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.FocusableFixture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FixtureFocuser extends Item {
    public FixtureFocuser(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if(!level.isClientSide()){
            if(player.isShiftKeyDown()){
                ItemStack itemInHand = player.getItemInHand(usedHand);
                CompoundTag cardData = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                if(cardData.contains("Light")){
                    cardData.read("Light", BlockPos.CODEC).ifPresent(lightPos -> {
                        BlockEntity blockEntity = level.getBlockEntity(lightPos);
                        if(blockEntity instanceof FocusableFixture focusableFixture){
                            focusableFixture.setTrackingEntity(null);
                            cardData.remove("Light");
                            CustomData.set(DataComponents.CUSTOM_DATA, itemInHand, cardData);
                        }
                    });
                }
            }
        }
        return super.use(level, player, usedHand);
    }
}

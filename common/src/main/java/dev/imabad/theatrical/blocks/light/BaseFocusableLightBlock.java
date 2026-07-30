package dev.imabad.theatrical.blocks.light;

import dev.imabad.theatrical.api.FocusableFixture;
import dev.imabad.theatrical.items.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseFocusableLightBlock extends BaseLightBlock {
    protected BaseFocusableLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemInHand, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult superResult = super.useItemOn(itemInHand, state, level, pos, player, hand, hit);
        if(superResult == InteractionResult.TRY_WITH_EMPTY_HAND) {
            if (!level.isClientSide()) {
                if(itemInHand.is(Items.FIXTURE_FOCUSER.get())){
                    CompoundTag itemTag = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    if(level.getBlockEntity(pos) instanceof FocusableFixture focusableFixture) {
                        if (!itemTag.contains("Light") && focusableFixture.getTrackingEntity() == null) {
                            itemTag.store("Light", BlockPos.CODEC, pos);
                            focusableFixture.setTrackingEntity(player);
                        } else if(focusableFixture.getTrackingEntity() != null) {
                            focusableFixture.setTrackingEntity(null);
                        }
                        CustomData.set(DataComponents.CUSTOM_DATA, itemInHand, itemTag);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return superResult;
    }
}

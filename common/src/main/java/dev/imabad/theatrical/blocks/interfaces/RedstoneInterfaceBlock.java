package dev.imabad.theatrical.blocks.interfaces;

import dev.imabad.theatrical.TheatricalScreen;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.OpenScreen;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RedstoneInterfaceBlock  extends Block implements EntityBlock {
    public RedstoneInterfaceBlock(Properties properties) {
        super(properties
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneInterfaceBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemInHand, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if(!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RedstoneInterfaceBlockEntity redstoneInterfaceBlockEntity) {
                if (!redstoneInterfaceBlockEntity.getNetworkId().equals(UUIDUtil.NULL)) {
                    TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(redstoneInterfaceBlockEntity.getNetworkId());
                    if (network != null && !network.members().isMember(player.getUUID())) {
                        return InteractionResult.FAIL;
                    }
                }
                if (itemInHand.is(Items.CONFIGURATION_CARD.get())) {
                    CompoundTag tagData = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    redstoneInterfaceBlockEntity.setNetworkId(tagData.read("network", net.minecraft.core.UUIDUtil.CODEC)
                            .orElse(UUIDUtil.NULL));
                    if (tagData.getBooleanOr("universeEnabled", false)) {
                        redstoneInterfaceBlockEntity.setUniverse(tagData.getIntOr("dmxUniverse", 0));
                    }
                    if (tagData.getBooleanOr("addressEnabled", false)) {
                        redstoneInterfaceBlockEntity.setChannelStartPoint(tagData.getIntOr("dmxAddress", 0));
                    }
                    if (tagData.getBooleanOr("autoIncrement", false)) {
                        tagData.putInt("dmxAddress", tagData.getIntOr("dmxAddress", 0) + redstoneInterfaceBlockEntity.getChannelCount());
                        CustomData.set(DataComponents.CUSTOM_DATA, itemInHand, tagData);
                    }
                    TheatricalNetworkData instance = TheatricalNetworkData.getInstance(level.getServer().overworld());
                    player.sendSystemMessage(Component.translatable("item.configurationcard.success", instance.getNetwork(redstoneInterfaceBlockEntity.getNetworkId()).name(), Integer.toString(redstoneInterfaceBlockEntity.getUniverse()), Integer.toString(redstoneInterfaceBlockEntity.getChannelStart()), Integer.toString(tagData.getIntOr("dmxAddress", 0))));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            new OpenScreen(pos, TheatricalScreen.GENERIC_DMX).sendTo((ServerPlayer) player);
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if(level.getBlockEntity(pos) instanceof RedstoneInterfaceBlockEntity be){
            return be.getRedstoneOutput();
        }
        return super.getSignal(state, level, pos, direction);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if(level.getBlockEntity(pos) instanceof RedstoneInterfaceBlockEntity be){
            return be.getRedstoneOutput();
        }
        return super.getDirectSignal(state, level, pos, direction);
    }
}

package dev.imabad.theatrical.blocks.interfaces;

import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.client.gui.screen.ArtNetConfigurationScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class ArtNetInterfaceBlock extends Block {
    public ArtNetInterfaceBlock(Properties properties) {
        super(properties
            .requiresCorrectToolForDrops()
            .strength(3, 3)
            .noOcclusion()
            .isValidSpawn(Blocks::neverAllowSpawn)
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL));
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if(level.isClientSide()){
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreenAndShow(new ArtNetConfigurationScreen(minecraft.gui.screen()));
        }
        return InteractionResult.SUCCESS;
    }
}

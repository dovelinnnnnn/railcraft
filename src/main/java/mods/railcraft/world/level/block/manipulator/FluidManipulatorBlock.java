package mods.railcraft.world.level.block.manipulator;

import mods.railcraft.util.LevelUtil;
import mods.railcraft.world.level.block.entity.manipulator.FluidManipulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class FluidManipulatorBlock<T extends FluidManipulatorBlockEntity>
    extends ManipulatorBlock<T> {

  protected FluidManipulatorBlock(Class<T> blockEntityType, Properties properties) {
    super(blockEntityType, properties);
    this.registerDefaultState(this.stateDefinition.any()
        .setValue(POWERED, false));
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level,
      BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
    return LevelUtil.getBlockEntity(level, blockPos, FluidManipulatorBlockEntity.class)
        .filter(blockEntity -> blockEntity.use(player, hand))
        .map(__ -> ItemInteractionResult.SUCCESS)
        .orElseGet(() -> super.useItemOn(itemStack, blockState, level, blockPos,
            player, hand, rayTraceResult));
  }
}

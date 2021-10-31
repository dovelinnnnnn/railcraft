package mods.railcraft.world.level.block;

import mods.railcraft.advancements.criterion.RailcraftCriteriaTriggers;
import mods.railcraft.world.level.block.entity.multiblock.CokeOvenBlockEntity;
import mods.railcraft.world.level.block.entity.multiblock.MultiblockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CokeOvenBricksBlock extends MultiblockBlock {
  public static final BooleanProperty LIT = BlockStateProperties.LIT;

  public CokeOvenBricksBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected BlockState addDefaultBlockState(BlockState defaultBlockState) {
    defaultBlockState = super.addDefaultBlockState(defaultBlockState);
    defaultBlockState.setValue(LIT, Boolean.valueOf(false));
    return defaultBlockState;
  }

  @Override
  protected void createBlockStateDefinition(
        StateContainer.Builder<Block, BlockState> stateContainer) {
    super.createBlockStateDefinition(stateContainer);
    stateContainer.add(LIT);
  }

  @Override
  public TileEntity createTileEntity(BlockState blockState, IBlockReader level) {
    return new CokeOvenBlockEntity();
  }

  @Override
  public boolean hasTileEntity(BlockState blockState) {
    return true;
  }

  @Override
  public ActionResultType use(BlockState blockState, World level,
      BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
    TileEntity blockEntity = level.getBlockEntity(pos);
    if (level.isClientSide()) {
      return ActionResultType.sidedSuccess(level.isClientSide());
    }

    if (!(blockEntity instanceof CokeOvenBlockEntity)) {
      return ActionResultType.PASS;
    }
    CokeOvenBlockEntity recast = (CokeOvenBlockEntity) blockEntity;

    if (recast.getParent() == null) {
      boolean ttmpResult = recast.tryToMakeParent(rayTraceResult.getDirection());

      if (!recast.isFormed() && !ttmpResult) { // it failed and it's not assembled.
        return ActionResultType.PASS;
      }

      if (ttmpResult) {
        RailcraftCriteriaTriggers.MULTIBLOCK_FORM.trigger((ServerPlayerEntity)player, recast);
      }
      return ActionResultType.PASS;
    }
    player.openMenu(recast);
    return ActionResultType.sidedSuccess(level.isClientSide());
  }
}

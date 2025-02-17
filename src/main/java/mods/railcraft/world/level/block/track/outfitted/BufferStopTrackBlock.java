package mods.railcraft.world.level.block.track.outfitted;

import java.util.List;
import java.util.function.Supplier;
import mods.railcraft.Translations;
import mods.railcraft.api.track.TrackType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BufferStopTrackBlock extends ReversibleOutfittedTrackBlock {

  private static final VoxelShape BUFFER_STOP_SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
  private static final VoxelShape SHAPE = Shapes.or(FLAT_AABB, BUFFER_STOP_SHAPE);

  public BufferStopTrackBlock(Supplier<? extends TrackType> trackType, Properties properties) {
    super(trackType, properties);
  }

  @Override
  public VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos,
      CollisionContext context) {
    return SHAPE;
  }

  @Override
  public VoxelShape getCollisionShape(BlockState blockState, BlockGetter level,
      BlockPos pos, CollisionContext context) {
    return BUFFER_STOP_SHAPE;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines,
      TooltipFlag flag) {
    lines.add(Component.translatable(Translations.Tips.BUFFER_STOP_TRACK)
        .withStyle(ChatFormatting.GRAY));
    lines.add(Component.translatable(Translations.Tips.HIT_CROWBAR_TO_CHANGE_DIRECTION)
        .withStyle(ChatFormatting.BLUE));
  }
}

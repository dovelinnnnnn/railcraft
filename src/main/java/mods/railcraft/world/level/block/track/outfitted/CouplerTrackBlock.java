package mods.railcraft.world.level.block.track.outfitted;

import java.util.List;
import java.util.function.Supplier;
import mods.railcraft.Translations;
import mods.railcraft.api.track.TrackType;
import mods.railcraft.world.level.block.entity.RailcraftBlockEntityTypes;
import mods.railcraft.world.level.block.entity.track.CouplerTrackBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class CouplerTrackBlock extends PoweredOutfittedTrackBlock implements EntityBlock {

  public static final EnumProperty<CouplerTrackBlockEntity.Mode> MODE =
      EnumProperty.create("mode", CouplerTrackBlockEntity.Mode.class);

  public CouplerTrackBlock(Supplier<? extends TrackType> trackType, Properties properties) {
    super(trackType, properties);
  }

  @Override
  protected BlockState buildDefaultState(BlockState blockState) {
    return super.buildDefaultState(blockState)
        .setValue(MODE, CouplerTrackBlockEntity.Mode.COUPLER);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(MODE);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
    return new CouplerTrackBlockEntity(blockPos, blockState);
  }

  @Override
  protected boolean crowbarWhack(BlockState blockState, Level level, BlockPos blockPos,
      Player player, InteractionHand hand, ItemStack itemStack) {
    var mode = CouplerTrackBlock.getMode(blockState);
    var newMode = player.isCrouching() ? mode.previous() : mode.next();
    var res = level.setBlockAndUpdate(blockPos, blockState.setValue(MODE, newMode));
    var currentMode = Component.translatable(Translations.Tips.CURRENT_MODE);
    var modeDisplay = newMode.getDisplayName().copy().withStyle(ChatFormatting.DARK_PURPLE);
    player.displayClientMessage(
        currentMode.append(CommonComponents.SPACE).append(modeDisplay), true);
    return res;
  }

  @Override
  public void onMinecartPass(BlockState blockState, Level level, BlockPos pos,
      AbstractMinecart cart) {
    super.onMinecartPass(blockState, level, pos, cart);
    level.getBlockEntity(pos, RailcraftBlockEntityTypes.COUPLER_TRACK.get())
        .ifPresent(couplerTrack -> getMode(blockState).minecartPassed(couplerTrack, cart));
  }

  @Override
  public int getPowerPropagation(BlockState blockState, Level level, BlockPos pos) {
    return getMode(blockState).getPowerPropagation();
  }

  public static CouplerTrackBlockEntity.Mode getMode(BlockState blockState) {
    return blockState.getValue(MODE);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines,
      TooltipFlag flag) {
    lines.add(Component.translatable(Translations.Tips.COUPLER_TRACK)
        .withStyle(ChatFormatting.GRAY));
    lines.add(Component.translatable(Translations.Tips.HIT_CROWBAR_TO_CHANGE_MODE)
        .withStyle(ChatFormatting.BLUE));
    lines.add(Component.translatable(Translations.Tips.APPLY_REDSTONE_TO_ENABLE)
        .withStyle(ChatFormatting.RED));
  }
}

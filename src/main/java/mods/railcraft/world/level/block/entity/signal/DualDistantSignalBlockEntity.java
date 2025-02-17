package mods.railcraft.world.level.block.entity.signal;

import mods.railcraft.api.core.CompoundTagKeys;
import mods.railcraft.api.signal.DualSignalReceiver;
import mods.railcraft.api.signal.SignalAspect;
import mods.railcraft.api.signal.SignalReceiver;
import mods.railcraft.api.signal.entity.SignalReceiverEntity;
import mods.railcraft.world.level.block.entity.RailcraftBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public class DualDistantSignalBlockEntity extends AbstractSignalBlockEntity
    implements SignalReceiverEntity, DualSignalBlockEntity {

  private final DualSignalReceiver signalReceiver = new DualSignalReceiver(this,
      this::syncToClient, __ -> this.refreshLight(), __ -> this.refreshLight());

  private void refreshLight() {
    this.level.getLightEngine().checkBlock(this.getBlockPos());
  }

  public DualDistantSignalBlockEntity(BlockPos blockPos, BlockState blockState) {
    super(RailcraftBlockEntityTypes.DUAL_DISTANT_SIGNAL.get(), blockPos, blockState);
  }

  @Override
  public void onLoad() {
    if (!this.level.isClientSide()) {
      this.signalReceiver.refresh();
    }
  }

  public void blockRemoved() {
    this.signalReceiver.destroy();
  }

  @Override
  public int getLightValue() {
    return Math.max(super.getLightValue(),
        this.getSecondarySignalAspect().getBlockLight());
  }

  @Override
  public SignalAspect getPrimarySignalAspect() {
    return this.signalReceiver.getPrimarySignalAspect();
  }

  @Override
  public SignalAspect getSecondarySignalAspect() {
    return this.signalReceiver.getSecondarySignalAspect();
  }

  @Override
  public SignalReceiver getSignalReceiver() {
    return this.signalReceiver;
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
    super.saveAdditional(tag, provider);
    tag.put(CompoundTagKeys.SIGNAL_RECEIVER, this.signalReceiver.serializeNBT(provider));
  }

  @Override
  public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
    super.loadAdditional(tag, provider);
    this.signalReceiver.deserializeNBT(provider, tag.getCompound(CompoundTagKeys.SIGNAL_RECEIVER));
  }

  @Override
  public void writeToBuf(RegistryFriendlyByteBuf data) {
    super.writeToBuf(data);
    this.signalReceiver.writeToBuf(data);
  }

  @Override
  public void readFromBuf(RegistryFriendlyByteBuf data) {
    super.readFromBuf(data);
    this.signalReceiver.readFromBuf(data);
  }
}

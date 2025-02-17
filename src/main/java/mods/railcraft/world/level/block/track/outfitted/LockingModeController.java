package mods.railcraft.world.level.block.track.outfitted;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface LockingModeController extends INBTSerializable<CompoundTag> {

  default void locked(AbstractMinecart cart) {}

  default void passed(AbstractMinecart cart) {}

  default void released(AbstractMinecart cart) {}

  default CompoundTag serializeNBT(HolderLookup.Provider provider) {
    return new CompoundTag();
  }

  default void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {}
}

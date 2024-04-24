package mods.railcraft.world.level.block.entity;

import org.jetbrains.annotations.Nullable;
import com.mojang.authlib.GameProfile;
import mods.railcraft.api.core.CompoundTagKeys;
import mods.railcraft.api.core.Lockable;
import mods.railcraft.api.util.EnumUtil;
import mods.railcraft.client.gui.widget.button.ButtonTexture;
import mods.railcraft.client.gui.widget.button.TexturePosition;
import mods.railcraft.gui.button.ButtonState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LockableSwitchTrackActuatorBlockEntity extends RailcraftBlockEntity
    implements Lockable {

  private Lock lock = Lock.UNLOCKED;

  public LockableSwitchTrackActuatorBlockEntity(BlockEntityType<?> type, BlockPos blockPos,
      BlockState blockState) {
    super(type, blockPos, blockState);
  }

  public Lock getLock() {
    return this.lock;
  }

  public void setLock(@Nullable GameProfile gameProfile) {
    this.lock = gameProfile == null ? Lock.UNLOCKED : Lock.LOCKED;
    this.setOwner(gameProfile);
  }

  @Override
  public boolean isLocked() {
    return this.lock == Lock.LOCKED;
  }

  public boolean canAccess(GameProfile gameProfile) {
    return !this.isLocked() || this.isOwnerOrOperator(gameProfile);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putString(CompoundTagKeys.LOCK, this.lock.getSerializedName());
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    this.lock = Lock.fromName(tag.getString(CompoundTagKeys.LOCK));
  }

  @Override
  public void writeToBuf(RegistryFriendlyByteBuf data) {
    super.writeToBuf(data);
    data.writeEnum(this.lock);
  }

  @Override
  public void readFromBuf(RegistryFriendlyByteBuf data) {
    super.readFromBuf(data);
    this.lock = data.readEnum(Lock.class);
  }

  public enum Lock implements ButtonState<Lock>, StringRepresentable {

    UNLOCKED("unlocked", ButtonTexture.UNLOCKED_BUTTON),
    LOCKED("locked", ButtonTexture.LOCKED_BUTTON);

    private static final StringRepresentable.EnumCodec<Lock> CODEC =
        StringRepresentable.fromEnum(Lock::values);

    private final String name;
    private final TexturePosition texture;

    Lock(String name, TexturePosition texture) {
      this.name = name;
      this.texture = texture;
    }

    @Override
    public Component label() {
      return Component.empty();
    }

    @Override
    public TexturePosition texturePosition() {
      return this.texture;
    }

    @Override
    public Lock next() {
      return EnumUtil.next(this, values());
    }

    @Override
    public String getSerializedName() {
      return this.name;
    }

    public static Lock fromName(String name) {
      return CODEC.byName(name, UNLOCKED);
    }
  }
}

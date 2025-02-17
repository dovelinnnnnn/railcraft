package mods.railcraft.world.module;

import java.util.concurrent.atomic.AtomicReference;
import mods.railcraft.api.charge.Charge;
import mods.railcraft.api.core.CompoundTagKeys;
import mods.railcraft.tags.RailcraftTags;
import mods.railcraft.util.container.AdvancedContainer;
import mods.railcraft.world.item.RailcraftItems;
import mods.railcraft.world.level.block.entity.SteamTurbineBlockEntity;
import mods.railcraft.world.level.material.StandardTank;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class SteamTurbineModule extends ChargeModule<SteamTurbineBlockEntity> {

  private static final int ROTOR_DAMAGE_CHANCE = 200;

  public static final int CHARGE_OUTPUT = 225;
  private static final int STEAM_USAGE = 360;
  public static final int TANK_STEAM = 0;
  public static final int TANK_WATER = 1;

  private final StandardTank steamTank;
  private final StandardTank waterTank;

  private final AdvancedContainer rotorContainer = new AdvancedContainer(1);
  private float operatingRatio;
  private int energy;

  private final IFluidHandler fluidHandler = new FluidHandler();

  public SteamTurbineModule(SteamTurbineBlockEntity provider, Charge network) {
    super(provider, network);
    this.rotorContainer.listener(this.provider);
    this.steamTank = StandardTank.ofBuckets(4)
        .filter(RailcraftTags.Fluids.STEAM)
        .disableDrain()
        .changeCallback(provider::setChanged);
    this.waterTank = StandardTank.ofBuckets(4)
        .filter(FluidTags.WATER)
        .disableFill()
        .changeCallback(provider::setChanged);
  }

  public IFluidHandler getFluidHandler() {
    return this.fluidHandler;
  }

  @Override
  public void serverTick() {
    super.serverTick();
    var addedEnergy = false;
    if (this.energy < CHARGE_OUTPUT) {
      var steam = this.steamTank.internalDrain(STEAM_USAGE, IFluidHandler.FluidAction.SIMULATE);
      if (steam.getAmount() >= STEAM_USAGE) {
        var rotorStack = this.rotorContainer.getItem(0);
        if (rotorStack.is(RailcraftItems.TURBINE_ROTOR.get())) {
          addedEnergy = true;
          this.energy += CHARGE_OUTPUT;
          this.steamTank.internalDrain(STEAM_USAGE, IFluidHandler.FluidAction.EXECUTE);
          this.waterTank.internalFill(new FluidStack(Fluids.WATER, 2), IFluidHandler.FluidAction.EXECUTE);
          this.rotorContainer.setItem(0, useRotor((ServerLevel) this.provider.level(), rotorStack));
        }
      }
    }

    var thisTick = addedEnergy ? 1 : 0;
    this.operatingRatio = (thisTick - this.operatingRatio) * 0.05F + this.operatingRatio;

    var chargeStorage = this.storage().get();
    if (!chargeStorage.isFull()) {
      chargeStorage.receiveEnergy(this.energy, false);
      this.energy = 0;
    }
  }

  public float getOperatingRatio() {
    return this.operatingRatio;
  }

  public Container getRotorContainer() {
    return this.rotorContainer;
  }

  public boolean needsMaintenance() {
    var rotorStack = this.rotorContainer.getItem(0);
    return rotorStack.isEmpty()
        || !rotorStack.is(RailcraftItems.TURBINE_ROTOR.get())
        || rotorStack.getDamageValue() / (float) rotorStack.getMaxDamage() > 0.75F;
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    var tag = super.serializeNBT(provider);
    tag.put(CompoundTagKeys.STEAM_TANK, this.steamTank.writeToNBT(provider, new CompoundTag()));
    tag.put(CompoundTagKeys.WATER_TANK, this.waterTank.writeToNBT(provider, new CompoundTag()));
    tag.put(CompoundTagKeys.ROTOR_CONTAINER, this.rotorContainer.createTag(provider));
    tag.putInt(CompoundTagKeys.ENERGY, this.energy);
    tag.putFloat(CompoundTagKeys.OPERATING_RATIO, this.operatingRatio);
    return tag;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
    super.deserializeNBT(provider, tag);
    this.steamTank.readFromNBT(provider, tag.getCompound(CompoundTagKeys.STEAM_TANK));
    this.waterTank.readFromNBT(provider, tag.getCompound(CompoundTagKeys.WATER_TANK));
    this.rotorContainer
        .fromTag(tag.getList(CompoundTagKeys.ROTOR_CONTAINER, Tag.TAG_COMPOUND), provider);
    this.energy = tag.getInt(CompoundTagKeys.ENERGY);
    this.operatingRatio = tag.getFloat(CompoundTagKeys.OPERATING_RATIO);
  }

  private static ItemStack useRotor(ServerLevel level, ItemStack stack) {
    var random = level.getRandom();
    if (random.nextInt(ROTOR_DAMAGE_CHANCE) == 0) {
      var result = new AtomicReference<>(stack);
      stack.hurtAndBreak(1, level, null, item -> result.set(ItemStack.EMPTY));
      return result.get();
    } else {
      return stack;
    }
  }

  public class FluidHandler implements IFluidHandler {

    @Override
    public int getTanks() {
      return 2;
    }

    private IFluidTank getTank(int tank) {
      return tank == 0 ? SteamTurbineModule.this.steamTank : SteamTurbineModule.this.waterTank;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
      return this.getTank(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
      return this.getTank(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
      return this.getTank(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
      return SteamTurbineModule.this.steamTank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
      return SteamTurbineModule.this.waterTank.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
      return SteamTurbineModule.this.waterTank.drain(maxDrain, action);
    }
  }
}

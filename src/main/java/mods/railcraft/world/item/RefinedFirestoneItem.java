package mods.railcraft.world.item;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import mods.railcraft.Translations.Tips;
import mods.railcraft.util.container.ContainerTools;
import mods.railcraft.world.entity.FirestoneItemEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.CommonHooks;

public class RefinedFirestoneItem extends FirestoneItem {

  public static final int CHARGES = 5000;

  protected final int heat;
  protected final RandomSource random;

  protected RefinedFirestoneItem(int heat, boolean spawnsFire, Properties properties) {
    super(spawnsFire, properties);
    this.heat = heat;
    this.random = RandomSource.create();
  }

  public RefinedFirestoneItem(boolean spawnsFire, Properties properties) {
    this(250, spawnsFire, properties);
  }

  public static ItemStack getItemCharged() {
    return RailcraftItems.REFINED_FIRESTONE.get().getDefaultInstance();
  }

  public static ItemStack getItemEmpty() {
    var itemStack = RailcraftItems.REFINED_FIRESTONE.get().getDefaultInstance();
    itemStack.setDamageValue(CHARGES - 1);
    return itemStack;
  }

  @Override
  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return true;
  }

  @Override
  public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
    ItemStack newStack;
    double damageLevel = (double) itemStack.getDamageValue() / (double) itemStack.getMaxDamage();
    if (random.nextDouble() < damageLevel * 0.0001) {
      newStack = CrackedFirestoneItem.getItemEmpty();
      if (itemStack.has(DataComponents.CUSTOM_NAME))
        newStack.set(DataComponents.CUSTOM_NAME, itemStack.getHoverName());
    } else {
      newStack = itemStack.copy();
    }
    newStack.setCount(1);
    var res = new AtomicReference<>(newStack);
    if (CommonHooks.getCraftingPlayer() instanceof ServerPlayer serverPlayer) {
      newStack.hurtAndBreak(1, serverPlayer.serverLevel(), serverPlayer,
          __ -> res.set(ItemStack.EMPTY));
    }
    return res.get();
  }

  @Override
  public final int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
    return itemStack.getDamageValue() < itemStack.getMaxDamage() ? this.heat : 0;
  }

  @Override
  public void appendHoverText(ItemStack itemStack, TooltipContext context,
      List<Component> lines, TooltipFlag adv) {
    MutableComponent component;
    if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 5) {
      component = Component.translatable(Tips.FIRESTONE_EMPTY);
    } else {
      component = Component.translatable(Tips.FIRESTONE_CHARGED);
    }
    lines.add(component.withStyle(ChatFormatting.GRAY));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var player = context.getPlayer();
    var stack = context.getItemInHand();
    var level = context.getLevel();
    var pos = context.getClickedPos();
    var side = context.getClickedFace();
    var blockState = level.getBlockState(pos);
    var random = level.getRandom();

    if (!(level instanceof ServerLevel serverLevel))
      return InteractionResult.sidedSuccess(level.isClientSide());

    if (stack.getDamageValue() == stack.getMaxDamage())
      return InteractionResult.PASS;

    var serverPlayer = (ServerPlayer) player;

    if (player.mayUseItemAt(pos, side, stack)) {
      if (!blockState.is(Blocks.STONE)) {
        var drops = Block.getDrops(blockState, serverLevel, pos, level.getBlockEntity(pos));
        if (drops.size() == 1 && !drops.getFirst().isEmpty()
            && drops.getFirst().getItem() instanceof BlockItem) {
          var cooked = cookedItem(level, drops.getFirst());
          if (cooked.getItem() instanceof BlockItem) {
            var newState = ContainerTools.getBlockStateFromStack(cooked, level, pos);
            if (newState != null) {
              level.setBlockAndUpdate(pos, newState);
              level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.AMBIENT, 1,
                  random.nextFloat() * 0.4F + 0.8F);
              stack.hurtAndBreak(1, serverLevel, serverPlayer, __ -> {});
              return InteractionResult.SUCCESS;
            }
          }
        }
      }

      pos = pos.relative(side);

      if (player.mayUseItemAt(pos, side, stack) && level.getBlockState(pos).isAir()) {
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.AMBIENT, 1,
            random.nextFloat() * 0.4F + 0.8F);
        level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        stack.hurtAndBreak(1, serverLevel, serverPlayer, __ -> {});
      }
    }
    return InteractionResult.sidedSuccess(level.isClientSide());
  }

  @NotNull
  private ItemStack cookedItem(Level level, ItemStack ingredient) {
    return level.getRecipeManager()
        .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(ingredient), level)
        .map(x -> x.value().getResultItem(level.registryAccess()))
        .orElse(ItemStack.EMPTY);
  }

  @Override
  public InteractionResult interactLivingEntity(ItemStack itemStack, Player player,
      LivingEntity livingEntity, InteractionHand hand) {
    var level = player.level();
    if (level instanceof ServerLevel serverLevel && !livingEntity.fireImmune()) {
      livingEntity.igniteForSeconds(5);
      itemStack.hurtAndBreak(1, serverLevel, player,
          item -> player.onEquippedItemBroken(item, LivingEntity.getSlotForHand(hand)));
      level.playSound(null, livingEntity.blockPosition(), SoundEvents.FIRECHARGE_USE,
          SoundSource.AMBIENT, 1, player.getRandom().nextFloat() * 0.4F + 0.8F);
      player.swing(hand);
      level.setBlockAndUpdate(livingEntity.blockPosition(), Blocks.FIRE.defaultBlockState());
    }
    return InteractionResult.sidedSuccess(level.isClientSide());
  }

  @Override
  @NotNull
  public FirestoneItemEntity createEntity(Level level, Entity entity, ItemStack itemStack) {
    var firestone = super.createEntity(level, entity, itemStack);
    firestone.setRefined(true);
    return firestone;
  }
}

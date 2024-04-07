package gg.moonflower.etched.common.item;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.menu.BoomboxMenu;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BoomboxItem extends Item implements ContainerItem {

    private static final Map<Integer, ItemStack> PLAYING_RECORDS = new Int2ObjectArrayMap<>();
    private static final Text PAUSE = Text.translatable("item." + Etched.MOD_ID + ".boombox.pause", Text.keybind("key.sneak"), Text.keybind("key.use")).formatted(Formatting.GRAY);
    private static final Text RECORDS = Text.translatable("item." + Etched.MOD_ID + ".boombox.records");
    public static final Text PAUSED = Text.translatable("item." + Etched.MOD_ID + ".boombox.paused").formatted(Formatting.YELLOW);

    public BoomboxItem(Settings properties) {
        super(properties);
    }

    public static void onLivingEntityUpdateClient(LivingEntity entity) {
        ItemStack newPlayingRecord = ItemStack.EMPTY;
        ItemStack mainStack = entity.getMainHandStack();
        ItemStack offStack = entity.getOffHandStack();
        if (mainStack.getItem() instanceof BoomboxItem && hasRecord(mainStack) && !isPaused(mainStack)) {
            newPlayingRecord = getRecord(mainStack);
        } else if (offStack.getItem() instanceof BoomboxItem && hasRecord(offStack) && !isPaused(offStack)) {
            newPlayingRecord = getRecord(offStack);
        }

        if (entity instanceof PlayerEntity && newPlayingRecord.isEmpty() && MinecraftClient.getInstance().cameraEntity == entity) {
            PlayerInventory inventory = ((PlayerEntity) entity).getInventory();
            for (ItemStack stack : inventory.main) {
                if (stack.getItem() instanceof BoomboxItem && hasRecord(stack) && !isPaused(stack)) {
                    newPlayingRecord = getRecord(stack);
                }
            }
        }

        updatePlaying(entity, newPlayingRecord);
    }

    private static void updatePlaying(Entity entity, ItemStack record) {
        if (!ItemStack.areEqual(PLAYING_RECORDS.getOrDefault(entity.getId(), ItemStack.EMPTY), record)) {
            SoundTracker.playBoombox(entity.getId(), record);
            if (record.isEmpty()) {
                PLAYING_RECORDS.remove(entity.getId());
            } else {
                PLAYING_RECORDS.put(entity.getId(), record);
            }
        }
    }


    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.method_48926().isClient()) {
            return false;
        }
        updatePlaying(entity, hasRecord(stack) && !isPaused(stack) ? getRecord(stack) : ItemStack.EMPTY);
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.shouldCancelInteraction()) {
            setPaused(stack, !isPaused(stack));
            return TypedActionResult.success(stack, level.isClient());
        }
        if (!Etched.SERVER_CONFIG.useBoomboxMenu.get()) {
            return TypedActionResult.fail(stack);
        }
        return this.use(this, level, player, hand);
    }

    @Override
    public ScreenHandler constructMenu(int containerId, PlayerInventory inventory, PlayerEntity player, int index) {
        return new BoomboxMenu(containerId, inventory, index);
    }

    @Override
    public boolean onStackClicked(ItemStack boombox, Slot slot, ClickType clickAction, PlayerEntity player) {
        if (Etched.SERVER_CONFIG.useBoomboxMenu.get()) {
            return false;
        }
        if (clickAction != ClickType.RIGHT) {
            return false;
        }

        ItemStack clickItem = slot.getStack();
        if (clickItem.isEmpty()) {
            this.playRemoveOneSound(player);
            removeOne(boombox).ifPresent(key -> setRecord(boombox, slot.insertStack(key)));
        } else if (canAdd(boombox, clickItem)) {
            this.playInsertSound(player);
            setRecord(boombox, slot.takeStackRange(clickItem.getCount(), 1, player).split(1));
        }

        return true;
    }

    @Override
    public boolean onClicked(ItemStack boombox, ItemStack clickItem, Slot slot, ClickType clickAction, PlayerEntity player, StackReference slotAccess) {
        if (Etched.SERVER_CONFIG.useBoomboxMenu.get()) {
            return false;
        }
        if (clickAction == ClickType.RIGHT && slot.canTakePartial(player)) {
            if (clickItem.isEmpty()) {
                removeOne(boombox).ifPresent(removedKey -> {
                    this.playRemoveOneSound(player);
                    slotAccess.set(removedKey);
                });
            } else if (canAdd(boombox, clickItem)) {
                this.playInsertSound(player);
                setRecord(boombox, clickItem.split(1));
            }

            return true;
        }

        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltipComponents, TooltipContext isAdvanced) {
        tooltipComponents.add(PAUSE);
        if (hasRecord(stack)) {
            ItemStack record = getRecord(stack);
            List<Text> records = new LinkedList<>();
            record.getItem().appendTooltip(record, level, records, isAdvanced);

            if (!records.isEmpty()) {
                tooltipComponents.add(Text.empty());
                tooltipComponents.add(RECORDS);
                tooltipComponents.addAll(records);
            }
        }
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.method_48926().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.method_48926().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.method_48926().getRandom().nextFloat() * 0.4F);
    }

    /**
     * Retrieves the current hand boombox sounds are coming from for the specified entity.
     *
     * @param entity The entity to check
     * @return The hand the entity is using or <code>null</code> if no boombox is playing
     */
    @Nullable
    public static Hand getPlayingHand(LivingEntity entity) {
        if (!PLAYING_RECORDS.containsKey(entity.getId())) {
            return null;
        }
        ItemStack stack = entity.getMainHandStack();
        if (stack.getItem() instanceof BoomboxItem && hasRecord(stack)) {
            return Hand.MAIN_HAND;
        }
        stack = entity.getOffHandStack();
        if (stack.getItem() instanceof BoomboxItem && hasRecord(stack)) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    public static boolean isPaused(ItemStack stack) {
        if (!(stack.getItem() instanceof BoomboxItem)) {
            return false;
        }
        NbtCompound compoundTag = stack.getNbt();
        return compoundTag != null && compoundTag.getBoolean("Paused");
    }

    public static boolean hasRecord(ItemStack stack) {
        if (!(stack.getItem() instanceof BoomboxItem)) {
            return false;
        }
        NbtCompound compoundTag = stack.getNbt();
        return compoundTag != null && compoundTag.contains("Record", NbtElement.COMPOUND_TYPE);
    }

    public static ItemStack getRecord(ItemStack stack) {
        if (!(stack.getItem() instanceof BoomboxItem)) {
            return ItemStack.EMPTY;
        }
        NbtCompound compoundTag = stack.getNbt();
        return compoundTag != null && compoundTag.contains("Record", NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(compoundTag.getCompound("Record")) : ItemStack.EMPTY;
    }

    public static void setRecord(ItemStack stack, ItemStack record) {
        if (!(stack.getItem() instanceof BoomboxItem)) {
            return;
        }

        if (record.isEmpty()) {
            stack.removeSubNbt("Record");
        } else {
            stack.getOrCreateNbt().put("Record", record.writeNbt(new NbtCompound()));
        }
    }

    public static void setPaused(ItemStack stack, boolean paused) {
        if (!(stack.getItem() instanceof BoomboxItem)) {
            return;
        }

        if (!paused) {
            stack.removeSubNbt("Paused");
        } else {
            stack.getOrCreateNbt().putBoolean("Paused", true);
        }
    }

    private static Optional<ItemStack> removeOne(ItemStack boombox) {
        if (!hasRecord(boombox)) {
            return Optional.empty();
        }

        ItemStack record = getRecord(boombox);
        if (record.isEmpty()) {
            return Optional.empty();
        }

        setRecord(boombox, ItemStack.EMPTY);
        return Optional.of(record);
    }

    private static boolean canAdd(ItemStack boombox, ItemStack record) {
        if (!(boombox.isOf(EtchedItems.BOOMBOX.get())) || !(record.getItem() instanceof PlayableRecord)) {
            return false;
        }
        return getRecord(boombox).isEmpty();
    }
}

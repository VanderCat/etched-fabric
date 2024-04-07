package gg.moonflower.etched.common.item;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.PlayableRecordItem;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.client.render.item.AlbumCoverItemRenderer;
import gg.moonflower.etched.common.menu.AlbumCoverMenu;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedItems;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AlbumCoverItem extends PlayableRecordItem implements ContainerItem {

    public static final int MAX_RECORDS = 9;

    public AlbumCoverItem(Settings properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                return AlbumCoverItemRenderer.INSTANCE;
            }
        });
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.shouldCancelInteraction()) {
            if (dropContents(stack, player)) {
                this.playDropContentsSound(player);
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.success(stack, level.isClient());
            }
            return TypedActionResult.pass(stack);
        }

        if (!Etched.SERVER_CONFIG.useAlbumCoverMenu.get()) {
            return TypedActionResult.fail(stack);
        }
        return this.use(this, level, player, hand);
    }

    @Override
    public ScreenHandler constructMenu(int containerId, PlayerInventory inventory, PlayerEntity player, int index) {
        return new AlbumCoverMenu(containerId, inventory, index);
    }

    @Override
    public boolean onStackClicked(ItemStack albumCover, Slot slot, ClickType clickAction, PlayerEntity player) {
        if (Etched.SERVER_CONFIG.useAlbumCoverMenu.get()) {
            return false;
        }
        if (clickAction != ClickType.RIGHT) {
            return false;
        }

        ItemStack clickItem = slot.getStack();
        if (clickItem.isEmpty()) {
            removeOne(albumCover).ifPresent(record -> {
                this.playRemoveOneSound(player);
                add(albumCover, slot.insertStack(record));
            });
        } else if (canAdd(albumCover, clickItem)) {
            this.playInsertSound(player);
            add(albumCover, slot.takeStackRange(clickItem.getCount(), 1, player));
        }

        return true;
    }

    @Override
    public boolean onClicked(ItemStack albumCover, ItemStack clickItem, Slot slot, ClickType clickAction, PlayerEntity player, StackReference slotAccess) {
        if (Etched.SERVER_CONFIG.useAlbumCoverMenu.get()) {
            return false;
        }
        if (clickAction == ClickType.RIGHT && slot.canTakePartial(player)) {
            if (clickItem.isEmpty()) {
                removeOne(albumCover).ifPresent(removedRecord -> {
                    this.playRemoveOneSound(player);
                    slotAccess.set(removedRecord);
                });
            } else if (canAdd(albumCover, clickItem)) {
                this.playInsertSound(player);
                add(albumCover, clickItem);
            }

            return true;
        }

        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> list, TooltipContext tooltipFlag) {
        for (ItemStack record : getRecords(stack)) {
            if (record.getItem() instanceof PlayableRecord) {
                record.getItem().appendTooltip(record, level, list, tooltipFlag);
            }
        }
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity itemEntity) {
        ItemUsage.spawnItemContents(itemEntity, getRecords(itemEntity.getStack()).stream());
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

    private static Optional<ItemStack> removeOne(ItemStack albumCover) {
        NbtCompound tag = albumCover.getOrCreateNbt();
        if (!tag.contains("Records", NbtElement.LIST_TYPE)) {
            return Optional.empty();
        }

        NbtList recordsNbt = tag.getList("Records", NbtElement.COMPOUND_TYPE);
        if (recordsNbt.isEmpty()) {
            return Optional.empty();
        }

        NbtCompound recordNbt = recordsNbt.getCompound(recordsNbt.size() - 1);
        ItemStack recordStack = ItemStack.fromNbt(recordNbt);
        recordsNbt.remove(recordsNbt.size() - 1);

        return Optional.of(recordStack);
    }

    private static boolean dropContents(ItemStack itemStack, PlayerEntity player) {
        NbtCompound tag = itemStack.getOrCreateNbt();
        if (!tag.contains("Records")) {
            return false;
        }

        if (player instanceof ServerPlayerEntity) {
            NbtList listTag = tag.getList("Records", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < listTag.size(); i++) {
                player.getInventory().offerOrDrop(ItemStack.fromNbt(listTag.getCompound(i)));
            }
        }

        itemStack.removeSubNbt("Records");
        return true;
    }

    private static void add(ItemStack albumCover, ItemStack record) {
        if (!albumCover.isOf(EtchedItems.ALBUM_COVER.get()) || !AlbumCoverMenu.isValid(record)) {
            return;
        }

        NbtCompound tag = albumCover.getOrCreateNbt();
        if (!tag.contains("Records")) {
            tag.put("Records", new NbtList());
        }

        NbtList recordsNbt = tag.getList("Records", NbtElement.COMPOUND_TYPE);

        ItemStack singleRecord = record.split(1);
        NbtCompound recordTag = new NbtCompound();
        singleRecord.writeNbt(recordTag);
        recordsNbt.add(recordTag);

        if (getCoverStack(albumCover).isEmpty()) {
            getRecords(albumCover).stream().filter(stack -> !stack.isEmpty()).findFirst().ifPresent(stack -> setCover(albumCover, stack));
        }
    }

    private static boolean canAdd(ItemStack albumCover, ItemStack record) {
        if (!albumCover.isOf(EtchedItems.ALBUM_COVER.get()) || !AlbumCoverMenu.isValid(record)) {
            return false;
        }
        return albumCover.getNbt() == null || !albumCover.getNbt().contains("Records", NbtElement.LIST_TYPE) || albumCover.getNbt().getList("Records", NbtElement.COMPOUND_TYPE).size() < MAX_RECORDS;
    }

    @Override
    public Optional<TrackData[]> getMusic(ItemStack stack) {
        List<ItemStack> records = getRecords(stack);
        return records.isEmpty() ? Optional.empty() : Optional.of(records.stream().filter(record -> record.getItem() instanceof PlayableRecord).flatMap(record -> Arrays.stream(((PlayableRecord) record.getItem()).getMusic(record).orElseGet(() -> new TrackData[0]))).toArray(TrackData[]::new));
    }

    @Override
    public Optional<TrackData> getAlbum(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public int getTrackCount(ItemStack stack) {
        return getRecords(stack).stream().filter(record -> record.getItem() instanceof PlayableRecord).mapToInt(record -> ((PlayableRecord) record.getItem()).getTrackCount(record)).sum();
    }

    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return getCoverStack(stack).isPresent();
    }

    public static Optional<ItemStack> getCoverStack(ItemStack stack) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.get()) {
            return Optional.empty();
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("CoverRecord", NbtElement.COMPOUND_TYPE)) {
            return Optional.empty();
        }

        ItemStack cover = ItemStack.fromNbt(nbt.getCompound("CoverRecord"));
        return cover.isEmpty() ? Optional.empty() : Optional.of(cover);
    }

    public static List<ItemStack> getRecords(ItemStack stack) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.get()) {
            return Collections.emptyList();
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("Records", NbtElement.LIST_TYPE)) {
            return Collections.emptyList();
        }

        NbtList recordsNbt = nbt.getList("Records", NbtElement.COMPOUND_TYPE);
        if (recordsNbt.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> list = new ArrayList<>(recordsNbt.size());
        for (int i = 0; i < Math.min(MAX_RECORDS, recordsNbt.size()); i++) {
            ItemStack record = ItemStack.fromNbt(recordsNbt.getCompound(i));
            if (!record.isEmpty()) {
                list.add(record);
            }
        }

        return list;
    }

    public static void setCover(ItemStack stack, ItemStack record) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.get()) {
            return;
        }

        if (record.isEmpty()) {
            stack.removeSubNbt("CoverRecord");
            return;
        }
        stack.getOrCreateNbt().put("CoverRecord", record.writeNbt(new NbtCompound()));
    }

    public static void setRecords(ItemStack stack, Collection<ItemStack> records) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.get() || records.isEmpty()) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList recordsNbt = new NbtList();
        int i = 0;
        for (ItemStack record : records) {
            if (record.isEmpty()) {
                continue;
            }
            if (i >= MAX_RECORDS) {
                break;
            }
            recordsNbt.add(record.writeNbt(new NbtCompound()));
            i++;
        }
        nbt.put("Records", recordsNbt);
    }
}

package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.common.item.AlbumCoverItem;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/**
 * @author Ocelot
 */
public class AlbumCoverContainer implements Inventory {

    private final PlayerInventory inventory;
    private final int index;
    private final ItemStack albumCover;
    private final DefaultedList<ItemStack> records;

    public AlbumCoverContainer(PlayerInventory inventory, int index) {
        this.inventory = inventory;
        this.index = index;
        this.albumCover = inventory.getStack(index);
        this.records = DefaultedList.ofSize(AlbumCoverItem.MAX_RECORDS, ItemStack.EMPTY);

        List<ItemStack> keys = AlbumCoverItem.getRecords(this.albumCover);
        for (int i = 0; i < keys.size(); i++) {
            this.records.set(i, keys.get(i));
        }
    }

    private void update() {
        AlbumCoverItem.setRecords(this.albumCover, this.records);
    }

    @Override
    public int size() {
        return this.records.size();
    }

    @Override
    public boolean isEmpty() {
        return this.records.isEmpty() || this.records.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int index) {
        if (index < 0 || index >= this.records.size()) {
            return ItemStack.EMPTY;
        }
        return this.records.get(index);
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        ItemStack result = Inventories.splitStack(this.records, index, count);
        this.update();
        return result;
    }

    @Override
    public ItemStack removeStack(int index) {
        ItemStack result = Inventories.removeStack(this.records, index);
        this.update();
        return result;
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        if (index < 0 || index >= this.records.size()) {
            return;
        }
        this.records.set(index, stack);
        this.update();
    }

    @Override
    public void markDirty() {
        this.update();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return ItemStack.areEqual(this.inventory.getStack(this.index), this.albumCover);
    }

    @Override
    public void clear() {
        this.records.clear();
        this.update();
    }
}

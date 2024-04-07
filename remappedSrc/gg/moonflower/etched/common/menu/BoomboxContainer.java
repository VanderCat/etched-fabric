package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.common.item.BoomboxItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/**
 * @author Ocelot
 */
public class BoomboxContainer implements Inventory {

    private final PlayerInventory inventory;
    private final int index;
    private final ItemStack boombox;
    private final DefaultedList<ItemStack> keys;

    public BoomboxContainer(PlayerInventory inventory, int index) {
        this.inventory = inventory;
        this.index = index;
        this.boombox = inventory.getStack(index);
        this.keys = DefaultedList.copyOf(ItemStack.EMPTY, BoomboxItem.getRecord(this.boombox));
    }

    private void update() {
        BoomboxItem.setRecord(this.boombox, this.keys.get(0));
    }

    @Override
    public int size() {
        return this.keys.size();
    }

    @Override
    public boolean isEmpty() {
        return this.keys.isEmpty() || this.keys.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int index) {
        if (index < 0 || index >= this.keys.size()) {
            return ItemStack.EMPTY;
        }
        return this.keys.get(index);
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        ItemStack result = Inventories.splitStack(this.keys, index, count);
        this.update();
        return result;
    }

    @Override
    public ItemStack removeStack(int index) {
        ItemStack result = Inventories.removeStack(this.keys, index);
        this.update();
        return result;
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        if (index < 0 || index >= this.keys.size()) {
            return;
        }
        this.keys.set(index, stack);
        this.update();
    }

    @Override
    public void markDirty() {
        this.update();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return ItemStack.areEqual(this.inventory.getStack(this.index), this.boombox);
    }

    @Override
    public void clear() {
        this.keys.clear();
        this.update();
    }
}

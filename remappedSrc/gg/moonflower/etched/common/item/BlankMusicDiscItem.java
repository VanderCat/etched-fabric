package gg.moonflower.etched.common.item;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class BlankMusicDiscItem extends Item implements DyeableItem {

    public BlankMusicDiscItem(Settings properties) {
        super(properties);
    }

    @Override
    public int getColor(ItemStack itemStack) {
        NbtCompound compoundTag = itemStack.getSubNbt("display");
        return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 0x515151;
    }
}

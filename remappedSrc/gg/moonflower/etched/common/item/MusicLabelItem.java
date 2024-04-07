package gg.moonflower.etched.common.item;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class MusicLabelItem extends SimpleMusicLabelItem implements DyeableItem {
    public MusicLabelItem(Settings properties) {
        super(properties);
    }

    @Override
    public int getColor(ItemStack itemStack) {
        NbtCompound compoundTag = itemStack.getSubNbt("display");
        return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 0xFFFFFF;
    }

    public static int getLabelColor(ItemStack stack) {
        if (stack.getItem() instanceof MusicLabelItem) {
            return ((MusicLabelItem) stack.getItem()).getColor(stack);
        }
        return -1;
    }
}

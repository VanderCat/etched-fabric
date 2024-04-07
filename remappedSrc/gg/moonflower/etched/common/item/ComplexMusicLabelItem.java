package gg.moonflower.etched.common.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ComplexMusicLabelItem extends SimpleMusicLabelItem {

    public ComplexMusicLabelItem(Settings properties) {
        super(properties);
    }

    public static int getPrimaryColor(ItemStack stack) {
        NbtCompound compoundTag = stack.getSubNbt("Label");
        return compoundTag != null && compoundTag.contains("PrimaryColor", 99) ? compoundTag.getInt("PrimaryColor") : 0xFFFFFF;
    }

    public static int getSecondaryColor(ItemStack itemStack) {
        NbtCompound compoundTag = itemStack.getSubNbt("Label");
        return compoundTag != null && compoundTag.contains("SecondaryColor", 99) ? compoundTag.getInt("SecondaryColor") : 0xFFFFFF;
    }

    public static void setColor(ItemStack stack, int primary, int secondary) {
        if (!(stack.getItem() instanceof ComplexMusicLabelItem)) {
            return;
        }

        NbtCompound tag = stack.getOrCreateSubNbt("Label");
        tag.putInt("PrimaryColor", primary);
        tag.putInt("SecondaryColor", secondary);
    }
}

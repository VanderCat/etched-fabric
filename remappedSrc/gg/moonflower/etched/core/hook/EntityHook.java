package gg.moonflower.etched.core.hook;

import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class EntityHook {

    public static void warpRadio(ServerWorld level, ItemEntity entity) {
        if (level.getRegistryKey() == World.NETHER) {
            ItemStack oldStack = entity.getStack();
            if (oldStack.getItem() != EtchedBlocks.RADIO.get().asItem())
                return;

            ItemStack newStack = new ItemStack(EtchedBlocks.PORTAL_RADIO_ITEM.get(), oldStack.getCount());
            newStack.setNbt(oldStack.getNbt());
            entity.setStack(newStack);
        }
    }
}

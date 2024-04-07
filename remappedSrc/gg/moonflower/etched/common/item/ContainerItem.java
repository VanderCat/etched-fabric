package gg.moonflower.etched.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public interface ContainerItem {

    static int findSlotMatchingItem(PlayerInventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.main.size(); ++i) {
            ItemStack slotStack = inventory.main.get(i);
            if (!slotStack.isEmpty() && ItemStack.canCombine(stack, slotStack)) {
                return i;
            }
        }

        return -1;
    }

    default TypedActionResult<ItemStack> use(Item item, World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        int index = findSlotMatchingItem(player.getInventory(), stack);
        if (index == -1) {
            return TypedActionResult.pass(stack);
        }

        if (!level.isClient()) {
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            player.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return stack.getName();
                }

                @Override
                public ScreenHandler createMenu(int containerId, PlayerInventory inventory, PlayerEntity player) {
                    return ContainerItem.this.constructMenu(containerId, inventory, player, index);
                }
            });
        }
        return TypedActionResult.success(stack, level.isClient());
    }

    ScreenHandler constructMenu(int containerId, PlayerInventory inventory, PlayerEntity player, int index);
}

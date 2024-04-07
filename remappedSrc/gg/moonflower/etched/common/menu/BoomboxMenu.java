package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * @author Ocelot
 */
public class BoomboxMenu extends ScreenHandler {

    private final Inventory boomboxInventory;

    public BoomboxMenu(int containerId, PlayerInventory inventory) {
        this(containerId, inventory, -1);
    }

    public BoomboxMenu(int containerId, PlayerInventory inventory, int index) {
        super(EtchedMenus.BOOMBOX_MENU.get(), containerId);
        this.boomboxInventory = index == -1 ? new SimpleInventory(1) : new BoomboxContainer(inventory, index);

        this.addSlot(new Slot(this.boomboxInventory, 0, 80, 20) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof PlayableRecord;
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, y * 18 + 51) {
                    @Override
                    public boolean canTakeItems(PlayerEntity player) {
                        return this.getStack().getItem() != EtchedItems.BOOMBOX.get();
                    }
                });
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 109) {
                @Override
                public boolean canTakeItems(PlayerEntity player) {
                    return this.getStack().getItem() != EtchedItems.BOOMBOX.get();
                }
            });
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.boomboxInventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 0) {
                if (!this.insertItem(itemStack2, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemStack;
    }
}

package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.item.AlbumCoverItem;
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
public class AlbumCoverMenu extends ScreenHandler {

    private final PlayerInventory inventory;
    private final Inventory albumCoverInventory;
    private final int albumCoverIndex;

    public AlbumCoverMenu(int containerId, PlayerInventory inventory) {
        this(containerId, inventory, -1);
    }

    public AlbumCoverMenu(int containerId, PlayerInventory inventory, int albumCoverIndex) {
        super(EtchedMenus.ALBUM_COVER_MENU.get(), containerId);
        this.albumCoverInventory = albumCoverIndex == -1 ? new SimpleInventory(AlbumCoverItem.MAX_RECORDS) : new AlbumCoverContainer(inventory, albumCoverIndex);
        this.albumCoverIndex = albumCoverIndex;
        this.inventory = inventory;

        for (int n = 0; n < 3; ++n) {
            for (int m = 0; m < 3; ++m) {
                this.addSlot(new Slot(this.albumCoverInventory, m + n * 3, 62 + m * 18, 17 + n * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return isValid(stack);
                    }
                });
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, y * 18 + 84) {
                    @Override
                    public boolean canTakeItems(PlayerEntity player) {
                        return this.getStack().getItem() != EtchedItems.ALBUM_COVER.get();
                    }
                });
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142) {
                @Override
                public boolean canTakeItems(PlayerEntity player) {
                    return this.getStack().getItem() != EtchedItems.ALBUM_COVER.get();
                }
            });
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.albumCoverInventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (this.albumCoverIndex == -1) {
            return;
        }
        ItemStack cover = this.inventory.getStack(this.albumCoverIndex);
        if (!AlbumCoverItem.getCoverStack(cover).isPresent()) {
            for (int i = 0; i < this.albumCoverInventory.size(); i++) {
                ItemStack stack = this.albumCoverInventory.getStack(i);
                if (!stack.isEmpty()) {
                    AlbumCoverItem.setCover(cover, stack);
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < 9) {
                if (!this.insertItem(itemStack2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, 9, false)) {
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

    public static boolean isValid(ItemStack stack) {
        return PlayableRecord.isPlayableRecord(stack) && !stack.isOf(EtchedItems.ALBUM_COVER.get());
    }
}

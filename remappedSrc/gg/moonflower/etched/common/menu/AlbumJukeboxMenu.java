package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.common.network.play.SetAlbumJukeboxTrackPacket;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Ocelot
 */
public class AlbumJukeboxMenu extends ScreenHandler {

    private final BlockPos.Mutable pos;
    private final Inventory container;
    private boolean initialized;

    public AlbumJukeboxMenu(int i, PlayerInventory inventory) {
        this(i, inventory, new SimpleInventory(9), BlockPos.ORIGIN);
    }

    public AlbumJukeboxMenu(int i, PlayerInventory inventory, Inventory container, BlockPos pos) {
        super(EtchedMenus.ALBUM_JUKEBOX_MENU.get(), i);
        checkSize(container, 9);
        this.container = container;
        container.onOpen(inventory.player);

        this.pos = new BlockPos.Mutable().set(pos);
        this.addProperty(new Property() {
            @Override
            public int get() {
                return AlbumJukeboxMenu.this.pos.getX();
            }

            @Override
            public void set(int value) {
                AlbumJukeboxMenu.this.pos.setX(value);
            }
        });
        this.addProperty(new Property() {
            @Override
            public int get() {
                return AlbumJukeboxMenu.this.pos.getY();
            }

            @Override
            public void set(int value) {
                AlbumJukeboxMenu.this.pos.setY(value);
            }
        });
        this.addProperty(new Property() {
            @Override
            public int get() {
                return AlbumJukeboxMenu.this.pos.getZ();
            }

            @Override
            public void set(int value) {
                AlbumJukeboxMenu.this.pos.setZ(value);
            }
        });

        for (int n = 0; n < 3; ++n) {
            for (int m = 0; m < 3; ++m) {
                this.addSlot(new Slot(container, m + n * 3, 62 + m * 18, 17 + n * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return PlayableRecord.isPlayableRecord(stack);
                    }
                });
            }
        }

        for (int n = 0; n < 3; ++n) {
            for (int m = 0; m < 9; ++m) {
                this.addSlot(new Slot(inventory, m + n * 9 + 9, 8 + m * 18, 84 + n * 18));
            }
        }

        for (int n = 0; n < 9; ++n) {
            this.addSlot(new Slot(inventory, n, 8 + n * 18, 142));
        }
    }

    public boolean setPlayingTrack(World level, SetAlbumJukeboxTrackPacket pkt) {
        BlockEntity blockEntity = level.getBlockEntity(this.pos);
        if (blockEntity instanceof AlbumJukeboxBlockEntity) {
            return ((AlbumJukeboxBlockEntity) blockEntity).setPlayingIndex(pkt.playingIndex(), pkt.track());
        }
        return false;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.container.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (i < this.container.size()) {
                if (!this.insertItem(itemStack2, this.container.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, this.container.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.container.onClose(player);
    }

    @Override
    public void setProperty(int index, int value) {
        super.setProperty(index, value);
        if (index >= 0 && index < 3) {
            this.initialized = true;
        }
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}

package gg.moonflower.etched.common.blockentity;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.block.AlbumJukeboxBlock;
import gg.moonflower.etched.common.menu.AlbumJukeboxMenu;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * @author Ocelot
 */
public class AlbumJukeboxBlockEntity extends LootableContainerBlockEntity implements SidedInventory {

    private static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    private DefaultedList<ItemStack> items;
    private int playingIndex;
    private int track;
    private ItemStack playingStack;
    private boolean loaded;

    public AlbumJukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(EtchedBlocks.ALBUM_JUKEBOX_BE.get(), pos, state);
        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        this.playingIndex = -1;
        this.track = 0;
        this.playingStack = ItemStack.EMPTY;
    }

    public static void tick(World level, BlockPos pos, BlockState state, AlbumJukeboxBlockEntity entity) {
        if (level == null || !level.isClient()) {
            return;
        }

        if (!entity.loaded) {
            entity.loaded = true;
            SoundTracker.playAlbum(entity, state, (ClientWorld) level, pos, false);
        }

        if (entity.isPlaying()) {
            Box range = new Box(pos).expand(3.45);
            List<LivingEntity> livingEntities = level.getNonSpectatingEntities(LivingEntity.class, range);
            livingEntities.forEach(living -> living.setNearbySongPlaying(pos, true));
        }
    }

    private void updateState() {
        if (this.world != null) {
            boolean hasItem = false;
            for (ItemStack stack : this.getInvStackList()) {
                if (stack != ItemStack.EMPTY) {
                    hasItem = true;
                    break;
                }
            }

            boolean hasRecord = this.world.getBlockState(this.pos).get(AlbumJukeboxBlock.HAS_RECORD);
            if (hasItem != hasRecord) {
                this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(AlbumJukeboxBlock.HAS_RECORD, hasItem), 3);
                this.markDirty();
            }
        }
    }

    private void updatePlaying() {
        if (this.world == null) {
            return;
        }
        this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, this.items);
        }
        if (this.loaded) {
            SoundTracker.playAlbum(this, this.getCachedState(), (ClientWorld) this.world, this.getPos(), false);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (!this.serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.items);
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int index, ItemStack stack, @Nullable Direction direction) {
        return PlayableRecord.isPlayableRecord(stack);
    }

    @Override
    public boolean canExtract(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int size() {
        return 9;
    }

    @Override
    public ItemStack removeStack(int index, int amount) {
        ItemStack stack = super.removeStack(index, amount);
        this.updateState();
        if (!stack.isEmpty()) {
            this.updatePlaying();
        }
        return stack;
    }

    @Override
    public ItemStack removeStack(int index) {
        ItemStack stack = super.removeStack(index);
        this.updateState();
        if (!stack.isEmpty()) {
            this.updatePlaying();
        }
        return stack;
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        super.setStack(index, stack);
        this.updateState();
        this.updatePlaying();
    }

    @Override
    public void clear() {
        super.clear();
        this.updateState();
        this.updatePlaying();
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.items;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container." + Etched.MOD_ID + ".album_jukebox");
    }

    @Override
    protected ScreenHandler createScreenHandler(int menuId, PlayerInventory inventory) {
        return new AlbumJukeboxMenu(menuId, inventory, this, this.getPos());
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    public int getPlayingIndex() {
        return this.playingIndex;
    }

    public int getTrack() {
        return this.track;
    }

    /**
     * Sets the playing disc and track.
     *
     * @param playingIndex The new index to play
     * @param track        The track to play on the disc
     * @return Whether a change was made in the index
     */
    public boolean setPlayingIndex(int playingIndex, int track) {
        this.playingIndex = playingIndex;
        this.track = track;

        if (this.recalculatePlayingIndex(false)) {
            int tracks = PlayableRecord.getStackTrackCount(this.playingStack);
            if (this.track >= tracks) {
                this.track = 0;
            }
            return true;
        }

        return false;
    }

    /**
     * Stops playing the current track and resets to the start.
     */
    public void stopPlaying() {
        this.playingIndex = -1;
        this.track = 0;
        this.playingStack = ItemStack.EMPTY;
    }

    /**
     * Cycles to the previous index to begin playing.
     */
    public void previous() {
        if (this.track > 0) {
            this.track--;
        } else {
            this.playingIndex--;
            if (this.playingIndex < 0) {
                this.playingIndex = this.size() - 1;
            }
            this.nextPlayingIndex(true);
            this.track = Math.max(0, this.playingIndex < 0 || this.playingIndex >= this.size() ? 0 : PlayableRecord.getStackTrackCount(this.getStack(this.playingIndex)) - 1);
            this.playingStack = ItemStack.EMPTY;
        }
    }

    /**
     * Cycles to the next index to begin playing.
     */
    public void next() {
        int tracks = this.playingIndex < 0 || this.playingIndex >= this.size() ? 1 : PlayableRecord.getStackTrackCount(this.getStack(this.playingIndex));
        if (this.track < tracks - 1) {
            this.track++;
        } else {
            this.playingIndex++;
            this.playingIndex %= this.size();
            this.nextPlayingIndex(false);
            this.track = 0;
            this.playingStack = ItemStack.EMPTY;
        }
    }

    /**
     * Starts playing the next valid song in the album.
     */
    public void nextPlayingIndex(boolean reverse) {
        boolean wrap = false;
        this.playingIndex = MathHelper.clamp(this.playingIndex, 0, this.size() - 1);
        while (!PlayableRecord.isPlayableRecord(this.getStack(this.playingIndex))) {
            if (reverse) {
                this.playingIndex--;
                if (this.playingIndex < 0) {
                    this.playingIndex = this.size() - 1;
                    if (wrap) {
                        this.playingIndex = -1;
                        this.track = 0;
                        this.playingStack = ItemStack.EMPTY;
                        return;
                    }
                    wrap = true;
                }
            } else {
                this.playingIndex++;
                if (this.playingIndex >= this.size()) {
                    this.playingIndex = 0;
                    if (wrap) {
                        this.playingIndex = -1;
                        this.track = 0;
                        this.playingStack = ItemStack.EMPTY;
                        return;
                    }
                    wrap = true;
                }
            }
        }
        this.playingStack = this.getStack(this.playingIndex).copy();
    }

    /**
     * Changes the current playing index to the next valid disc.
     *
     * @return Whether a change was made
     */
    public boolean recalculatePlayingIndex(boolean reverse) {
        if (this.isEmpty()) {
            if (this.playingIndex == -1) {
                return false;
            }
            this.playingIndex = -1;
            this.track = 0;
            return true;
        }

        int oldIndex = this.playingIndex;
        ItemStack oldStack = this.playingStack.copy();
        this.nextPlayingIndex(reverse);
        if (oldIndex != this.playingIndex || !ItemStack.areEqual(oldStack, this.playingStack)) {
            this.track = reverse ? Math.max(0, this.playingIndex < 0 || this.playingIndex >= this.size() ? 0 : PlayableRecord.getStackTrackCount(this.getStack(this.playingIndex)) - 1) : 0;
            return true;
        }
        return false;
    }

    public boolean isPlaying() {
        BlockState state = this.getCachedState();
        return (!state.contains(AlbumJukeboxBlock.POWERED) || !state.get(AlbumJukeboxBlock.POWERED)) && !this.isEmpty();
    }
}
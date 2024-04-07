package gg.moonflower.etched.common.entity;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedEntities;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ocelot
 */
public class MinecartJukebox extends AbstractMinecartEntity implements SidedInventory {

    private static final TrackedData<Boolean> DATA_ID_HAS_RECORD = DataTracker.registerData(MinecartJukebox.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final int[] SLOTS = {0};

    private ItemStack record;

    public MinecartJukebox(EntityType<?> entityType, World level) {
        super(entityType, level);
        this.record = ItemStack.EMPTY;
    }

    public MinecartJukebox(World level, double d, double e, double f) {
        super(EtchedEntities.JUKEBOX_MINECART.get(), level, d, e, f);
        this.record = ItemStack.EMPTY;
    }

    private void startPlaying(ItemStack stack, boolean restart) {
        if (PlayableRecord.isPlayableRecord(stack)) {
            PlayableRecord.playEntityRecord(this, stack, restart);
            this.dataTracker.set(DATA_ID_HAS_RECORD, true);
        }
    }

    private void stopPlaying() {
        if (this.dataTracker.get(DATA_ID_HAS_RECORD)) {
            PlayableRecord.stopEntityRecord(this);
            this.dataTracker.set(DATA_ID_HAS_RECORD, false);
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_ID_HAS_RECORD, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.method_48926().isClient()) {
            if (Etched.CLIENT_CONFIG.showNotes.get() && this.random.nextInt(6) == 0) {
                SoundInstance instance = SoundTracker.getEntitySound(this.getId());
                if (instance != null && MinecraftClient.getInstance().getSoundManager().isPlaying(instance)) {
                    this.method_48926().addParticle(ParticleTypes.NOTE, this.getX(), this.getY() + 1.2D, this.getZ(), this.random.nextInt(25) / 24D, 0, 0);
                }
            }
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (this.dataTracker.get(DATA_ID_HAS_RECORD)) {
            if (!this.method_48926().isClient()) {
                ItemStack itemStack = this.record;
                if (!itemStack.isEmpty()) {
                    this.clear();
                    ItemEntity itemEntity = new ItemEntity(this.method_48926(), this.getX(), this.getY() + 0.8, this.getZ(), itemStack.copy());
                    itemEntity.setToDefaultPickupDelay();
                    this.method_48926().spawnEntity(itemEntity);
                }
            }
            return ActionResult.success(this.method_48926().isClient());
        } else if (stack.getItem() instanceof PlayableRecord) {
            if (!this.method_48926().isClient()) {
                this.setStack(0, stack.copy());
                stack.decrement(1);
                player.incrementStat(Stats.PLAY_RECORD);
            }
            return ActionResult.success(this.method_48926().isClient());
        }
        return ActionResult.PASS;
    }

    @Override
    public void onActivatorRail(int x, int y, int z, boolean powered) {
        if (!this.record.isEmpty()) {
            this.startPlaying(this.record.copy(), true);
        }
    }

    @Override
    public void dropItems(DamageSource damageSource) {
        super.dropItems(damageSource);
        if (this.method_48926().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemScatterer.spawn(this.method_48926(), this.getX(), this.getY(), this.getZ(), this.record);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.method_48926().isClient() && reason.shouldDestroy()) {
            ItemScatterer.spawn(this.method_48926(), this, this);
        }
        super.remove(reason);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("HasRecord", this.dataTracker.get(DATA_ID_HAS_RECORD));
        if (!this.record.isEmpty()) {
            nbt.put("RecordItem", this.record.writeNbt(new NbtCompound()));
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(DATA_ID_HAS_RECORD, nbt.getBoolean("HasRecord"));
        if (nbt.contains("RecordItem", 10)) {
            this.record = ItemStack.fromNbt(nbt.getCompound("RecordItem"));
        }
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return Blocks.JUKEBOX.getDefaultState().with(JukeboxBlock.HAS_RECORD, this.dataTracker.get(DATA_ID_HAS_RECORD));
    }

    @Override
    public Item getItem() {
        return EtchedItems.JUKEBOX_MINECART.get();
    }

    @Override
    public Type getMinecartType() {
        return Type.SPAWNER;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
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
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.record.isEmpty();
    }

    @Override
    public ItemStack getStack(int index) {
        return index == 0 ? this.record : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        if (index != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack split = this.record.split(count);
        this.markDirty();
        if (this.record.isEmpty()) {
            this.stopPlaying();
        }
        return split;
    }

    @Override
    public ItemStack removeStack(int index) {
        return this.removeStack(index, this.record.getCount());
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        if (index == 0) {
            if (!this.record.isEmpty()) {
                this.stopPlaying();
            }
            this.startPlaying(stack.copy(), false);
            this.record = stack;
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        if (!this.record.isEmpty()) {
            this.setStack(0, ItemStack.EMPTY);
        }
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public void markDirty() {
    }
}

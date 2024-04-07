package gg.moonflower.etched.common.blockentity;

import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.block.RadioBlock;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Clearable;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * @author Ocelot
 */
public class RadioBlockEntity extends BlockEntity implements Clearable {

    private String url;
    private boolean loaded;

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(EtchedBlocks.RADIO_BE.get(), pos, state);
    }

    public static void tick(World level, BlockPos pos, BlockState state, RadioBlockEntity blockEntity) {
        if (level == null || !level.isClient()) {
            return;
        }

        if (!blockEntity.loaded) {
            blockEntity.loaded = true;
            SoundTracker.playRadio(blockEntity.url, state, (ClientWorld) level, pos);
        }

        if (blockEntity.isPlaying()) {
            Box range = new Box(pos).expand(3.45);
            List<LivingEntity> livingEntities = level.getNonSpectatingEntities(LivingEntity.class, range);
            livingEntities.forEach(living -> living.setNearbySongPlaying(pos, true));
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.url = nbt.contains("Url", NbtElement.STRING_TYPE) ? nbt.getString("Url") : null;
        if (this.loaded) {
            SoundTracker.playRadio(this.url, this.getCachedState(), (ClientWorld) this.world, this.getPos());
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        if (this.url != null) {
            nbt.putString("Url", this.url);
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
    public void clear() {
        this.url = null;
        if (this.world != null && this.world.isClient()) {
            SoundTracker.playRadio(this.url, this.getCachedState(), (ClientWorld) this.world, this.getPos());
        }
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        if (!Objects.equals(this.url, url)) {
            this.url = url;
            this.markDirty();
            if (this.world != null) {
                this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
            }
        }
    }

    public boolean isPlaying() {
        BlockState state = this.getCachedState();
        return (!state.contains(RadioBlock.POWERED) || !state.get(RadioBlock.POWERED)) && !StringHelper.isEmpty(this.url);
    }
}

package gg.moonflower.etched.core.mixin;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ClientboundPlayMusicPacket;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements SingleStackInventory {

    @Shadow
    @Final
    private DefaultedList<ItemStack> items;

    @Shadow
    protected abstract void setHasRecordBlockState(@Nullable Entity entity, boolean hasRecord);

    @Shadow
    public abstract void startPlaying();

    @Shadow
    public abstract boolean isRecordPlaying();

    @Shadow
    protected abstract boolean shouldSendJukeboxPlayingEvent();

    @Shadow
    private int ticksSinceLastEvent;

    @Shadow
    protected abstract void spawnMusicParticles(World level, BlockPos pos);

    public JukeboxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "startPlaying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/player/Player;ILnet/minecraft/core/BlockPos;I)V", shift = At.Shift.AFTER))
    public void startPlaying(CallbackInfo ci) {
        if (!(this.getStack().getItem() instanceof MusicDiscItem)) {
            BlockPos pos = this.getPos();
            EtchedMessages.PLAY.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64, this.level.dimension())), new ClientboundPlayMusicPacket(this.getStack().copy(), pos));
        }
    }

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    public void setItem(int slot, ItemStack stack, CallbackInfo ci) {
        if (stack.isOf(EtchedItems.ALBUM_COVER.get()) && this.world != null) {
            this.items.set(slot, stack);
            this.setHasRecordBlockState(null, true);
            this.startPlaying();
            ci.cancel();
        }
    }

    @Inject(method = "canPlaceItem", at = @At("RETURN"), cancellable = true)
    public void canPlaceItem(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            cir.setReturnValue(stack.isOf(EtchedItems.ALBUM_COVER.get()) && this.getStack(index).isEmpty());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(World level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this.isRecordPlaying()) {
            Item item = this.getStack().getItem();
            if (!(item instanceof MusicDiscItem) && item instanceof PlayableRecord) {
                ++this.ticksSinceLastEvent;

                // Allow music particles and events to play for custom records
                if (this.shouldSendJukeboxPlayingEvent()) {
                    this.ticksSinceLastEvent = 0;
                    level.emitGameEvent(GameEvent.JUKEBOX_PLAY, pos, GameEvent.Emitter.of(state));
                    this.spawnMusicParticles(level, pos);
                } else {
                    this.ticksSinceLastEvent--;
                }
            }
        }
    }
}

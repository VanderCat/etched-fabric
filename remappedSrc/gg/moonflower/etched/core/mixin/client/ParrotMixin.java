package gg.moonflower.etched.core.mixin.client;

import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.item.BoomboxItem;
import gg.moonflower.etched.core.registry.EtchedTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ParrotEntity.class)
public abstract class ParrotMixin extends Entity {

    @Shadow
    private BlockPos jukebox;
    @Shadow
    private boolean partyParrot;

    @Unique
    private BlockPos etched$musicPos;
    @Unique
    private boolean etched$dancing;

    public ParrotMixin(EntityType<?> entityType, World level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void capture(CallbackInfo ci) {
        this.etched$musicPos = this.jukebox;
        this.etched$dancing = this.partyParrot;
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/ShoulderRidingEntity;aiStep()V"))
    public void addAudioProviders(CallbackInfo ci) {
        if (this.etched$musicPos == null || !this.etched$musicPos.isWithinDistance(this.getPos(), 3.46) || !this.method_48926().getBlockState(this.etched$musicPos).isOf(Blocks.JUKEBOX) && !this.method_48926().getBlockState(this.etched$musicPos).isIn(EtchedTags.AUDIO_PROVIDER)) {
            this.partyParrot = false;
            this.jukebox = null;
        } else {
            this.partyParrot = this.etched$dancing;
            this.jukebox = this.etched$musicPos;
        }

        if (this.method_48926().isClient()) {
            List<Entity> entities = this.method_48926().getOtherEntities(this, this.getBoundingBox().expand(3.45), entity -> {
                if (!entity.isAlive() || entity.isSpectator()) {
                    return false;
                }
                if (entity == MinecraftClient.getInstance().player && BoomboxItem.getPlayingHand((LivingEntity) entity) == null) {
                    return false;
                }

                return SoundTracker.getEntitySound(entity.getId()) != null;
            });

            if (!entities.isEmpty()) {
                this.partyParrot = true;
            }
        }
    }
}

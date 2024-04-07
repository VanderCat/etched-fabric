package gg.moonflower.etched.core.mixin.client;

import gg.moonflower.etched.common.item.BoomboxItem;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BipedEntityModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {

    @Final
    @Shadow
    public ModelPart leftArm;

    @Final
    @Shadow
    public ModelPart rightArm;

    // TODO: fix arm swing when holding a boombox

    @Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
    public void poseRightArm(T livingEntity, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity player) {
            Hand playingHand = BoomboxItem.getPlayingHand(livingEntity);
            if ((livingEntity.getMainArm() == Arm.RIGHT && playingHand == Hand.MAIN_HAND) ||
                    (livingEntity.getMainArm() == Arm.LEFT && playingHand == Hand.OFF_HAND)) {
                this.rightArm.pitch = (float) Math.PI;
                this.rightArm.yaw = 0.0F;
                this.rightArm.roll = -0.610865F;
                ci.cancel();
            }
        }
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
    public void poseLeftArm(T livingEntity, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity player) {
            Hand playingHand = BoomboxItem.getPlayingHand(livingEntity);
            if ((livingEntity.getMainArm() == Arm.LEFT && playingHand == Hand.MAIN_HAND) ||
                    (livingEntity.getMainArm() == Arm.RIGHT && playingHand == Hand.OFF_HAND)) {
                this.leftArm.pitch = (float) Math.PI;
                this.leftArm.yaw = 0.0F;
                this.leftArm.roll = 0.610865F;
                ci.cancel();
            }
        }
    }

    @Inject(method = "setupAttackAnimation", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;xRot:F", ordinal = 2), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void setupAttackAnimation(T livingEntity, float f, CallbackInfo ci, Arm arm, ModelPart part) {
        Hand playingHand = BoomboxItem.getPlayingHand(livingEntity);
        boolean leftArm = ((livingEntity.getMainArm() == Arm.LEFT && playingHand == Hand.MAIN_HAND) || (livingEntity.getMainArm() == Arm.RIGHT && playingHand == Hand.OFF_HAND)) && arm == Arm.LEFT;
        boolean rightArm = ((livingEntity.getMainArm() == Arm.RIGHT && playingHand == Hand.MAIN_HAND) || (livingEntity.getMainArm() == Arm.LEFT && playingHand == Hand.OFF_HAND)) && arm == Arm.RIGHT;
        if (leftArm || rightArm) {
            ci.cancel();
        }
    }

}

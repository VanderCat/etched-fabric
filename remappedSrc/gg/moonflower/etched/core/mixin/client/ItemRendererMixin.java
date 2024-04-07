package gg.moonflower.etched.core.mixin.client;

import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Unique
    private static final ModelIdentifier etched$BOOMBOX_IN_HAND_MODEL = new ModelIdentifier(new Identifier(Etched.MOD_ID, "boombox_in_hand"), "inventory");

    @Shadow
    @Final
    private ItemModels itemModelShaper;

    @Unique
    private Item etched$capturedItem;

    @Unique
    private Item etched$capturedHandItem;

    @Inject(method = "render", at = @At("HEAD"))
    public void capture(ItemStack itemStack, ModelTransformationMode displayContext, boolean leftHand, MatrixStack poseStack, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        this.etched$capturedItem = itemStack.getItem();
    }

    @ModifyVariable(method = "render", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0, shift = At.Shift.BEFORE), argsOnly = true)
    public BakedModel render(BakedModel original) {
        if (this.etched$capturedHandItem == EtchedItems.BOOMBOX.get()) {
            return this.itemModelShaper.getModel(this.etched$capturedItem);
        }
        return original;
    }

    @Inject(method = "getModel", at = @At("HEAD"))
    public void capture(ItemStack itemStack, World level, LivingEntity livingEntity, int i, CallbackInfoReturnable<BakedModel> cir) {
        this.etched$capturedHandItem = itemStack.getItem();
    }

    @ModifyVariable(method = "getModel", ordinal = 0, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/ItemModelShaper;getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", shift = At.Shift.AFTER))
    public BakedModel getModel(BakedModel original) {
        if (this.etched$capturedHandItem == EtchedItems.BOOMBOX.get()) {
            return this.itemModelShaper.getModelManager().getModel(etched$BOOMBOX_IN_HAND_MODEL);
        }
        return original;
    }
}

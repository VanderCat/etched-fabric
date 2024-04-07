package gg.moonflower.etched.core.mixin.client;

import gg.moonflower.etched.common.item.BoomboxItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getTooltipLines", at = @At("TAIL"))
    public void addBoomboxStatus(PlayerEntity player, TooltipContext isAdvanced, CallbackInfoReturnable<List<Text>> cir) {
        if (this.getItem() instanceof BoomboxItem && BoomboxItem.isPaused((ItemStack) (Object) this)) {
            cir.getReturnValue().add(BoomboxItem.PAUSED);
        }
    }
}

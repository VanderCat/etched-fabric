package gg.moonflower.etched.core.mixin.fabric.client;

import gg.moonflower.etched.common.item.BoomboxItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow
    public abstract ItemStack getItem();

    private ItemEntityMixin(EntityType<?> entityType, World level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ItemStack stack = this.getItem();
        if (stack.getItem() instanceof BoomboxItem)
            ((BoomboxItem) stack.getItem()).onEntityItemUpdate(stack, (ItemEntity) (Object) this);
    }
}

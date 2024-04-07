package gg.moonflower.etched.core.mixin.fabric;

import gg.moonflower.etched.core.hook.EntityHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    public void createPortalRadio(ServerWorld server, CallbackInfoReturnable<Entity> cir) {
        if ((Object) this instanceof ItemEntity) {
            EntityHook.warpRadio(server, (ItemEntity) (Object) this);
        }
    }
}

package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.core.Etched;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import java.util.function.Supplier;

public class EtchedEntities {
    //TODO: Fix
    //public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Etched.MOD_ID);

    //public static final EntityType<MinecartJukebox> JUKEBOX_MINECART =
    //        Etched.REGISTRATE.entity("jukebox_minecart", (entityType, level) -> new MinecartJukebox(entityType, level))
    //                .register();
            //register("jukebox_minecart", () -> EntityType.Builder.<MinecartJukebox>of(MinecartJukebox::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).build("minecart_jukebox"));

    public static <R extends Entity> EntityType<R> register(String name, Supplier<EntityType<R>>value) {
        var id = new ResourceLocation(Etched.MOD_ID, name);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, value.get());
    }
    public static void register() {}
}

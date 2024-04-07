package gg.moonflower.etched.core.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructurePool.class)
public interface StructureTemplatePoolAccessor {

    @Accessor
    ObjectArrayList<StructurePoolElement> getTemplates();
}

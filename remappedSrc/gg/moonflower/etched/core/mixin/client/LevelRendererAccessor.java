package gg.moonflower.etched.core.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.math.BlockPos;

@Mixin(WorldRenderer.class)
public interface LevelRendererAccessor {

    @Accessor
    Map<BlockPos, SoundInstance> getPlayingRecords();
}

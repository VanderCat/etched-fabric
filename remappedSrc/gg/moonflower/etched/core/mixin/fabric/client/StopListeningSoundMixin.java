package gg.moonflower.etched.core.mixin.fabric.client;

import gg.moonflower.etched.api.sound.StopListeningSound;
import gg.moonflower.etched.api.sound.WrappedSoundInstance;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.util.Identifier;

@Mixin(StopListeningSound.class)
public abstract class StopListeningSoundMixin implements SoundInstance, WrappedSoundInstance {

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
        return this.getParent().getAudioStream(loader, id, repeatInstantly);
    }
}

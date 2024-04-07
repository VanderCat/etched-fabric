package gg.moonflower.etched.api.sound;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

/**
 * Wrapper for {@link SoundInstance} that respects {@link SoundStopListener}.
 *
 * @author Ocelot
 */
public class StopListeningSound implements SoundInstance, SoundStopListener, WrappedSoundInstance {

    private final SoundInstance source;
    private final SoundStopListener listener;
    private boolean ignoringEvents;

    StopListeningSound(SoundInstance source, SoundStopListener listener) {
        this.source = source;
        this.listener = listener;
        this.ignoringEvents = false;
    }

    public static StopListeningSound create(SoundInstance source, SoundStopListener listener) {
        return source instanceof TickableSoundInstance ? new TickableStopListeningSound((TickableSoundInstance) source, listener) : new StopListeningSound(source, listener);
    }

    public void stopListening() {
        this.ignoringEvents = true;
    }

    @Override
    public SoundInstance getParent() {
        return this.source;
    }

    @Override
    public Identifier getId() {
        return this.source.getId();
    }

    @Nullable
    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return this.source.getSoundSet(soundManager);
    }

    @Override
    public Sound getSound() {
        return this.source.getSound();
    }

    @Override
    public SoundCategory getCategory() {
        return this.source.getCategory();
    }

    @Override
    public boolean isRepeatable() {
        return this.source.isRepeatable();
    }

    @Override
    public boolean isRelative() {
        return this.source.isRelative();
    }

    @Override
    public int getRepeatDelay() {
        return this.source.getRepeatDelay();
    }

    @Override
    public float getVolume() {
        return this.source.getVolume();
    }

    @Override
    public float getPitch() {
        return this.source.getPitch();
    }

    @Override
    public double getX() {
        return this.source.getX();
    }

    @Override
    public double getY() {
        return this.source.getY();
    }

    @Override
    public double getZ() {
        return this.source.getZ();
    }

    @Override
    public AttenuationType getAttenuationType() {
        return this.source.getAttenuationType();
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return this.source.shouldAlwaysPlay();
    }

    @Override
    public boolean canPlay() {
        return this.source.canPlay();
    }

    public CompletableFuture<AudioStream> getAudioStream(SoundLoader soundBuffers, Identifier id, boolean looping) {
        return this.source.getAudioStream(soundBuffers, id, looping);
    }

    @Override
    public void onStop() {
        if (!this.ignoringEvents) {
            this.listener.onStop();
        }
    }
}

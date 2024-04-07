package gg.moonflower.etched.api.sound;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;

/**
 * Wrapper for {@link SoundInstance} that respects {@link SoundStopListener} and {@link TickableSoundInstance}.
 *
 * @author Ocelot
 */
public class TickableStopListeningSound extends StopListeningSound implements TickableSoundInstance {

    private final TickableSoundInstance tickableSource;

    TickableStopListeningSound(TickableSoundInstance source, SoundStopListener listener) {
        super(source, listener);
        this.tickableSource = source;
    }

    @Override
    public boolean isDone() {
        return this.tickableSource.isDone();
    }

    @Override
    public void tick() {
        this.tickableSource.tick();
    }
}

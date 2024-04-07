package gg.moonflower.etched.api.sound;

import net.minecraft.client.sound.AudioStream;

public interface SoundStreamModifier {

    AudioStream modifyStream(AudioStream stream);
}

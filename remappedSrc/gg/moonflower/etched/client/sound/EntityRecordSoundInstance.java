package gg.moonflower.etched.client.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * @author Ocelot
 */
public class EntityRecordSoundInstance extends MovingSoundInstance {

    private final Entity entity;

    public EntityRecordSoundInstance(SoundEvent soundEvent, Entity entity) {
        super(soundEvent, SoundCategory.RECORDS, SoundInstance.createRandom());
        this.volume = 4.0F;
        this.entity = entity;
    }

    @Override
    public void tick() {
        if (!this.entity.isAlive()) {
            this.setDone();
        } else {
            this.x = this.entity.getX();
            this.y = this.entity.getY();
            this.z = this.entity.getZ();
        }
    }
}

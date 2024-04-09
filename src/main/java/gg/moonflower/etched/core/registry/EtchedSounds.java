package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.core.Etched;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class EtchedSounds {

    //public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Etched.MOD_ID);

    public static final SoundEvent UI_ETCHER_TAKE_RESULT = registerSound("ui.etching_table.take_result");

    private static SoundEvent registerSound(String id) {
        var full_id = new ResourceLocation(Etched.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, full_id, SoundEvent.createVariableRangeEvent(full_id));
    }
}

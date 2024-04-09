package gg.moonflower.etched.core.fabric;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

import gg.moonflower.etched.core.Etched;

public class EtchedFabric implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        Etched.init();
    }
}

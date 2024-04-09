package gg.moonflower.etched.core;

import com.tterrag.registrate.Registrate;
import gg.moonflower.etched.api.sound.download.SoundSourceManager;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.sound.download.BandcampSource;
import gg.moonflower.etched.common.sound.download.SoundCloudSource;
import gg.moonflower.etched.core.quilt.EtchedConfig;
import gg.moonflower.etched.core.registry.*;

public class Etched {

    public static final String MOD_ID = "etched";
    public static final Registrate REGISTRATE = Registrate.create(MOD_ID);

    public Etched() {

    }

    public static void init() {
        //i guess following is needed to preload classes before registration or they will
        // not be registered
        EtchedTags.register();
        EtchedMessages.init();
        EtchedBlocks.register();
        EtchedEntities.register();
        EtchedItems.register();
        EtchedMenus.register();
        EtchedRecipes.register();
        EtchedSounds.register();
        REGISTRATE.register();
        EtchedConfig config = EtchedConfig.INSTANCE;
        SoundSourceManager.registerSource(new SoundCloudSource());
        SoundSourceManager.registerSource(new BandcampSource());
    }
}

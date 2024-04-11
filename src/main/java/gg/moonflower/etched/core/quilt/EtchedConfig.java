package gg.moonflower.etched.core.quilt;

import gg.moonflower.etched.core.Etched;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.loader.api.config.v2.QuiltConfig;


public class EtchedConfig extends ReflectiveConfig {
    public static final EtchedConfig INSTANCE = QuiltConfig.create(Etched.MOD_ID, Etched.MOD_ID, EtchedConfig.class);

    public final Server SERVER = new Server();
    public final Client CLIENT = new Client();
    public class Server extends ReflectiveConfig.Section {
        @Comment("Disables right clicking music discs into boomboxes and allows the menu to be used by shift right-clicking.")
        public final TrackedValue<Boolean> useBoomboxMenu = this.value(false );

        @Comment("Disables right clicking music discs into album covers and allows the menu to be used by shift right-clicking")
        public final TrackedValue<Boolean> useAlbumCoverMenu = this.value(false);

    }
    public class Client extends ReflectiveConfig.Section {
        @Comment("Displays note particles appear above jukeboxes while a record is playing.")
        public final TrackedValue<Boolean> showNotes = this.value(true );

        @Comment("Always plays tracks in stereo even when in-world.")
        public final TrackedValue<Boolean> forceStereo = this.value(false);

        @Comment("Use this token to stream audio from vk. ")
        public final TrackedValue<String> vkAudioToken = this.value("");

    }
}

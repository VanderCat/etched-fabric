package gg.moonflower.etched.core;

import com.tterrag.registrate.Registrate;
import gg.moonflower.etched.api.sound.download.SoundSourceManager;
import gg.moonflower.etched.client.screen.*;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import gg.moonflower.etched.common.item.BoomboxItem;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.sound.download.BandcampSource;
import gg.moonflower.etched.common.sound.download.SoundCloudSource;
import gg.moonflower.etched.core.registry.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class Etched {

    public static final String MOD_ID = "etched";
    public static final EtchedConfig.Client CLIENT_CONFIG;
    public static final EtchedConfig.Server SERVER_CONFIG;
    private static final ForgeConfigSpec clientSpec;
    private static final ForgeConfigSpec serverSpec;
    public static final Registrate REGISTRATE = Registrate.create(MOD_ID);

    static {
        Pair<EtchedConfig.Client, ForgeConfigSpec> clientConfig = new ForgeConfigSpec.Builder().configure(EtchedConfig.Client::new);
        clientSpec = clientConfig.getRight();
        CLIENT_CONFIG = clientConfig.getLeft();

        Pair<EtchedConfig.Server, ForgeConfigSpec> serverConfig = new ForgeConfigSpec.Builder().configure(EtchedConfig.Server::new);
        serverSpec = serverConfig.getRight();
        SERVER_CONFIG = serverConfig.getLeft();
    }

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
        SoundSourceManager.registerSource(new SoundCloudSource());
        SoundSourceManager.registerSource(new BandcampSource());
    }
}

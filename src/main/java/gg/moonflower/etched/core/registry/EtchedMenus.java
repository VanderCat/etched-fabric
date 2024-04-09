package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.client.screen.AlbumCoverScreen;
import gg.moonflower.etched.client.screen.AlbumJukeboxScreen;
import gg.moonflower.etched.client.screen.BoomboxScreen;
import gg.moonflower.etched.client.screen.EtchingScreen;
import gg.moonflower.etched.client.screen.RadioScreen;
import gg.moonflower.etched.common.menu.*;
import gg.moonflower.etched.core.Etched;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import com.tterrag.registrate.util.entry.MenuEntry;

public class EtchedMenus {

    //public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Etched.MOD_ID);

    public static final MenuEntry<EtchingMenu> ETCHING_MENU = 
        Etched.REGISTRATE.menu(
            "etching_table", 
            (type, windowId, inv) -> new EtchingMenu(windowId, inv), 
            ()->EtchingScreen::new
        )
        .register();
    //REGISTRY.register("etching_table", () -> new MenuType<>(EtchingMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuEntry<AlbumJukeboxMenu> ALBUM_JUKEBOX_MENU = 
        Etched.REGISTRATE.menu(
            "album_jukebox", 
            (type, windowId, inv) ->  new AlbumJukeboxMenu(windowId, inv), 
            ()->AlbumJukeboxScreen::new
        )
        .register();;
    public static final MenuEntry<BoomboxMenu> BOOMBOX_MENU = 
        Etched.REGISTRATE.menu(
            "boombox", 
            (type, windowId, inv) ->  new BoomboxMenu(windowId, inv), 
            ()->BoomboxScreen::new
        )
        .register();
    //REGISTRY.register("boombox", () -> new MenuType<>(BoomboxMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuEntry<AlbumCoverMenu> ALBUM_COVER_MENU = 
        Etched.REGISTRATE.menu(
            "album_cover", 
            (type, windowId, inv) ->  new AlbumCoverMenu(windowId, inv), 
            ()->AlbumCoverScreen::new
        )
        .register();
    //REGISTRY.register("album_cover", () -> new MenuType<>(AlbumCoverMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuEntry<RadioMenu> RADIO_MENU = 
        Etched.REGISTRATE.menu(
            "album_cover", 
            (type, windowId, inv) ->  new RadioMenu(windowId, inv), 
            ()->RadioScreen::new
        )
        .register();
    //REGISTRY.register("radio", () -> new MenuType<>(RadioMenu::new, FeatureFlags.VANILLA_SET));
    public static void register() {}
}

package gg.moonflower.etched.core.registry;

import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import gg.moonflower.etched.client.screen.AlbumCoverScreen;
import gg.moonflower.etched.client.screen.AlbumJukeboxScreen;
import gg.moonflower.etched.client.screen.BoomboxScreen;
import gg.moonflower.etched.client.screen.EtchingScreen;
import gg.moonflower.etched.client.screen.RadioScreen;
import gg.moonflower.etched.common.menu.*;
import gg.moonflower.etched.core.Etched;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import com.tterrag.registrate.util.entry.MenuEntry;

public class EtchedMenus {

    //public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Etched.MOD_ID);

    public static final MenuType<EtchingMenu> ETCHING_MENU =
            register("etching_table", EtchingMenu::new);

    //register("etching_table", EtchingMenu::new, ()->EtchingScreen::new);
    //REGISTRY.register("etching_table", () -> new MenuType<>(EtchingMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuType<AlbumJukeboxMenu> ALBUM_JUKEBOX_MENU =
            register("album_jukebox", AlbumJukeboxMenu::new);
    public static final MenuType<BoomboxMenu> BOOMBOX_MENU =
            register("boombox", BoomboxMenu::new);
    //REGISTRY.register("boombox", () -> new MenuType<>(BoomboxMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuType<AlbumCoverMenu> ALBUM_COVER_MENU =
            register("album_cover", AlbumCoverMenu::new);
    //REGISTRY.register("album_cover", () -> new MenuType<>(AlbumCoverMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuType<RadioMenu> RADIO_MENU =
            register("radio", RadioMenu::new);
    //REGISTRY.register("radio", () -> new MenuType<>(RadioMenu::new, FeatureFlags.VANILLA_SET));
    public static void register() {}

    public static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> supplier) {
        return Registry.register(BuiltInRegistries.MENU, new ResourceLocation(Etched.MOD_ID, name), new MenuType<T>(supplier, FeatureFlags.VANILLA_SET));
    }
}

package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.common.item.*;
import gg.moonflower.etched.core.Etched;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

public class EtchedItems {

    //public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Etched.MOD_ID);

    public static final Item MUSIC_LABEL = register("music_label", () -> new MusicLabelItem(new Item.Properties()));
    public static final Item COMPLEX_MUSIC_LABEL = register("complex_music_label", () -> new ComplexMusicLabelItem(new Item.Properties()));
    public static final Item BLANK_MUSIC_DISC = register("blank_music_disc", () -> new BlankMusicDiscItem(new Item.Properties()));
    public static final Item ETCHED_MUSIC_DISC = register("etched_music_disc", () -> new EtchedMusicDiscItem(new Item.Properties().stacksTo(1)));
    public static final Item JUKEBOX_MINECART = register("jukebox_minecart", () -> new MinecartJukeboxItem(new Item.Properties().stacksTo(1)));
    public static final Item BOOMBOX = register("boombox", () -> new BoomboxItem(new Item.Properties().stacksTo(1)));
    public static final Item ALBUM_COVER = register("album_cover", () -> new AlbumCoverItem(new Item.Properties().stacksTo(1)));


    public static <R extends Item> R register(String id, Supplier<R> item) {
        var mod_id = new ResourceLocation(Etched.MOD_ID, id);
        var item_a = item.get();
        return Registry.register(BuiltInRegistries.ITEM, mod_id, item_a);
    }
}

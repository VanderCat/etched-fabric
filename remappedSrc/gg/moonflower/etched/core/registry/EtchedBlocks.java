package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.common.block.AlbumJukeboxBlock;
import gg.moonflower.etched.common.block.EtchingTableBlock;
import gg.moonflower.etched.common.block.RadioBlock;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.common.blockentity.RadioBlockEntity;
import gg.moonflower.etched.common.item.PortalRadioItem;
import gg.moonflower.etched.core.Etched;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class EtchedBlocks {

    public static final RegistryKey<Registry<Block>> BLOCK = RegistryKey.ofRegistry(new Identifier("etched_blocks"));
    public static final RegistryKey<Registry<BlockEntity>> BLOCK_ENTITIES = RegistryKey.ofRegistry(new Identifier("etched_block_entities"));

    public static final RegistryObject<Block> ETCHING_TABLE = registerWithItem("etching_table", () -> new EtchingTableBlock(AbstractBlock.Settings.create().mapColor(MapColor.SPRUCE_BROWN).strength(2.5F).sounds(BlockSoundGroup.WOOD)), new Item.Settings());
    public static final RegistryObject<Block> ALBUM_JUKEBOX = registerWithItem("album_jukebox", () -> new AlbumJukeboxBlock(AbstractBlock.Settings.copy(Blocks.JUKEBOX)), new Item.Settings());
    public static final RegistryObject<Block> RADIO = registerWithItem("radio", () -> new RadioBlock(AbstractBlock.Settings.copy(Blocks.JUKEBOX).nonOpaque()), new Item.Settings());
    public static final RegistryObject<Item> PORTAL_RADIO_ITEM = EtchedItems.REGISTRY.register("portal_radio", () -> new PortalRadioItem(RADIO.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<AlbumJukeboxBlockEntity>> ALBUM_JUKEBOX_BE = BLOCK_ENTITIES.register("album_jukebox", () -> BlockEntityType.Builder.of(AlbumJukeboxBlockEntity::new, ALBUM_JUKEBOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<RadioBlockEntity>> RADIO_BE = BLOCK_ENTITIES.register("radio", () -> BlockEntityType.Builder.of(RadioBlockEntity::new, RADIO.get()).build(null));

    /**
     * Registers a block with a simple item.
     *
     * @param id         The id of the block
     * @param block      The block to register
     * @param properties The properties of the item to register
     * @param <R>        The type of block being registered
     * @return The registered block
     */
    private static <R extends Block> RegistryObject<R> registerWithItem(String id, Supplier<R> block, Item.Settings properties) {
        return registerWithItem(id, block, object -> new BlockItem(object, properties));
    }

    /**
     * Registers a block with an item.
     *
     * @param id          The id of the block
     * @param block       The block to register
     * @param itemFactory The factory to create a new item from the registered block
     * @param <R>         The type of block being registered
     * @return The registered block
     */
    private static <R extends Block> RegistryObject<R> registerWithItem(String id, Supplier<R> block, Function<R, Item> itemFactory) {
        RegistryObject<R> register = BLOCKS.register(id, block);
        EtchedItems.REGISTRY.register(id, () -> itemFactory.apply(register.get()));
        return register;
    }
}

package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.core.Etched;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class EtchedTags {

    public static final TagKey<Block> AUDIO_PROVIDER = BlockTags.of(new Identifier(Etched.MOD_ID, "audio_providers"));
}

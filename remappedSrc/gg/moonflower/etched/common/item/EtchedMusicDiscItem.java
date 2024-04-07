package gg.moonflower.etched.common.item;

import gg.moonflower.etched.api.record.PlayableRecordItem;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.core.Etched;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * @author Ocelot
 */
public class EtchedMusicDiscItem extends PlayableRecordItem {

    public EtchedMusicDiscItem(Settings properties) {
        super(properties);
    }

    @Override
    public Optional<TrackData[]> getMusic(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || (!nbt.contains("Music", NbtElement.COMPOUND_TYPE) && !nbt.contains("Music", NbtElement.LIST_TYPE))) {
            return Optional.empty();
        }

        if (nbt.contains("Music", NbtElement.LIST_TYPE)) {
            NbtList musicNbt = nbt.getList("Music", NbtElement.COMPOUND_TYPE);
            TrackData[] data = new TrackData[musicNbt.size()];

            int valid = 0;
            for (int i = 0; i < musicNbt.size(); i++) {
                NbtCompound trackNbt = musicNbt.getCompound(i);
                if (TrackData.isValid(trackNbt)) {
                    Optional<TrackData> optional = TrackData.CODEC.parse(NbtOps.INSTANCE, trackNbt).result();
                    if (optional.isPresent()) {
                        data[valid++] = optional.get();
                    }
                }
            }

            if (valid == 0) {
                return Optional.empty();
            }
            if (valid >= data.length) {
                return Optional.of(data);
            }

            TrackData[] result = new TrackData[valid];
            System.arraycopy(data, 0, result, 0, result.length);
            return Optional.of(result);
        }

        return TrackData.isValid(nbt.getCompound("Music")) ? TrackData.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("Music")).result().map(track -> new TrackData[]{track}) : Optional.empty();
    }

    @Override
    public Optional<TrackData> getAlbum(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("Album", NbtElement.COMPOUND_TYPE) && !nbt.contains("Music", NbtElement.LIST_TYPE)) {
            return this.getMusic(stack).filter(data -> data.length > 0).map(data -> data[0]);
        }
        return TrackData.isValid(nbt.getCompound("Album")) ? TrackData.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("Album")).result() : Optional.empty();
    }

    @Override
    public int getTrackCount(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || (!nbt.contains("Music", NbtElement.COMPOUND_TYPE) && !nbt.contains("Music", NbtElement.LIST_TYPE))) {
            return 0;
        }

        if (nbt.contains("Music", NbtElement.LIST_TYPE)) {
            NbtList musicNbt = nbt.getList("Music", NbtElement.COMPOUND_TYPE);

            int valid = 0;
            for (int i = 0; i < musicNbt.size(); i++) {
                NbtCompound trackNbt = musicNbt.getCompound(i);
                if (TrackData.isValid(trackNbt)) {
                    valid++;
                }
            }

            return valid;
        }

        return TrackData.isValid(nbt.getCompound("Music")) ? 1 : 0;
    }

    /**
     * Retrieves the label pattern from the specified stack.
     *
     * @param stack The stack to get the pattern from
     * @return The pattern for that item
     */
    public static LabelPattern getPattern(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("Pattern", NbtElement.NUMBER_TYPE)) {
            return LabelPattern.FLAT;
        }
        int id = nbt.getByte("Pattern");
        return id < 0 || id >= LabelPattern.values().length ? LabelPattern.FLAT : LabelPattern.values()[id];
    }

    /**
     * Retrieves the color of the physical disc from the specified stack.
     *
     * @param stack The stack to get the color from
     * @return The color for the physical disc
     */
    public static int getDiscColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return 0x515151;
        }

        // Convert old colors
        if (nbt.contains("PrimaryColor", NbtElement.NUMBER_TYPE)) {
            nbt.putInt("DiscColor", nbt.getInt("PrimaryColor"));
            nbt.remove("PrimaryColor");
        }

        if (!nbt.contains("DiscColor", NbtElement.NUMBER_TYPE)) {
            return 0x515151;
        }
        return nbt.getInt("DiscColor");
    }

    /**
     * Retrieves the primary color of the label from the specified stack.
     *
     * @param stack The stack to get the color from
     * @return The color for the label
     */
    public static int getLabelPrimaryColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return 0xFFFFFF;
        }

        // Convert old colors
        NbtCompound labelTag = nbt.getCompound("LabelColor");
        if (nbt.contains("SecondaryColor", NbtElement.NUMBER_TYPE)) {
            labelTag.putInt("Primary", nbt.getInt("SecondaryColor"));
            labelTag.putInt("Secondary", nbt.getInt("SecondaryColor"));
            nbt.put("LabelColor", labelTag);
            nbt.remove("SecondaryColor");
        }

        return labelTag.contains("Primary", NbtElement.NUMBER_TYPE) ? labelTag.getInt("Primary") : 0xFFFFFF;
    }

    /**
     * Retrieves the secondary color of the label from the specified stack.
     *
     * @param stack The stack to get the color from
     * @return The color for the label
     */
    public static int getLabelSecondaryColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return 0xFFFFFF;
        }

        // Convert old colors
        NbtCompound labelTag = nbt.getCompound("LabelColor");
        if (nbt.contains("SecondaryColor", NbtElement.NUMBER_TYPE)) {
            labelTag.putInt("Primary", nbt.getInt("SecondaryColor"));
            labelTag.putInt("Secondary", nbt.getInt("SecondaryColor"));
            nbt.put("LabelColor", labelTag);
            nbt.remove("SecondaryColor");
        }

        return labelTag.contains("Secondary", NbtElement.NUMBER_TYPE) ? labelTag.getInt("Secondary") : 0xFFFFFF;
    }

    /**
     * Sets the URL for the specified stack.
     *
     * @param stack  The stack to set NBT for
     * @param tracks The tracks to apply to the disk. If more than one are provided, the first is treated as the album data
     */
    public static void setMusic(ItemStack stack, TrackData... tracks) {
        if (tracks.length == 0) {
            stack.removeSubNbt("Music");
            stack.removeSubNbt("Album");
        } else if (tracks.length == 1) {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put("Music", tracks[0].save(new NbtCompound()));
            nbt.remove("Album");
        } else {
            NbtList musicNbt = new NbtList();
            for (int i = 1; i < tracks.length; i++) {
                musicNbt.add(tracks[i].save(new NbtCompound()));
            }
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put("Music", musicNbt);
            nbt.put("Album", tracks[0].save(new NbtCompound()));
        }
    }

    /**
     * Sets the pattern for the specified stack.
     *
     * @param stack   The stack to set NBT for
     * @param pattern The pattern to apply to the disk or <code>null</code> to remove and default to {@link LabelPattern#FLAT}
     */
    public static void setPattern(ItemStack stack, @Nullable LabelPattern pattern) {
        if (pattern == null) {
            stack.removeSubNbt("Pattern");
        } else {
            stack.getOrCreateNbt().putByte("Pattern", (byte) pattern.ordinal());
        }
    }

    /**
     * Sets the color for the specified stack.
     *
     * @param stack          The stack to set NBT for
     * @param primaryColor   The color to use for the physical disk
     * @param secondaryColor The color to use for the label
     */
    public static void setColor(ItemStack stack, int discColor, int primaryColor, int secondaryColor) {
        NbtCompound tag = stack.getOrCreateNbt();
        tag.putInt("DiscColor", discColor);

        NbtCompound labelTag = tag.getCompound("LabelColor");
        labelTag.putInt("Primary", primaryColor);
        labelTag.putInt("Secondary", secondaryColor);
        tag.put("LabelColor", labelTag);
    }

    /**
     * @author Jackson
     */
    public enum LabelPattern {

        FLAT, CROSS, EYE, PARALLEL, STAR, GOLD(true);

        private final boolean simple;
        private final Pair<Identifier, Identifier> textures;

        LabelPattern() {
            this(false);
        }

        LabelPattern(boolean simple) {
            this.simple = simple;

            String name = this.name().toLowerCase(Locale.ROOT);
            this.textures = Pair.of(
                    new Identifier(Etched.MOD_ID, "textures/item/" + name + "_label" + (simple ? "" : "_top") + ".png"),
                    new Identifier(Etched.MOD_ID, "textures/item/" + name + "_label" + (simple ? "" : "_bottom") + ".png")
            );
        }

        /**
         * @return A pair of {@link Identifier} for a top and bottom texture. If the pattern is simple, both locations are the same.
         */
        public Pair<Identifier, Identifier> getTextures() {
            return this.textures;
        }

        /**
         * @return Whether the label pattern supports two colors.
         */
        public boolean isSimple() {
            return this.simple;
        }

        /**
         * @return Whether this label can be colored
         */
        public boolean isColorable() {
            return this != GOLD;
        }
    }
}

package gg.moonflower.etched.api.record;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.moonflower.etched.core.Etched;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

/**
 * Information about track metadata for discs
 *
 * @param url The URL for the track
 * @param artist The name of the artist
 * @param title The title of the track
 * @author Ocelot
 * @since 2.0.0
 */
public record TrackData(String url, String artist, Text title) {

    public static final TrackData EMPTY = new TrackData(null, "Unknown", Text.literal("Custom Music"));
    public static final Codec<TrackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("Url").forGetter(TrackData::url),
            Codec.STRING.optionalFieldOf("Author", EMPTY.artist()).forGetter(TrackData::artist),
            Codec.STRING.optionalFieldOf("Title", Text.Serializer.toJson(EMPTY.title())).<Text>xmap(json -> {
                if (!json.startsWith("{")) {
                    return Text.literal(json);
                }
                try {
                    return Text.Serializer.fromJson(json);
                } catch (JsonParseException e) {
                    return Text.literal(json);
                }
            }, Text.Serializer::toJson).forGetter(TrackData::title)
    ).apply(instance, TrackData::new));

    private static final Pattern RESOURCE_LOCATION_PATTERN = Pattern.compile("[a-z0-9_.-]+");

    public static boolean isValid(NbtCompound nbt) {
        return nbt.contains("Url", NbtElement.STRING_TYPE) && isValidURL(nbt.getString("Url"));
    }

    /**
     * Checks to see if the specified string is a valid music URL.
     *
     * @param url The text to check
     * @return Whether the data is valid
     */
    public static boolean isValidURL(@Nullable String url) {
        if (url == null) {
            return false;
        }
        if (isLocalSound(url)) {
            return true;
        }
        try {
            String scheme = new URI(url).getScheme();
            return "http".equals(scheme) || "https".equals(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Checks to see if the specified URL is a resource location sound.
     *
     * @param url The url to check
     * @return Whether that sound can be played as a local sound event
     */
    public static boolean isLocalSound(@Nullable String url) {
        if (url == null) {
            return false;
        }
        String[] parts = url.split(":");
        if (parts.length > 2) {
            return false;
        }
        for (String part : parts) {
            if (!RESOURCE_LOCATION_PATTERN.matcher(part).matches()) {
                return false;
            }
        }
        return true;
    }

    public NbtCompound save(NbtCompound nbt) {
        if (this.url != null) {
            nbt.putString("Url", this.url);
        }
        if (this.title != null) {
            nbt.putString("Title", Text.Serializer.toJson(this.title));
        }
        if (this.artist != null) {
            nbt.putString("Author", this.artist);
        }
        return nbt;
    }

    public TrackData withUrl(String url) {
        return new TrackData(url, this.artist, this.title);
    }

    public TrackData withArtist(String artist) {
        return new TrackData(this.url, artist, this.title);
    }

    public TrackData withTitle(String title) {
        return new TrackData(this.url, this.artist, Text.literal(title));
    }

    public TrackData withTitle(Text title) {
        return new TrackData(this.url, this.artist, title);
    }

    /**
     * @return The name to show as the record title
     */
    public Text getDisplayName() {
        return Text.translatable("sound_source." + Etched.MOD_ID + ".info", this.artist, this.title);
    }
}

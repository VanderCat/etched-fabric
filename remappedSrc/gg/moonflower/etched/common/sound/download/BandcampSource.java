package gg.moonflower.etched.common.sound.download;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.download.SoundDownloadSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.api.util.ProgressTrackingInputStream;
import gg.moonflower.etched.core.Etched;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.JsonHelper;

/**
 * @author Ocelot
 */
public class BandcampSource implements SoundDownloadSource {

    private static final Pattern DATA_PATTERN = Pattern.compile("data-tralbum=\"([^\"]+)\"");
    private static final Text BRAND = Text.translatable("sound_source." + Etched.MOD_ID + ".bandcamp").styled(style -> style.withColor(TextColor.fromRgb(0x477987)));

    private final Map<String, Boolean> validCache = new WeakHashMap<>();

    private InputStream get(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy) throws IOException {
        HttpURLConnection httpURLConnection;
        if (progressListener != null) {
            progressListener.progressStartRequest(Text.translatable("sound_source." + Etched.MOD_ID + ".requesting", this.getApiName()));
        }

        try {
            URL uRL = new URL(url);
            httpURLConnection = (HttpURLConnection) uRL.openConnection(proxy);
            httpURLConnection.setInstanceFollowRedirects(true);
            Map<String, String> map = SoundDownloadSource.getDownloadHeaders();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            long size = httpURLConnection.getContentLengthLong();
            int response = httpURLConnection.getResponseCode();
            if (response != 200) {
                throw new IOException(response + " " + httpURLConnection.getResponseMessage());
            }

            return size != -1 && progressListener != null ? new ProgressTrackingInputStream(httpURLConnection.getInputStream(), size, progressListener) : httpURLConnection.getInputStream();
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    private <T> T resolve(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy, SourceRequest<T> function) throws IOException, JsonParseException {
        try (InputStream stream = this.get(url, progressListener, proxy)) {
            Matcher dataMatcher = DATA_PATTERN.matcher(IOUtils.toString(stream, StandardCharsets.UTF_8));
            String raw = dataMatcher.find() ? dataMatcher.group(1) : null;
            if (raw == null) {
                throw new IOException("Failed to find properties");
            }

            JsonObject json = JsonParser.parseString(StringEscapeUtils.unescapeHtml4(raw)).getAsJsonObject();
            String type = JsonHelper.getString(JsonHelper.getObject(json, "current"), "type");
            if (!"track".equals(type) && !"album".equals(type)) {
                throw new IOException("URL is not a track or album");
            }

            return function.process(json);
        }
    }

    @Nullable
    private String getTrackUrl(JsonObject fileJson) {
        if (fileJson.has("mp3-128")) {
            return JsonHelper.getString(fileJson, "mp3-128");
        }
        return null;
    }

    @Override
    public List<URL> resolveUrl(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy) throws IOException {
        return this.resolve(url, progressListener, proxy, json -> {
            if (progressListener != null) {
                progressListener.progressStartRequest(RESOLVING_TRACKS);
            }
            JsonArray trackInfoArray = JsonHelper.getArray(json, "trackinfo");
            List<URL> trackUrls = new ArrayList<>(trackInfoArray.size());
            for (int i = 0; i < trackInfoArray.size(); i++) {
                JsonObject trackInfoJson = JsonHelper.asObject(trackInfoArray.get(i), "trackinfo[" + i + "]");
                JsonObject fileJson = JsonHelper.getObject(trackInfoJson, "file");
                String trackUrl = this.getTrackUrl(fileJson);
                if (trackUrl != null) {
                    trackUrls.add(new URL(trackUrl));
                }
            }
            return trackUrls;
        });
    }

    @Override
    public List<TrackData> resolveTracks(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy) throws IOException, JsonParseException {
        return this.resolve(url, progressListener, proxy, json -> {
            int urlEnd = url.indexOf(".com/");
            if (urlEnd == -1) {
                urlEnd = url.length() - 4;
            }
            JsonObject current = JsonHelper.getObject(json, "current");
            String artist = StringEscapeUtils.unescapeHtml4(JsonHelper.getString(json, "artist"));
            String title = StringEscapeUtils.unescapeHtml4(JsonHelper.getString(current, "title"));
            String type = JsonHelper.getString(current, "type");
            if ("album".equals(type)) {
                JsonArray trackInfoJson = JsonHelper.getArray(json, "trackinfo");
                List<TrackData> tracks = new ArrayList<>(trackInfoJson.size());
                tracks.add(new TrackData(url, artist, Text.literal(title)));
                for (int i = 0; i < trackInfoJson.size(); i++) {
                    JsonObject trackJson = JsonHelper.asObject(trackInfoJson.get(i), "trackinfo[" + i + "]");
                    String trackUrl = url.substring(0, urlEnd + 4) + JsonHelper.getString(trackJson, "title_link");
                    String trackArtist = trackJson.has("artist") && !trackJson.get("artist").isJsonNull() ? StringEscapeUtils.unescapeHtml4(JsonHelper.getString(trackJson, "artist", artist)) : artist;
                    String trackTitle = StringEscapeUtils.unescapeHtml4(JsonHelper.getString(trackJson, "title"));

                    tracks.add(new TrackData(trackUrl, trackArtist, Text.literal(trackTitle)));
                }
                return tracks;
            }
            return Collections.singletonList(new TrackData(url, artist, Text.literal(title)));
        });
    }

    @Override
    public Optional<String> resolveAlbumCover(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy, ResourceManager resourceManager) throws IOException {
        return this.resolve(url, progressListener, proxy, json -> {
            JsonObject current = JsonHelper.getObject(json, "current");
            if (!current.has("art_id") || current.get("art_id").isJsonNull()) {
                return Optional.empty();
            }
            return Optional.of("https://f4.bcbits.com/img/a" + current.get("art_id") + "_1.jpg");
        });
    }

    @Override
    public boolean isValidUrl(String url) {
        return this.validCache.computeIfAbsent(url, key -> {
            try {
                String host = new URI(key).getHost();
                return host != null && host.endsWith("bandcamp.com");
            } catch (URISyntaxException e) {
                return false;
            }
        });
    }

    @Override
    public boolean isTemporary(String url) {
        return true;
    }

    @Override
    public String getApiName() {
        return "Bandcamp";
    }

    @Override
    public Optional<Text> getBrandText(String url) {
        return Optional.of(BRAND);
    }
}

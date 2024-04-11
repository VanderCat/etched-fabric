package gg.moonflower.etched.common.sound.download;

import com.google.gson.*;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.download.SoundDownloadSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.api.util.M3uParser;
import gg.moonflower.etched.api.util.ProgressTrackingInputStream;
import gg.moonflower.etched.common.sound.download.vk.*;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.quilt.EtchedConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VkMusicSource implements SoundDownloadSource  {

    private static final Component BRAND = Component.translatable("sound_source." + Etched.MOD_ID + ".vk").withStyle(style -> style.withColor(TextColor.fromRgb(0x477987)));

    public static final Logger LOGGER = LogManager.getLogger("Etched/Sources/Vk");
    private URL getVkApiUrl(String method, Map<String, String> query) {
        var token = EtchedConfig.INSTANCE.CLIENT.vkAudioToken.value();
        if(token != null) query.put("access_token", token);
        ArrayList<String> queryStr = new ArrayList<String>();
        query.forEach((key, value) -> {
            var kv = "";
            kv+=key;
            kv+="=";
            kv+=value;
            queryStr.add(kv);
        });
        var str = String.join("&", queryStr);
        URL url;
        try {
            url = new URL("https://api.vk.com/method/"+method+"?"+str);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    private String getIdFromUrl(String url) {
        return url.replace("https://vk.com/audio", "");
    }
    private InputStream get(URL uRL, @Nullable DownloadProgressListener progressListener, Proxy proxy, int attempt) throws IOException {
        HttpURLConnection httpURLConnection;
        if (progressListener != null) {
            progressListener.progressStartRequest(Component.translatable("sound_source." + Etched.MOD_ID + ".requesting", this.getApiName()));
        }

        try {
            httpURLConnection = (HttpURLConnection) uRL.openConnection(proxy);
            httpURLConnection.setInstanceFollowRedirects(true);
            Map<String, String> map = SoundDownloadSource.getDownloadHeaders();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            int response = httpURLConnection.getResponseCode();

            long size = httpURLConnection.getContentLengthLong();
            if (response != 200) {
                throw new IOException(httpURLConnection.getResponseMessage());
            }

            return size != -1 && progressListener != null ? new ProgressTrackingInputStream(httpURLConnection.getInputStream(), size, progressListener) : httpURLConnection.getInputStream();
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
    private <T> T resolve(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy, VkSourceRequest<T> function) throws IOException, JsonParseException {
        var query = new HashMap<String, String>();
        query.put("audios", getIdFromUrl(url));
        query.put("v", "5.95");
        URL uRL = getVkApiUrl("audio.getById", query);
        try (InputStreamReader reader = new InputStreamReader(this.get(uRL, progressListener, proxy, 0))) {
            var parser = JsonParser.parseReader(reader);
            LOGGER.info(parser.getAsJsonObject().toString());
            VkResponse response = new Gson().fromJson(parser, VkResponse.class);
            if (response.error != null) {
                throw new IOException("Vk returned an error: "+response.error.error_msg);
            }
            return function.process(response.response);
        }
    }
    @Override
    public List<URL> resolveUrl(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy) throws IOException {
        return new ArrayList<URL>(List.of(new URL[]{new URL(url)}));
    }

    @Override
    public List<TrackData> resolveTracks(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy) throws IOException, JsonParseException {
        return this.resolve(url, progressListener, proxy, trackInfos -> {
            if (progressListener != null) {
                progressListener.progressStartRequest(RESOLVING_TRACKS);
            }
            var list = new ArrayList<TrackData>();
            for (VkTrackInfo track: trackInfos) {
                list.add(new TrackData(track.url.toString(), track.artist, Component.literal(track.title)));
            }
            return list;
        });
    }

    @Override
    public Optional<String> resolveAlbumCover(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy, ResourceManager resourceManager) throws IOException {
        return Optional.empty();
    }

    @Override
    public boolean isValidUrl(String url) {
        return url.startsWith("https://vk.com/audio") || url.startsWith("http://vk.com/audio");
    }

    @Override
    public boolean isTemporary(String url) {
        return true;
    }

    @Override
    public String getApiName() {
        return "VKontakte";
    }

    @Override
    public Optional<Component> getBrandText(String url) {
        return Optional.of(BRAND);
    }
}

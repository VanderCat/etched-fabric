package gg.moonflower.etched.common.sound.download.vk;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface VkSourceRequest<T> {

    T process(List<VkTrackInfo> trackInfos) throws IOException;
}
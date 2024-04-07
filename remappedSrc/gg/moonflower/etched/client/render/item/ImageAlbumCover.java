package gg.moonflower.etched.client.render.item;

import gg.moonflower.etched.api.record.AlbumCover;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ImageAlbumCover(NativeImage image) implements AlbumCover {
}

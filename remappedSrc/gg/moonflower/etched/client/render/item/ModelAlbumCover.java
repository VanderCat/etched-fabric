package gg.moonflower.etched.client.render.item;

import gg.moonflower.etched.api.record.AlbumCover;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ModelAlbumCover(ModelIdentifier model) implements AlbumCover {
}

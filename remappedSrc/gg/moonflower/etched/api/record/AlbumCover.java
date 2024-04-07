package gg.moonflower.etched.api.record;

import gg.moonflower.etched.client.render.item.ImageAlbumCover;
import gg.moonflower.etched.client.render.item.ModelAlbumCover;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

/**
 * Defines a cover texture for an album cover item.
 *
 * @author Ocelot
 * @since 2.0.0
 */
public interface AlbumCover {

    AlbumCover EMPTY = new AlbumCover() {
    };

    /**
     * Creates a cover with an image. This will be turned into a model and rendered when ready.
     *
     * @param image The image to use
     * @return The cover instance
     */
    static AlbumCover of(NativeImage image) {
        return new ImageAlbumCover(image);
    }

    /**
     * Creates a cover with a static model location.
     *
     * @param location The model to use
     * @return The cover instance
     */
    static AlbumCover of(ModelIdentifier location) {
        return new ModelAlbumCover(location);
    }

    /**
     * Creates a cover with a static model location.
     *
     * @param location The model to use. The "inventory" variant is used
     * @return The cover instance
     */
    static AlbumCover of(Identifier location) {
        return new ModelAlbumCover(new ModelIdentifier(location, "inventory"));
    }
}

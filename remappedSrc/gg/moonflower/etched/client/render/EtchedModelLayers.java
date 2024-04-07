package gg.moonflower.etched.client.render;

import gg.moonflower.etched.core.Etched;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class EtchedModelLayers {

    public static final EntityModelLayer JUKEBOX_MINECART = create("jukebox_minecart");

    public static EntityModelLayer create(String model) {
        return create(model, "main");
    }

    public static EntityModelLayer create(String model, String layer) {
        return new EntityModelLayer(new Identifier(Etched.MOD_ID, model), layer);
    }
}

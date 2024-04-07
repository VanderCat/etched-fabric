package gg.moonflower.etched.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

public class JukeboxMinecartRenderer<T extends AbstractMinecartEntity> extends MinecartEntityRenderer<T> {

    public JukeboxMinecartRenderer(EntityRendererFactory.Context context) {
        super(context, EtchedModelLayers.JUKEBOX_MINECART);
    }
}

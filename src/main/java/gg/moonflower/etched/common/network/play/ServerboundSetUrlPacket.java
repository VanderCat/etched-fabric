package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.handler.EtchedServerPlayPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * @param url The URL to set in the etching table
 * @author Jackson
 */
@ApiStatus.Internal
public record ServerboundSetUrlPacket(String url) implements EtchedPacket {

    public ServerboundSetUrlPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeUtf(this.url);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SERVER_SET_URL;
    }
}

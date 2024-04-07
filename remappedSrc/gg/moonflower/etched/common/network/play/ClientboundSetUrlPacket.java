package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.play.handler.EtchedClientPlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * @param url The URL to set in the etching table
 * @author Ocelot
 */
@ApiStatus.Internal
public record ClientboundSetUrlPacket(String url) implements EtchedPacket {

    public ClientboundSetUrlPacket(@Nullable String url) {
        this.url = url != null ? url : "";
    }

    public ClientboundSetUrlPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    @Override
    public void writePacketData(PacketByteBuf buf) {
        buf.writeString(this.url);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        EtchedClientPlayPacketHandler.handleSetUrl(this, ctx);
    }
}

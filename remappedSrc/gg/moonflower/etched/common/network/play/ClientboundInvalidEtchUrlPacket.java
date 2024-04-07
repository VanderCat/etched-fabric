package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.play.handler.EtchedClientPlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * @param exception The exception to set in the etching table
 * @author Jackson
 */
@ApiStatus.Internal
public record ClientboundInvalidEtchUrlPacket(String exception) implements EtchedPacket {

    public ClientboundInvalidEtchUrlPacket(PacketByteBuf buf) {
        this(buf.readString());
    }

    @Override
    public void writePacketData(PacketByteBuf buf) {
        buf.writeString(this.exception);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        EtchedClientPlayPacketHandler.handleSetInvalidEtch(this, ctx);
    }
}

package gg.moonflower.etched.common.network.play;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

/**
 * A message intended for the specified message handler.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public interface EtchedPacket {

    /**
     * Writes the raw message data to the data stream.
     *
     * @param buf The buffer to write to
     */
    void writePacketData(FriendlyByteBuf buf) throws IOException;
}

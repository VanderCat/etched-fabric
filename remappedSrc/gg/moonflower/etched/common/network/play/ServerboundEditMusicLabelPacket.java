package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.play.handler.EtchedServerPlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * @param slot   The slot the music label is in
 * @param author The new author
 * @param title  The new title
 */
@ApiStatus.Internal
public record ServerboundEditMusicLabelPacket(int slot, String author, String title) implements EtchedPacket {

    public ServerboundEditMusicLabelPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readString(128), buf.readString(128));
    }

    @Override
    public void writePacketData(PacketByteBuf buf) {
        buf.writeVarInt(this.slot);
        buf.writeString(this.author, 128);
        buf.writeString(this.title, 128);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        EtchedServerPlayPacketHandler.handleEditMusicLabel(this, ctx);
    }
}

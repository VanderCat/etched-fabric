package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.handler.EtchedServerPlayPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * @param slot   The slot the music label is in
 * @param author The new author
 * @param title  The new title
 */
@ApiStatus.Internal
public record ServerboundEditMusicLabelPacket(int slot, String author, String title) implements EtchedPacket {

    public ServerboundEditMusicLabelPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readUtf(128), buf.readUtf(128));
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeVarInt(this.slot);
        buf.writeUtf(this.author, 128);
        buf.writeUtf(this.title, 128);
    }
    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SERVER_EDIT_MUSIC_LABEL;
    }
}

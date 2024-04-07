package gg.moonflower.etched.common.network.play.handler;

import gg.moonflower.etched.common.item.SimpleMusicLabelItem;
import gg.moonflower.etched.common.menu.AlbumJukeboxMenu;
import gg.moonflower.etched.common.menu.EtchingMenu;
import gg.moonflower.etched.common.menu.RadioMenu;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ServerboundEditMusicLabelPacket;
import gg.moonflower.etched.common.network.play.ServerboundSetUrlPacket;
import gg.moonflower.etched.common.network.play.SetAlbumJukeboxTrackPacket;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EtchedServerPlayPacketHandler {

    public static void handleSetUrl(ServerboundSetUrlPacket pkt, NetworkEvent.Context ctx) {
        PlayerEntity player = ctx.getSender();
        if (player == null) {
            return;
        }

        if (player.currentScreenHandler instanceof EtchingMenu menu) {
            ctx.enqueueWork(() -> menu.setUrl(pkt.url()));
        } else if (player.currentScreenHandler instanceof RadioMenu menu) {
            ctx.enqueueWork(() -> menu.setUrl(pkt.url()));
        }
    }

    public static void handleEditMusicLabel(ServerboundEditMusicLabelPacket pkt, NetworkEvent.Context ctx) {
        int slot = pkt.slot();
        if (!PlayerInventory.isValidHotbarIndex(slot) && slot != 40) {
            return;
        }

        ServerPlayerEntity player = ctx.getSender();
        if (player == null) {
            return;
        }

        ItemStack labelStack = player.getInventory().getStack(slot);
        if (!labelStack.isOf(EtchedItems.MUSIC_LABEL.get())) {
            return;
        }

        ctx.enqueueWork(() -> {
            SimpleMusicLabelItem.setTitle(labelStack, StringUtils.normalizeSpace(pkt.title()));
            SimpleMusicLabelItem.setAuthor(labelStack, StringUtils.normalizeSpace(pkt.author()));
        });
    }

    public static void handleSetAlbumJukeboxTrack(SetAlbumJukeboxTrackPacket pkt, NetworkEvent.Context ctx) {
        ServerPlayerEntity player = ctx.getSender();
        if (player == null) {
            return;
        }

        if (player.currentScreenHandler instanceof AlbumJukeboxMenu menu) {
            ctx.enqueueWork(() -> {
                ServerLevel level = player.serverLevel();
                if (menu.setPlayingTrack(level, pkt)) {
                    EtchedMessages.PLAY.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(menu.getPos())), new SetAlbumJukeboxTrackPacket(pkt.playingIndex(), pkt.track()));
                }
            });
        }
    }
}

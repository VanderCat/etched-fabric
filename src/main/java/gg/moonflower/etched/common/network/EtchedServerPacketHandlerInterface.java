package gg.moonflower.etched.common.network;

import gg.moonflower.etched.common.network.play.EtchedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface EtchedServerPacketHandlerInterface<MSG extends EtchedPacket> {
    public void handle(MSG packet, MinecraftServer client, ServerPlayer player);
}

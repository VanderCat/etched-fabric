package gg.moonflower.etched.common.network;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import gg.moonflower.etched.common.network.play.EtchedPacket;
import net.minecraft.client.Minecraft;

@ClientOnly
@FunctionalInterface
public interface EtchedClientPacketHandlerInterface<MSG extends EtchedPacket> {
    public void handle(MSG packet, Minecraft client);
}

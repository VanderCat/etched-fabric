package gg.moonflower.etched.core.fabric;

import gg.moonflower.etched.common.entity.MinecartJukebox;
import gg.moonflower.etched.core.EtchedClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class EtchedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EtchedClient.init();
        EtchedClient.postInit();

        ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
            if (result.getType() == HitResult.Type.ENTITY && player.getAbilities().creativeMode) {
                Entity entity = ((EntityHitResult) result).getEntity();
                if (entity instanceof MinecartJukebox minecart) {
                    return new ItemStack(minecart.getItem());
                }
            }
            return ItemStack.EMPTY;
        });
    }
}

package gg.moonflower.etched.core.quilt;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

//import gg.moonflower.etched.common.entity.MinecartJukebox;
import gg.moonflower.etched.core.EtchedClient;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.ClientCommandManager;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;
// getString(ctx, "string")
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
// word()
import static com.mojang.brigadier.arguments.StringArgumentType.word;
// literal("foo")
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
// argument("bar", word())

public class EtchedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer mod) {
        EtchedClient.registerItemGroups();
        
        ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
            if (result.getType() == HitResult.Type.ENTITY && player.getAbilities().instabuild) {
                Entity entity = ((EntityHitResult) result).getEntity();
                //FIXME
                //if (entity instanceof MinecartJukebox minecart) {
                //    return new ItemStack(minecart.getDropItem());
                //}
            }
            return ItemStack.EMPTY;
        });
        EtchedClient.registerItemColors();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        ClientCommandManager.literal("etched_token").then(
                                ClientCommandManager.literal("get").executes(context -> {
                                    context.getSource().sendFeedback(
                                            Component.translatable("command.etched.token.vk",
                                                    Component.translatable("command.etched.token.vk.here")
                                                            .withStyle(ChatFormatting.AQUA)
                                                            .setStyle(Style.EMPTY.withClickEvent(
                                                                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://oauth.vk.com/authorize?client_id=6121396&scope=1&redirect_uri=https://oauth.vk.com/blank.html&display=page&response_type=token&revoke=1&redirect_uri=close.html")))));
                                    return 1;
                                })
                        ).then(
                                ClientCommandManager.literal("set_url").then(ClientCommandManager.argument("url", StringArgumentType.greedyString())).executes(context -> {
                                   var url = context.getArgument("url", String.class);
                                    var p = Pattern.compile("(?:https://oauth.vk.com/close.html#access_token=)([^\s]*)(?:&exp)");
                                    var matcher = p.matcher(url);
                                    if (!matcher.find())
                                        context.getSource().sendFeedback(Component.translatable("command.etched.token.vk.error"));
                                    EtchedConfig.INSTANCE.CLIENT.vkAudioToken.setValue(matcher.group(0), true);
                                    return 1;
                                })
                        ).then(
                                ClientCommandManager.literal("set").then(ClientCommandManager.argument("url", StringArgumentType.string())).executes(context -> {
                                    var url = context.getArgument("url", String.class);
                                    var p = Pattern.compile("(?:https://oauth.vk.com/close.html#access_token=)([^\s]*)(?:&expires)");
                                    var matcher = p.matcher(url);
                                    if (!matcher.find())
                                        context.getSource().sendFeedback(Component.translatable("command.etched.token.vk.error"));
                                    EtchedConfig.INSTANCE.CLIENT.vkAudioToken.setValue(matcher.group(0), true);
                                    return 1;
                                })
                        )
                )
        );
    }
}

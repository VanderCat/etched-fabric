package gg.moonflower.etched.common.item;

import gg.moonflower.etched.client.screen.EditMusicLabelScreen;
import gg.moonflower.etched.core.Etched;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SimpleMusicLabelItem extends Item {

    public SimpleMusicLabelItem(Settings properties) {
        super(properties);
    }

    public static String getAuthor(ItemStack stack) {
        if (!(stack.getItem() instanceof SimpleMusicLabelItem)) {
            return "";
        }
        return stack.getOrCreateSubNbt("Label").getString("Author");
    }

    public static String getTitle(ItemStack stack) {
        if (!(stack.getItem() instanceof SimpleMusicLabelItem)) {
            return "";
        }
        return stack.getOrCreateSubNbt("Label").getString("Title");
    }

    public static void setAuthor(ItemStack stack, String author) {
        if (!(stack.getItem() instanceof SimpleMusicLabelItem)) {
            return;
        }

        NbtCompound tag = stack.getOrCreateSubNbt("Label");
        tag.putString("Author", author);
    }

    public static void setTitle(ItemStack stack, String title) {
        if (!(stack.getItem() instanceof SimpleMusicLabelItem)) {
            return;
        }

        NbtCompound tag = stack.getOrCreateSubNbt("Label");
        tag.putString("Title", title);
    }

    @ClientOnly
    private void openMusicLabelEditScreen(PlayerEntity player, Hand hand, ItemStack stack) {
        MinecraftClient.getInstance().setScreen(new EditMusicLabelScreen(player, hand, stack));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (level.isClient()) {
            this.openMusicLabelEditScreen(player, hand, stack);
        }
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(stack, level.isClient());
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity entity, int i, boolean bl) {
        if (getAuthor(itemStack).isEmpty()) {
            setAuthor(itemStack, entity.getDisplayName().getString());
        }
        if (getTitle(itemStack).isEmpty()) {
            setTitle(itemStack, "Custom Music");
        }
    }

    @Override
    public void appendTooltip(ItemStack itemStack, @Nullable World level, List<Text> list, TooltipContext tooltipFlag) {
        if (!getAuthor(itemStack).isEmpty() && !getTitle(itemStack).isEmpty()) {
            list.add(Text.translatable("sound_source." + Etched.MOD_ID + ".info", getAuthor(itemStack), getTitle(itemStack)).formatted(Formatting.GRAY));
        }
    }
}

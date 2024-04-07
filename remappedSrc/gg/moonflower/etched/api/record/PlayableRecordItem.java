package gg.moonflower.etched.api.record;

import gg.moonflower.etched.api.sound.download.SoundSourceManager;
import gg.moonflower.etched.core.Etched;
import org.jetbrains.annotations.Nullable;

import java.net.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.resource.ResourceManager;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public abstract class PlayableRecordItem extends Item implements PlayableRecord {

    private static final Text ALBUM = Text.translatable("item." + Etched.MOD_ID + ".etched_music_disc.album").formatted(Formatting.DARK_GRAY);

    public PlayableRecordItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.isOf(Blocks.JUKEBOX) || state.get(JukeboxBlock.HAS_RECORD)) {
            return ActionResult.PASS;
        }

        ItemStack stack = context.getStack();
        if (this.getMusic(stack).isEmpty()) {
            return ActionResult.PASS;
        }

        if (!level.isClient()) {
            PlayerEntity player = context.getPlayer();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof JukeboxBlockEntity jukeboxblockentity) {
                jukeboxblockentity.setStack(stack.copy());
                level.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));
            }

            stack.decrement(1);
            if (player != null) {
                player.incrementStat(Stats.PLAY_RECORD);
            }
        }

        return ActionResult.success(level.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> list, TooltipContext tooltipFlag) {
        this.getAlbum(stack).ifPresent(track -> {
            boolean album = this.getTrackCount(stack) > 1;
            list.add(track.getDisplayName().copy().formatted(Formatting.GRAY));
            SoundSourceManager.getBrandText(track.url())
                    .map(component -> Text.literal("  ").append(component.copy()))
                    .map(component -> album ? component.append(" ").append(ALBUM) : component)
                    .ifPresentOrElse(list::add, () -> {
                        if (album) {
                            list.add(ALBUM);
                        }
                    });
        });
    }

    @Override
    public CompletableFuture<AlbumCover> getAlbumCover(ItemStack stack, Proxy proxy, ResourceManager resourceManager) {
        return this.getAlbum(stack).map(data -> SoundSourceManager.resolveAlbumCover(data.url(), null, proxy, resourceManager)).orElseGet(() -> CompletableFuture.completedFuture(AlbumCover.EMPTY));
    }
}

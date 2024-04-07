package gg.moonflower.etched.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.common.menu.AlbumJukeboxMenu;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.SetAlbumJukeboxTrackPacket;
import gg.moonflower.etched.core.Etched;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * @author Ocelot
 */
public class AlbumJukeboxScreen extends HandledScreen<AlbumJukeboxMenu> {

    private static final Identifier CONTAINER_LOCATION = new Identifier("textures/gui/container/dispenser.png");
    private static final Text NOW_PLAYING = Text.translatable("screen." + Etched.MOD_ID + ".album_jukebox.now_playing").formatted(Formatting.YELLOW);

    private int playingIndex;
    private int playingTrack;

    public AlbumJukeboxScreen(AlbumJukeboxMenu dispenserMenu, PlayerInventory inventory, Text component) {
        super(dispenserMenu, inventory, component);
    }

    private void update(boolean next) {
        ClientWorld level = this.client.world;
        if (level == null || !this.handler.isInitialized()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(this.handler.getPos());
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity albumJukebox) || !((AlbumJukeboxBlockEntity) blockEntity).isPlaying()) {
            return;
        }

        int oldIndex = albumJukebox.getPlayingIndex();
        int oldTrack = albumJukebox.getTrack();
        if (next) {
            albumJukebox.next();
        } else {
            albumJukebox.previous();
        }

        if (((albumJukebox.getPlayingIndex() == oldIndex && albumJukebox.getTrack() != oldTrack) || albumJukebox.recalculatePlayingIndex(!next)) && albumJukebox.getPlayingIndex() != -1) {
            SoundTracker.playAlbum(albumJukebox, albumJukebox.getCachedState(), level, this.handler.getPos(), true);
            EtchedMessages.PLAY.sendToServer(new SetAlbumJukeboxTrackPacket(albumJukebox.getPlayingIndex(), albumJukebox.getTrack()));
        }
    }

    @Override
    protected void init() {
        super.init();

        int buttonPadding = 6;
        Text last = Text.literal("Last");
        Text next = Text.literal("Next");
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        this.addDrawableChild(ButtonWidget.builder(last, b -> this.update(false)).dimensions(this.x + 7 + (54 - font.getWidth(last)) / 2 - buttonPadding, this.y + 33, font.getWidth(last) + 2 * buttonPadding, 20).build());
        this.addDrawableChild(ButtonWidget.builder(next, b -> this.update(true)).dimensions(this.x + 115 + (54 - font.getWidth(last)) / 2 - buttonPadding, this.y + 33, font.getWidth(next) + 2 * buttonPadding, 20).build());
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.drawMouseoverTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int guiLeft = (this.width - this.backgroundWidth) / 2;
        int guiTop = (this.height - this.backgroundHeight) / 2;
        guiGraphics.drawTexture(CONTAINER_LOCATION, guiLeft, guiTop, 0, 0, this.backgroundWidth, this.backgroundHeight);

        this.playingIndex = -1;
        this.playingTrack = 0;
        ClientWorld level = this.client.world;
        if (level == null || !this.handler.isInitialized()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(this.handler.getPos());
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity)) {
            return;
        }

        this.playingIndex = ((AlbumJukeboxBlockEntity) blockEntity).getPlayingIndex();
        this.playingTrack = ((AlbumJukeboxBlockEntity) blockEntity).getTrack();
        if (this.playingIndex != -1) {
            int x = this.playingIndex % 3;
            int y = this.playingIndex / 3;
            guiGraphics.fillGradient(guiLeft + 62 + x * 18, guiTop + 17 + y * 18, guiLeft + 78 + x * 18, guiTop + 33 + y * 18, 0x3CF6FF00, 0x3CF6FF00);
        }
    }

    @Override
    protected List<Text> getTooltipFromItem(ItemStack stack) {
        List<Text> tooltip = super.getTooltipFromItem(stack);

        if (this.focusedSlot != null) {
            if (this.focusedSlot.id == this.playingIndex) {
                if (this.playingTrack >= 0 && PlayableRecord.getStackTrackCount(stack) > 0) {
                    Optional<TrackData[]> optional = PlayableRecord.getStackMusic(stack).filter(tracks -> this.playingTrack < tracks.length);
                    if (optional.isPresent()) {
                        TrackData track = optional.get()[this.playingTrack];
                        tooltip.add(NOW_PLAYING.copy().append(": ").append(track.getDisplayName()).append(" (" + (this.playingTrack + 1) + "/" + optional.get().length + ")"));
                    } else {
                        tooltip.add(NOW_PLAYING);
                    }
                } else {
                    tooltip.add(NOW_PLAYING);
                }
            }
        }

        return tooltip;
    }
}

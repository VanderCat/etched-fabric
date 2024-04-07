package gg.moonflower.etched.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.common.item.ComplexMusicLabelItem;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.common.item.MusicLabelItem;
import gg.moonflower.etched.common.menu.EtchingMenu;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ServerboundSetUrlPacket;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedItems;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * @author Jackson
 */
public class EtchingScreen extends HandledScreen<EtchingMenu> implements ScreenHandlerListener {

    private static final Identifier TEXTURE = new Identifier(Etched.MOD_ID, "textures/gui/container/etching_table.png");
    private static final Text INVALID_URL = Text.translatable("screen." + Etched.MOD_ID + ".etching_table.error.invalid_url");
    private static final Text CANNOT_CREATE = Text.translatable("screen." + Etched.MOD_ID + ".etching_table.error.cannot_create");
    private static final Text CANNOT_CREATE_MISSING_DISC = Text.translatable("screen." + Etched.MOD_ID + ".etching_table.error.cannot_create.missing_disc").formatted(Formatting.GRAY);
    private static final Text CANNOT_CREATE_MISSING_LABEL = Text.translatable("screen." + Etched.MOD_ID + ".etching_table.error.cannot_create.missing_label").formatted(Formatting.GRAY);

    private ItemStack discStack;
    private ItemStack labelStack;
    private TextFieldWidget url;
    private int urlTicks;
    private String oldUrl;
    private String invalidReason;
    private boolean displayLabels;

    public EtchingScreen(EtchingMenu menu, PlayerInventory inventory, Text component) {
        super(menu, inventory, component);
        this.backgroundHeight = 180;
        this.playerInventoryTitleY += 14;

        this.discStack = ItemStack.EMPTY;
        this.labelStack = ItemStack.EMPTY;

        this.invalidReason = "";
    }

    @Override
    protected void init() {
        super.init();
        this.url = new TextFieldWidget(this.textRenderer, this.x + 11, this.y + 25, 154, 16, this.url, Text.translatable("container." + Etched.MOD_ID + ".etching_table.url"));
        this.url.setEditableColor(-1);
        this.url.setUneditableColor(-1);
        this.url.setDrawsBackground(false);
        this.url.setMaxLength(32500);
        this.url.setChangedListener(s -> {
            if (!Objects.equals(this.oldUrl, s) && this.urlTicks <= 0) {
                EtchedMessages.PLAY.sendToServer(new ServerboundSetUrlPacket(""));
            }
            this.urlTicks = 8;
        });
        this.url.setFocusUnlocked(true);
        this.addSelectableChild(this.url);
        this.handler.addListener(this);
    }

    @Override
    public void handledScreenTick() {
        this.url.tick();
        if (this.urlTicks > 0) {
            this.urlTicks--;
            if (this.urlTicks <= 0 && !Objects.equals(this.oldUrl, this.url.getText())) {
                this.oldUrl = this.url.getText();
                EtchedMessages.PLAY.sendToServer(new ServerboundSetUrlPacket(this.url.getText()));
            }
        }
    }

    @Override
    public void onSlotUpdate(ScreenHandler abstractContainerMenu, int slot, ItemStack stack) {
        if (slot == 0) {
            if (this.discStack.isEmpty() && !stack.isEmpty()) {
                this.url.setText("");
            }
            PlayableRecord.getStackAlbum(stack).ifPresent(track -> this.url.setText(track.url()));
            this.discStack = stack;
        }

        if (slot == 1) {
            this.labelStack = stack;
        }

        boolean editable = this.discStack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get() || (!this.discStack.isEmpty() && !this.labelStack.isEmpty());
        this.url.setEditable(editable);
        this.url.setVisible(editable);
        this.url.setFocused(editable);
        this.setFocused(editable ? this.url : null);

        this.displayLabels = !this.discStack.isEmpty() && !this.labelStack.isEmpty();
    }

    @Override
    public void onPropertyUpdate(ScreenHandler abstractContainerMenu, int index, int value) {
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.url.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext guiGraphics, int x, int y) {
        super.drawMouseoverTooltip(guiGraphics, x, y);

        boolean isEtched = this.discStack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get();
        List<OrderedText> reasonLines = new ArrayList<>();
        if (!isEtched && !this.discStack.isEmpty() && this.labelStack.isEmpty()) {
            reasonLines.add(CANNOT_CREATE.asOrderedText());
            reasonLines.add(CANNOT_CREATE_MISSING_LABEL.asOrderedText());
        } else if (!isEtched && this.discStack.isEmpty() && !this.labelStack.isEmpty()) {
            reasonLines.add(CANNOT_CREATE.asOrderedText());
            reasonLines.add(CANNOT_CREATE_MISSING_DISC.asOrderedText());
        } else if ((!this.url.getText().isEmpty() && !TrackData.isValidURL(this.url.getText())) || !this.invalidReason.isEmpty()) {
            reasonLines.add(INVALID_URL.asOrderedText());
            if (!this.invalidReason.isEmpty()) {
                reasonLines.addAll(this.textRenderer.wrapLines(Text.literal(this.invalidReason).formatted(Formatting.GRAY), 200));
            }
        }

        if (x >= this.x + 83 && x < this.x + 110 && y >= this.y + 44 && y < this.y + 61) {
            guiGraphics.drawOrderedTooltip(this.textRenderer, reasonLines, x, y);
        }
    }

    @Override
    protected void drawBackground(DrawContext guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        if ((!this.url.getText().isEmpty() && !TrackData.isValidURL(this.url.getText())) || !this.invalidReason.isEmpty() || (this.discStack.getItem() != EtchedItems.ETCHED_MUSIC_DISC.get() && ((!this.discStack.isEmpty() && this.labelStack.isEmpty()) || (this.discStack.isEmpty() && !this.labelStack.isEmpty())))) {
            guiGraphics.drawTexture(TEXTURE, this.x + 83, this.y + 44, 0, 226, 27, 17);
        }

        guiGraphics.drawTexture(TEXTURE, this.x + 9, this.y + 21, 0, (this.discStack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get() || (!this.discStack.isEmpty() && !this.labelStack.isEmpty()) ? 180 : 196), 158, 16);

        if (this.displayLabels) {
            for (int index = 0; index < 6; index++) {
                int x = this.x + 46 + (index * 14);
                int y = this.y + 65;
                RenderSystem.setShaderTexture(0, TEXTURE);

                int u = index == this.handler.getLabelIndex() ? 14 : mouseX >= x && mouseY >= y && mouseX < x + 14 && mouseY < y + 14 ? 28 : 0;
                guiGraphics.drawTexture(TEXTURE, x, y, u, 212, 14, 14);
                this.renderLabel(guiGraphics, x, y, index);
            }
        }
    }

    // FIXME rewrite
    private void renderLabel(DrawContext guiGraphics, int x, int y, int index) {
        if (this.labelStack.isEmpty() || this.discStack.isEmpty()) {
            return;
        }

        EtchedMusicDiscItem.LabelPattern pattern = EtchedMusicDiscItem.LabelPattern.values()[index];
        int primaryLabelColor = 0xFFFFFF;
        int secondaryLabelColor = primaryLabelColor;
        if (this.labelStack.getItem() instanceof MusicLabelItem) {
            primaryLabelColor = MusicLabelItem.getLabelColor(this.labelStack);
            secondaryLabelColor = primaryLabelColor;
        } else if (this.labelStack.getItem() instanceof ComplexMusicLabelItem) {
            primaryLabelColor = ComplexMusicLabelItem.getPrimaryColor(this.labelStack);
            secondaryLabelColor = ComplexMusicLabelItem.getSecondaryColor(this.labelStack);
        }

        if (pattern.isColorable()) {
            RenderSystem.setShaderColor((float) (primaryLabelColor >> 16 & 255) / 255.0F, (float) (primaryLabelColor >> 8 & 255) / 255.0F, (float) (primaryLabelColor & 255) / 255.0F, 1.0F);
        }

        Pair<Identifier, Identifier> textures = pattern.getTextures();
        RenderSystem.setShaderTexture(0, textures.getLeft());

        guiGraphics.drawTexture(textures.getLeft(), x, y, 14, 14, 1, 1, 14, 14, 16, 16);
        if (!pattern.isSimple()) {
            if (pattern.isColorable()) {
                RenderSystem.setShaderColor((float) (secondaryLabelColor >> 16 & 255) / 255.0F, (float) (secondaryLabelColor >> 8 & 255) / 255.0F, (float) (secondaryLabelColor & 255) / 255.0F, 1.0F);
            }

            guiGraphics.drawTexture(textures.getRight(), x, y, 14, 14, 1, 1, 14, 14, 16, 16);
        }
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int i) {
        if (this.displayLabels) {
            for (int index = 0; index < 6; index++) {
                int x = this.x + 46 + (index * 14);
                int y = this.y + 65;

                if (mouseX >= x && mouseY >= y && mouseX < x + 14 && mouseY < y + 14 && this.handler.getLabelIndex() != index) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    this.client.interactionManager.clickButton(this.handler.syncId, index);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return this.url.keyPressed(i, j, k) || (this.url.isFocused() && this.url.isVisible() && i != 256) || super.keyPressed(i, j, k);
    }

    public void setReason(String exception) {
        this.invalidReason = exception;
    }
}

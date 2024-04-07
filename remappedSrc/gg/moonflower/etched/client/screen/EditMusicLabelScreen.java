package gg.moonflower.etched.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.moonflower.etched.common.item.ComplexMusicLabelItem;
import gg.moonflower.etched.common.item.MusicLabelItem;
import gg.moonflower.etched.common.item.SimpleMusicLabelItem;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ServerboundEditMusicLabelPacket;
import gg.moonflower.etched.core.Etched;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class EditMusicLabelScreen extends Screen {

    private static final Identifier TEXTURE = new Identifier(Etched.MOD_ID, "textures/gui/edit_music_label.png");
    private static final Identifier LABEL = new Identifier(Etched.MOD_ID, "textures/gui/label.png");
    private static final Text TITLE_COMPONENT = Text.translatable("screen.etched.edit_music_label.title");
    private static final Text AUTHOR_COMPONENT = Text.translatable("screen.etched.edit_music_label.author");

    private final PlayerEntity player;
    private final Hand hand;
    private final ItemStack labelStack;
    private final int imageWidth = 176;
    private final int imageHeight = 139;

    private ButtonWidget doneButton;
    private TextFieldWidget title;
    private TextFieldWidget author;

    public EditMusicLabelScreen(PlayerEntity player, Hand hand, ItemStack stack) {
        super(TITLE_COMPONENT);
        this.player = player;
        this.hand = hand;
        this.labelStack = stack;
    }

    @Override
    protected void init() {
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        this.doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.saveChanges();
            this.client.setScreen(null);
        }).dimensions(leftPos, topPos + this.imageHeight + 5, this.imageWidth, 20).build();
        this.addDrawableChild(this.doneButton);

        this.title = new TextFieldWidget(this.textRenderer, leftPos + 10, topPos + 91, 154, 10, TITLE_COMPONENT);
        this.title.setText(SimpleMusicLabelItem.getTitle(this.labelStack));
        this.title.setUneditableColor(-1);
        this.title.setEditableColor(-1);
        this.title.setMaxLength(128);
        this.title.setDrawsBackground(false);
        this.title.setFocusUnlocked(true);
        this.title.setFocused(true);
        this.setFocused(this.title);

        this.author = new TextFieldWidget(this.textRenderer, leftPos + 10, topPos + 121, 154, 10, AUTHOR_COMPONENT);
        this.author.setText(SimpleMusicLabelItem.getAuthor(this.labelStack));
        this.author.setUneditableColor(-1);
        this.author.setEditableColor(-1);
        this.author.setMaxLength(128);
        this.author.setDrawsBackground(false);
        this.author.setFocusUnlocked(true);

        this.title.setChangedListener(string -> {
            if ((this.author.getText().isEmpty() || string.isEmpty()) && this.doneButton.active) {
                this.doneButton.active = false;
            } else if ((!this.author.getText().isEmpty() && !string.isEmpty()) && !this.doneButton.active) {
                this.doneButton.active = true;
            }
        });
        this.addDrawableChild(this.title);

        this.author.setChangedListener(string -> {
            if ((this.title.getText().isEmpty() || string.isEmpty()) && this.doneButton.active) {
                this.doneButton.active = false;
            } else if ((!this.title.getText().isEmpty() && !string.isEmpty()) && !this.doneButton.active) {
                this.doneButton.active = true;
            }
        });
        this.addDrawableChild(this.author);
    }

    @Override
    public void resize(MinecraftClient minecraft, int i, int j) {
        String title = this.title.getText();
        String author = this.author.getText();

        boolean titleFocused = this.title.isFocused();
        boolean authorFocused = this.author.isFocused();
        Element focused = this.getFocused();

        this.init(minecraft, i, j);
        this.title.setText(title);
        this.title.setFocused(titleFocused);
        this.author.setText(author);
        this.author.setFocused(authorFocused);
        this.setFocused(focused);
    }

    @Override
    public void tick() {
        this.title.tick();
        this.author.tick();
    }

    protected void renderBg(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        graphics.drawTexture(TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        graphics.drawText(this.textRenderer, TITLE_COMPONENT, leftPos + 7, topPos + 77, 4210752, false);
        graphics.drawText(this.textRenderer, AUTHOR_COMPONENT, leftPos + 7, topPos + 77 + 30, 4210752, false);

        int primaryLabelColor = 0xFFFFFF;
        int secondaryLabelColor = primaryLabelColor;
        if (this.labelStack.getItem() instanceof MusicLabelItem) {
            primaryLabelColor = MusicLabelItem.getLabelColor(this.labelStack);
            secondaryLabelColor = primaryLabelColor;
        } else if (this.labelStack.getItem() instanceof ComplexMusicLabelItem) {
            primaryLabelColor = ComplexMusicLabelItem.getPrimaryColor(this.labelStack);
            secondaryLabelColor = ComplexMusicLabelItem.getSecondaryColor(this.labelStack);
        }

        RenderSystem.setShaderColor((float) (primaryLabelColor >> 16 & 255) / 255.0F, (float) (primaryLabelColor >> 8 & 255) / 255.0F, (float) (primaryLabelColor & 255) / 255.0F, 1.0F);
        graphics.drawTexture(LABEL, leftPos, topPos, 0, 0, this.imageWidth, 70);

        RenderSystem.setShaderColor((float) (secondaryLabelColor >> 16 & 255) / 255.0F, (float) (secondaryLabelColor >> 8 & 255) / 255.0F, (float) (secondaryLabelColor & 255) / 255.0F, 1.0F);
        graphics.drawTexture(LABEL, leftPos, topPos, 0, 70, this.imageWidth, 70);
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBg(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void saveChanges() {
        String author = this.author.getText().trim();
        String title = this.title.getText().trim();

        SimpleMusicLabelItem.setTitle(this.labelStack, title);
        SimpleMusicLabelItem.setAuthor(this.labelStack, author);

        int slot = this.hand == Hand.MAIN_HAND ? this.player.getInventory().selectedSlot : 40;
        EtchedMessages.PLAY.sendToServer(new ServerboundEditMusicLabelPacket(slot, author, title));
    }
}

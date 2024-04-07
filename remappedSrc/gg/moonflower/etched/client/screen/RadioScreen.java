package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.menu.RadioMenu;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ServerboundSetUrlPacket;
import gg.moonflower.etched.core.Etched;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * @author Ocelot
 */
public class RadioScreen extends HandledScreen<RadioMenu> {

    private static final Identifier TEXTURE = new Identifier(Etched.MOD_ID, "textures/gui/radio.png");

    private boolean canEdit;
    private TextFieldWidget url;

    public RadioScreen(RadioMenu menu, PlayerInventory inventory, Text component) {
        super(menu, inventory, component);
        this.backgroundHeight = 39;
    }

    @Override
    protected void init() {
        super.init();
        this.url = new TextFieldWidget(this.textRenderer, this.x + 10, this.y + 21, 154, 16, this.url, Text.translatable("container." + Etched.MOD_ID + ".radio.url"));
        this.url.setEditableColor(-1);
        this.url.setUneditableColor(-1);
        this.url.setDrawsBackground(false);
        this.url.setMaxLength(32500);
        this.url.setVisible(this.canEdit);
        this.url.setFocusUnlocked(false);
        this.addDrawableChild(this.url);
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            EtchedMessages.PLAY.sendToServer(new ServerboundSetUrlPacket(this.url.getText()));
            this.client.setScreen(null);
        }).dimensions((this.width - this.backgroundWidth) / 2, (this.height - this.backgroundHeight) / 2 + this.backgroundHeight + 5, this.backgroundWidth, 20).build());
    }

    @Override
    public void handledScreenTick() {
        this.url.tick();
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawBackground(DrawContext guiGraphics, float f, int mouseX, int mouseY) {
        guiGraphics.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        guiGraphics.drawTexture(TEXTURE, this.x + 8, this.y + 18, 0, this.canEdit ? 39 : 53, 160, 14);
    }

    @Override
    protected void drawForeground(DrawContext guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return this.url.keyPressed(i, j, k) || (this.url.isFocused() && this.url.isVisible() && i != 256) || super.keyPressed(i, j, k);
    }

    public void receiveUrl(String url) {
        this.canEdit = true;
        this.url.setVisible(true);
        this.url.setText(url);
        this.setFocused(this.url);
        this.url.setFocused(true);
    }
}

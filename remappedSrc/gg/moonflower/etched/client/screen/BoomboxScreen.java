package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.menu.BoomboxMenu;
import gg.moonflower.etched.core.Etched;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * @author Ocelot
 */
public class BoomboxScreen extends HandledScreen<BoomboxMenu> {

    private static final Identifier BOOMBOX_LOCATION = new Identifier(Etched.MOD_ID, "textures/gui/container/boombox.png");

    public BoomboxScreen(BoomboxMenu hopperMenu, PlayerInventory inventory, Text component) {
        super(hopperMenu, inventory, component);
        this.backgroundHeight = 133;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.drawMouseoverTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        guiGraphics.drawTexture(BOOMBOX_LOCATION, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}

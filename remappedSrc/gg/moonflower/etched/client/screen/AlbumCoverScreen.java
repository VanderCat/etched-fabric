package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.menu.AlbumCoverMenu;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * @author Ocelot
 */
public class AlbumCoverScreen extends HandledScreen<AlbumCoverMenu> {

    private static final Identifier CONTAINER_LOCATION = new Identifier("textures/gui/container/dispenser.png");

    public AlbumCoverScreen(AlbumCoverMenu hopperMenu, PlayerInventory inventory, Text component) {
        super(hopperMenu, inventory, component);
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
        guiGraphics.drawTexture(CONTAINER_LOCATION, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}

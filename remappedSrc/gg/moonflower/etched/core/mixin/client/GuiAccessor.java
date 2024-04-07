package gg.moonflower.etched.core.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGameHud.class)
public interface GuiAccessor {

    @Accessor
    Text getOverlayMessageString();

    @Accessor
    void setOverlayMessageTime(int overlayMessageTime);
}

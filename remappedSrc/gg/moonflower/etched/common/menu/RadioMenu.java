package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.core.registry.EtchedBlocks;
import gg.moonflower.etched.core.registry.EtchedMenus;
import java.util.function.Consumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

/**
 * @author Ocelot
 */
public class RadioMenu extends ScreenHandler {

    private final ScreenHandlerContext access;
    // Workaround for thread concurrency issues
    private final Consumer<String> urlConsumer;

    public RadioMenu(int id, PlayerInventory inventory) {
        this(id, inventory, ScreenHandlerContext.EMPTY, url -> {
        });
    }

    public RadioMenu(int id, PlayerInventory inventory, ScreenHandlerContext access, Consumer<String> containerLevelAccess) {
        super(EtchedMenus.RADIO_MENU.get(), id);
        this.access = access;
        this.urlConsumer = containerLevelAccess;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.access, player, EtchedBlocks.RADIO.get());
    }

    /**
     * Sets the URL for the resulting stack to the specified value.
     *
     * @param url The new URL
     */
    public void setUrl(String url) {
        this.urlConsumer.accept(url);
    }
}
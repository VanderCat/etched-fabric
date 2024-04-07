package gg.moonflower.etched.common.item;

import gg.moonflower.etched.common.block.RadioBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;

public class PortalRadioItem extends BlockItem {

    public PortalRadioItem(Block block, Settings properties) {
        super(block, properties);
    }

    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockState = this.getBlock().getPlacementState(context);
        if (blockState == null) {
            return null;
        }
        blockState = blockState.with(RadioBlock.PORTAL, true);
        return this.canPlace(context, blockState) ? blockState : null;
    }

    @Override
    public void appendBlocks(Map<Block, Item> map, Item item) {
    }

    @Override
    public String getTranslationKey() {
        return this.getOrCreateTranslationKey();
    }
}

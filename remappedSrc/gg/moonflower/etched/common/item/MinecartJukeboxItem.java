package gg.moonflower.etched.common.item;

import gg.moonflower.etched.common.entity.MinecartJukebox;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * @author Ocelot
 */
public class MinecartJukeboxItem extends Item {

    private static final DispenserBehavior DISPENSE_ITEM_BEHAVIOR = new ItemDispenserBehavior() {
        private final ItemDispenserBehavior defaultDispenseItemBehavior = new ItemDispenserBehavior();

        @Override
        public ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
            Direction direction = source.getBlockState().get(DispenserBlock.FACING);
            World level = source.getWorld();
            double d = source.getX() + (double) direction.getOffsetX() * 1.125D;
            double e = Math.floor(source.getY()) + (double) direction.getOffsetY();
            double f = source.getZ() + (double) direction.getOffsetZ() * 1.125D;
            BlockPos blockPos = source.getPos().offset(direction);
            BlockState blockState = level.getBlockState(blockPos);
            RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock ? blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double k;
            if (blockState.isIn(BlockTags.RAILS)) {
                if (railShape.isAscending()) {
                    k = 0.6D;
                } else {
                    k = 0.1D;
                }
            } else {
                if (!blockState.isAir() || !level.getBlockState(blockPos.down()).isIn(BlockTags.RAILS)) {
                    return this.defaultDispenseItemBehavior.dispense(source, stack);
                }

                BlockState blockState2 = level.getBlockState(blockPos.down());
                RailShape railShape2 = blockState2.getBlock() instanceof AbstractRailBlock ? blockState2.get(((AbstractRailBlock) blockState2.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railShape2.isAscending()) {
                    k = -0.4D;
                } else {
                    k = -0.9D;
                }
            }

            MinecartJukebox jukeboxMinecart = new MinecartJukebox(level, d, e + k, f);
            if (stack.hasCustomName()) {
                jukeboxMinecart.setCustomName(stack.getName());
            }

            level.spawnEntity(jukeboxMinecart);
            stack.decrement(1);
            return stack;
        }

        protected void playSound(BlockPointer blockSource) {
            blockSource.getWorld().syncWorldEvent(1000, blockSource.getPos(), 0);
        }
    };

    public MinecartJukeboxItem(Item.Settings properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext useOnContext) {
        World level = useOnContext.getWorld();
        BlockPos blockPos = useOnContext.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.isIn(BlockTags.RAILS)) {
            return ActionResult.FAIL;
        }

        ItemStack stack = useOnContext.getStack();
        if (!level.isClient()) {
            RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock ? blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0.0D;
            if (railShape.isAscending()) {
                d = 0.5D;
            }

            MinecartJukebox jukeboxMinecart = new MinecartJukebox(level, blockPos.getX() + 0.5D, blockPos.getY() + 0.0625D + d, blockPos.getZ() + 0.5D);
            if (stack.hasCustomName()) {
                jukeboxMinecart.setCustomName(stack.getName());
            }

            level.spawnEntity(jukeboxMinecart);
        }

        stack.decrement(1);
        return ActionResult.success(level.isClient);
    }
}

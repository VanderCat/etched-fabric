package gg.moonflower.etched.common.block;

import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.mixin.client.LevelRendererAccessor;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.level.block.*;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.Map;

/**
 * @author Ocelot
 */
public class AlbumJukeboxBlock extends BlockWithEntity {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty HAS_RECORD = Properties.HAS_RECORD;

    public AlbumJukeboxBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false).with(HAS_RECORD, false));
    }

    @Override
    public ActionResult onUse(BlockState blockState, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result) {
        if (level.isClient()) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AlbumJukeboxBlockEntity) {
            player.openHandledScreen((AlbumJukeboxBlockEntity) blockEntity);
        }
        // TODO: stats
        return ActionResult.CONSUME;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos())).with(HAS_RECORD, false);
    }

    @Override
    public void neighborUpdate(BlockState blockState, World level, BlockPos pos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClient()) {
            boolean bl2 = blockState.get(POWERED);
            if (bl2 != level.isReceivingRedstonePower(pos)) {
                level.setBlockState(pos, blockState.cycle(POWERED), 2);
                level.updateListeners(pos, blockState, level.getBlockState(pos), 3);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Inventory) {
                ItemScatterer.spawn(level, pos, (Inventory) blockEntity);
                level.updateComparators(pos, this);
            }
            level.syncWorldEvent(1011, pos, 0);

            super.onStateReplaced(state, level, pos, newState, moving);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World level, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(level.getBlockEntity(pos));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AlbumJukeboxBlockEntity(blockPos, blockState);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, HAS_RECORD);
    }

    @ClientOnly
    @Override
    public void randomDisplayTick(BlockState state, World level, BlockPos pos, Random random) {
        if (!Etched.CLIENT_CONFIG.showNotes.get() || !level.getBlockState(pos.up()).isAir()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity jukebox)) {
            return;
        }

        if (jukebox.getPlayingIndex() <= -1) {
            return;
        }

        MinecraftClient minecraft = MinecraftClient.getInstance();
        Map<BlockPos, SoundInstance> sounds = ((LevelRendererAccessor) minecraft.worldRenderer).getPlayingRecords();
        if (sounds.containsKey(pos) && minecraft.getSoundManager().isPlaying(sounds.get(pos))) {
            level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, random.nextInt(25) / 24D, 0, 0);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return checkType(blockEntityType, EtchedBlocks.ALBUM_JUKEBOX_BE.get(), AlbumJukeboxBlockEntity::tick);
    }
}

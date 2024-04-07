package gg.moonflower.etched.common.block;

import gg.moonflower.etched.common.blockentity.RadioBlockEntity;
import gg.moonflower.etched.common.menu.RadioMenu;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ClientboundSetUrlPacket;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.mixin.client.LevelRendererAccessor;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RadioBlock extends BlockWithEntity {

    public static final IntProperty ROTATION = Properties.ROTATION;
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty PORTAL = BooleanProperty.of("portal");
    private static final VoxelShape X_SHAPE = Block.createCuboidShape(5.0D, 0.0D, 2.0D, 11.0D, 8.0D, 14.0D);
    private static final VoxelShape Z_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 5.0D, 14.0D, 8.0D, 11.0D);
    private static final VoxelShape ROTATED_SHAPE = Block.createCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);
    private static final Text CONTAINER_TITLE = Text.translatable("container." + Etched.MOD_ID + ".radio");

    public RadioBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(ROTATION, 0).with(POWERED, false).with(PORTAL, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClient()) {
            return ActionResult.SUCCESS;
        }
        ItemStack stack = player.getStackInHand(interactionHand);
        if (stack.getItem() == Items.CAKE && !state.get(PORTAL)) {
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            level.setBlockState(pos, state.with(PORTAL, true), 3);
            return ActionResult.SUCCESS;
        }
        player.openHandledScreen(state.createScreenHandlerFactory(level, pos)).ifPresent(__ -> {
            String url = level.getBlockEntity(pos) instanceof RadioBlockEntity be ? be.getUrl() : "";
            EtchedMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new ClientboundSetUrlPacket(url));
        });
        return ActionResult.CONSUME;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState()
                .with(ROTATION, MathHelper.floor((double) ((180.0F + context.getPlayerYaw()) * 16.0F / 360.0F) + 0.5) & 15)
                .with(POWERED, context.getWorld().isReceivingRedstonePower(context.getBlockPos()));
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
            if (blockEntity instanceof RadioBlockEntity) {
                if (((RadioBlockEntity) blockEntity).isPlaying()) {
                    level.syncWorldEvent(1011, pos, 0);
                }
                Clearable.clear(blockEntity);
            }

            super.onStateReplaced(state, level, pos, newState, moving);
        }
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState blockState, World level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return new SimpleNamedScreenHandlerFactory((menuId, playerInventory, player) -> new RadioMenu(menuId, playerInventory, ScreenHandlerContext.create(level, blockPos), blockEntity instanceof RadioBlockEntity ? ((RadioBlockEntity) blockEntity)::setUrl : url -> {
        }), CONTAINER_TITLE);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView level, BlockPos pos, ShapeContext collisionContext) {
        int rotation = state.get(ROTATION);
        if (rotation % 8 == 0) {
            return Z_SHAPE;
        }
        if (rotation % 8 == 4) {
            return X_SHAPE;
        }
        return ROTATED_SHAPE;
    }

    @Override
    public boolean hasSidedTransparency(BlockState blockState) {
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(ROTATION, rotation.rotate(state.get(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(ROTATION, mirror.mirror(state.get(ROTATION), 16));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RadioBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, POWERED, PORTAL);
    }

    @Override
    public boolean canPathfindThrough(BlockState blockState, BlockView blockGetter, BlockPos blockPos, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public ItemStack getPickStack(BlockView level, BlockPos pos, BlockState state) {
        return new ItemStack(state.get(PORTAL) ? EtchedBlocks.PORTAL_RADIO_ITEM.get() : EtchedBlocks.RADIO.get());
    }

    @Override
    public void randomDisplayTick(BlockState state, World level, BlockPos pos, Random random) {
        if (!Etched.CLIENT_CONFIG.showNotes.get() || !level.getBlockState(pos.up()).isAir()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof RadioBlockEntity radio)) {
            return;
        }

        if (radio.getUrl() == null) {
            return;
        }

        MinecraftClient minecraft = MinecraftClient.getInstance();
        Map<BlockPos, SoundInstance> sounds = ((LevelRendererAccessor) minecraft.worldRenderer).getPlayingRecords();
        if (sounds.containsKey(pos) && minecraft.getSoundManager().isPlaying(sounds.get(pos))) {
            level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D, random.nextInt(25) / 24D, 0, 0);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return checkType(blockEntityType, EtchedBlocks.RADIO_BE.get(), RadioBlockEntity::tick);
    }
}

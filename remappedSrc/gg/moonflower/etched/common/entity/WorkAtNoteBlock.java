package gg.moonflower.etched.common.entity;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class WorkAtNoteBlock extends VillagerWorkTask {

    @Override
    protected void performAdditionalWork(ServerWorld level, VillagerEntity villager) {
        Optional<GlobalPos> optional = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
        if (optional.isPresent()) {
            GlobalPos globalPos = optional.get();
            BlockState blockState = level.getBlockState(globalPos.getPos());
            if (blockState.isOf(Blocks.NOTE_BLOCK)) {
                this.playNoteBlock(level, villager, globalPos, blockState);
            }
        }
    }

    private void playNoteBlock(ServerWorld level, VillagerEntity villager, GlobalPos globalPos, BlockState state) {
        BlockPos pos = globalPos.getPos();
        if (villager.getRandom().nextBoolean()) {
            state = state.cycle(NoteBlock.NOTE);
            level.setBlockState(pos, state, 3);
        }

        this.playNote(level, state.getBlock(), pos);
    }

    private void playNote(World level, Block block, BlockPos pos) {
        if (level.getBlockState(pos.up()).isAir()) {
            level.addSyncedBlockEvent(pos, block, 0, 0);
        }
    }

}


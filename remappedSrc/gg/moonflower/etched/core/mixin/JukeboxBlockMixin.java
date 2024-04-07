package gg.moonflower.etched.core.mixin;

import gg.moonflower.etched.api.record.PlayableRecord;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin extends BlockWithEntity {

    protected JukeboxBlockMixin(Settings properties) {
        super(properties);
    }

    @Inject(method = "getAnalogOutputSignal", at = @At("TAIL"), cancellable = true)
    public void getAnalogOutputSignal(BlockState state, World level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity be) {
            ItemStack record = be.getStack();
            if (!(record.getItem() instanceof MusicDiscItem) && record.getItem() instanceof PlayableRecord) {
                cir.setReturnValue(15);
            }
        }
    }
}

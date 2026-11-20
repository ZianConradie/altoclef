package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class WorldBlockModifiedMixin {

    private static boolean hasBlock(BlockState state, BlockPos pos) {
        return !state.isAir() && state.isRedstoneConductor(Minecraft.getInstance().level, pos);
    }

    @Inject(
            method = "setBlock",
            at = @At("HEAD")
    )
    public void onBlockWasChanged(BlockPos pos, BlockState newBlock, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> ci) {
        Level self = (Level) (Object) this;
        BlockState oldBlock = self.getBlockState(pos);
        if (!hasBlock(oldBlock, pos) && hasBlock(newBlock, pos)) {
            BlockPlaceEvent evt = new BlockPlaceEvent(pos, newBlock);
            EventBus.publish(evt);
        }
    }
}

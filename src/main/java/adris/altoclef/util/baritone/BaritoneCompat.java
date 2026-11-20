package adris.altoclef.util.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.utils.BlockOptionalMetaLookup;
import baritone.api.utils.IPlayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Stand-ins for the handful of Baritone <em>internal</em> helpers AltoClef used to call.
 *
 * <p>The helpers it replaces ({@code MovementHelper.isLava/isWater/canPlaceAgainst/avoidBreaking},
 * {@code MineProcess.searchWorld/plausibleToBreak}, {@code ToolSet.calculateSpeedVsBlock}) are
 * package-private or need a Baritone {@code CalculationContext}, so they are not reachable from
 * AltoClef. These implementations reproduce the behaviour AltoClef actually relies on using
 * vanilla APIs.
 */
public final class BaritoneCompat {

    private BaritoneCompat() {
    }

    /** Replaces {@code MovementHelper.isLava(BlockState)}. */
    public static boolean isLava(BlockState state) {
        return !state.isAir() && state.getFluidState().is(FluidTags.LAVA);
    }

    /** Replaces {@code MovementHelper.isWater(BlockState)}. */
    public static boolean isWater(BlockState state) {
        return !state.isAir() && state.getFluidState().is(FluidTags.WATER);
    }

    /**
     * Replaces {@code MovementHelper.canPlaceAgainst(IPlayerContext, BlockPos)}: a block may be
     * placed against {@code pos} when {@code pos} is a solid, occupied block.
     */
    public static boolean canPlaceAgainst(IPlayerContext ctx, BlockPos pos) {
        BlockState state = ctx.world().getBlockState(pos);
        return !state.isAir() && state.isSolid();
    }

    /**
     * Replaces {@code MovementHelper.avoidBreaking(BlockStateInterface, x, y, z, BlockState)}.
     *
     * <p>Baritone refuses to break blocks it cannot safely remove (air and unbreakable/protected
     * blocks). Falling-block support above the target is handled by the callers' own fall checks.
     */
    public static boolean avoidBreaking(Level world, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }
        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER || block == Blocks.COMMAND_BLOCK
                || block == Blocks.STRUCTURE_VOID) {
            return true;
        }
        return state.getDestroySpeed(world, pos) < 0;
    }

    /**
     * Replaces {@code MineProcess.plausibleToBreak(CalculationContext, BlockPos)}: is this block
     * something the current player could conceivably mine.
     */
    public static boolean plausibleToBreak(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        return state.getDestroySpeed(world, pos) >= 0;
    }

    /**
     * Replaces {@code ToolSet.calculateSpeedVsBlock(ItemStack, BlockState)}: vanilla destroy speed
     * of the stack against the state (Baritone's version additionally models enchantments and
     * status effects; the caller here only needs a relative ranking).
     */
    public static double calculateSpeedVsBlock(ItemStack stack, BlockState state) {
        return stack.getDestroySpeed(state);
    }

    /**
     * Replaces {@code MineProcess.searchWorld(IPlayerContext, BlockOptionalMetaLookup, max, ...)}
     * using Baritone's public world scanner.
     */
    public static List<BlockPos> searchWorld(IPlayerContext ctx, BlockOptionalMetaLookup lookup, int max) {
        BlockPos player = ctx.player().blockPosition();
        List<BlockPos> found = BaritoneAPI.getProvider().getWorldScanner()
                .scanChunkRadius(ctx, lookup, player.getX() >> 4, player.getZ() >> 4, 8);
        if (found.size() > max) {
            return found.subList(0, max);
        }
        return found;
    }
}

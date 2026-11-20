package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;

public class SearchChunkForBlockTask extends SearchChunksExploreTask {

    private final HashSet<Block> _toSearchFor = new HashSet<>();

    public SearchChunkForBlockTask(Block... blocks) {
        _toSearchFor.addAll(Arrays.asList(blocks));
    }

    @Override
    protected boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos) {
        ClientLevel world = mod.getWorld();
        if (world == null) return false;
        LevelChunk chunk = world.getChunk(pos.x(), pos.z());
        // Ask each section's palette whether it *could* hold one of our blocks. This is a
        // handful of palette lookups instead of walking every block in the chunk (which was
        // ~100k block state reads per chunk, every time a chunk was considered).
        for (LevelChunkSection section : chunk.getSections()) {
            if (section == null || section.hasOnlyAir()) continue;
            if (section.maybeHas(state -> _toSearchFor.contains(state.getBlock()))) return true;
        }
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof SearchChunkForBlockTask blockTask) {
            return Arrays.equals(blockTask._toSearchFor.toArray(Block[]::new), _toSearchFor.toArray(Block[]::new));
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Searching chunk for blocks " + ArrayUtils.toString(_toSearchFor.toArray(Block[]::new));
    }
}

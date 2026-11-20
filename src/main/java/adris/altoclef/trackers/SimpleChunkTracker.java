package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.eventbus.events.ChunkUnloadEvent;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Keeps track of currently loaded chunks. That's it.
 */
public class SimpleChunkTracker {

    private final AltoClef _mod;
    private final Set<ChunkPos> _loaded = new HashSet<>();

    public SimpleChunkTracker(AltoClef mod) {
        _mod = mod;

        // When chunks load...
        EventBus.subscribe(ChunkLoadEvent.class, evt -> onLoad(evt.chunk.getPos()));
        EventBus.subscribe(ChunkUnloadEvent.class, evt -> onUnload(evt.chunkPos));
    }

    private void onLoad(ChunkPos pos) {
        //Debug.logInternal("LOADED: " + pos);
        _loaded.add(pos);
    }

    private void onUnload(ChunkPos pos) {
        //Debug.logInternal("unloaded: " + pos);
        _loaded.remove(pos);
    }

    public boolean isChunkLoaded(ChunkPos pos) {
        return !(_mod.getWorld().getChunk(pos.x(), pos.z()) instanceof EmptyLevelChunk);
    }

    public boolean isChunkLoaded(BlockPos pos) {
        return isChunkLoaded(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }

    public List<ChunkPos> getLoadedChunks() {
        List<ChunkPos> result = new ArrayList<>(_loaded);
        // Only show LOADED chunks.
        result = result.stream()
                .filter(this::isChunkLoaded)
                .distinct()
                .collect(Collectors.toList());
        return result;
    }

    /**
     * Loops through every block in a chunk if it is loaded.
     * If the chunk isn't loaded, it doesn't scan anything.
     *
     * @param chunk       The chunk pos to scan
     * @param onBlockStop Run for every block until it returns true, where it stops scanning.
     * @return whether `onBlockStop` returned true at any point.
     */
    public boolean scanChunk(ChunkPos chunk, Predicate<BlockPos> onBlockStop) {
        if (!isChunkLoaded(chunk)) return false;
        ClientLevel world = _mod.getWorld();
        if (world == null) return false;
        //Debug.logInternal("SCANNED CHUNK " + chunk.toString());
        LevelChunk levelChunk = world.getChunk(chunk.x(), chunk.z());
        LevelChunkSection[] sections = levelChunk.getSections();
        int minY = levelChunk.getMinY();
        for (int sectionIndex = 0; sectionIndex < sections.length; ++sectionIndex) {
            LevelChunkSection section = sections[sectionIndex];
            // Most sections of a chunk column are pure air; skipping them avoids scanning
            // ~4096 blocks apiece (this used to walk all 320+ layers of every chunk).
            if (section == null || section.hasOnlyAir()) continue;
            int sectionMinY = minY + (sectionIndex << 4);
            int startY = Math.max(WorldHelper.WORLD_FLOOR_Y, sectionMinY);
            int endY = Math.min(WorldHelper.WORLD_CEILING_Y, sectionMinY + 15);
            for (int yy = startY; yy <= endY; ++yy) {
                for (int xx = chunk.getMinBlockX(); xx <= chunk.getMaxBlockX(); ++xx) {
                    for (int zz = chunk.getMinBlockZ(); zz <= chunk.getMaxBlockZ(); ++zz) {
                        if (onBlockStop.test(new BlockPos(xx, yy, zz))) return true;
                    }
                }
            }
        }
        return false;
    }

    public void scanChunk(ChunkPos chunk, Consumer<BlockPos> onBlock) {
        scanChunk(chunk, (block) -> {
            onBlock.accept(block);
            return false;
        });
    }

    public void reset(AltoClef mod) {
        Debug.logInternal("CHUNKS RESET");
        _loaded.clear();
    }
}

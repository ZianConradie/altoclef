package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandStatusOverlay {

    private static final long TEXT_REFRESH_INTERVAL_MS = 100L;
    private static final int MAX_LINES = 10;

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter

    // Rendering happens every frame, but building task debug strings is comparatively
    // expensive (it walks the whole task chain and allocates lists/strings per task),
    // so the text is cached and rebuilt a few times per second instead.
    private final List<String> _lines = new ArrayList<>();
    private long _lastRefresh = 0;

    //For the ingame timer
    private long _timeRunning;
    private long _lastTime = 0;

    public void render(AltoClef mod, GuiGraphicsExtractor graphics) {
        if (!mod.getModSettings().shouldShowTaskChain()) {
            return;
        }
        List<Task> tasks = Collections.emptyList();
        if (mod.getTaskRunner().getCurrentTaskChain() != null) {
            tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
        }

        long now = Instant.now().toEpochMilli();
        if (now - _lastRefresh >= TEXT_REFRESH_INTERVAL_MS) {
            _lastRefresh = now;
            rebuildLines(mod, tasks, now);
        }

        Font font = Minecraft.getInstance().font;
        int x = 0;
        int y = 0;
        for (String line : _lines) {
            graphics.text(font, line, x, y, 0xFFFFFFFF);
            x += 8;
            y += font.lineHeight + 2;
        }
    }

    private void rebuildLines(AltoClef mod, List<Task> tasks, long now) {
        _lines.clear();
        if (tasks.size() == 0) {
            _lines.add(" (no task running) ");
            if (_lastTime + 10000 < now && mod.getModSettings().shouldShowTimer()) {//if it doesn't run any task in 10 secs
                _timeRunning = now;//reset the timer
            }
            return;
        }
        if (mod.getModSettings().shouldShowTimer()) { //If it's enabled
            _lastTime = now; //keep the last time for the timer reset
            _lines.add("<" + DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(now - _timeRunning)) + ">"); //Format the running time to string
        }
        if (tasks.size() > MAX_LINES) {
            for (int i = 0; i < tasks.size(); ++i) {
                // Skip over the next tasks
                if (i == 0 || i > tasks.size() - MAX_LINES) {
                    _lines.add(tasks.get(i).toString());
                } else if (i == 1) {
                    _lines.add(" ... ");
                } else {
                    continue;
                }
            }
        } else {
            for (Task task : tasks) {
                _lines.add(task.toString());
            }
        }
    }
}

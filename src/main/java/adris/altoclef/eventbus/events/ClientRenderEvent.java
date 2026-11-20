package adris.altoclef.eventbus.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ClientRenderEvent {
    public GuiGraphicsExtractor graphics;
    public DeltaTracker tickDelta;

    public ClientRenderEvent(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
        this.graphics = graphics;
        this.tickDelta = tickDelta;
    }
}

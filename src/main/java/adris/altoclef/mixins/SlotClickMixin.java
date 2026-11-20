package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SlotClickChangedEvent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerMenu.class)
public class SlotClickMixin {

    private NonNullList<Slot> slots() {
        return ((AbstractContainerMenu) (Object) this).slots;
    }

    private final ThreadLocal<List<ItemStack>> _beforeStacks = ThreadLocal.withInitial(ArrayList::new);

    @Inject(
            method = "clicked",
            at = @At("HEAD")
    )
    private void onSlotClickBegin(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        List<Slot> slots = slots();
        List<ItemStack> beforeStacks = new ArrayList<>(slots.size());
        for (Slot slot : slots) {
            beforeStacks.add(slot.getItem().copy());
        }
        _beforeStacks.set(beforeStacks);
    }

    @Inject(
            method = "clicked",
            at = @At("RETURN")
    )
    private void onSlotClickEnd(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        List<ItemStack> beforeStacks = _beforeStacks.get();
        NonNullList<Slot> slots = slots();
        for (int i = 0; i < beforeStacks.size() && i < slots.size(); ++i) {
            ItemStack before = beforeStacks.get(i);
            ItemStack after = slots.get(i).getItem();
            if (!ItemStack.matches(before, after)) {
                adris.altoclef.util.slots.Slot slot = adris.altoclef.util.slots.Slot.getFromCurrentScreen(i);
                EventBus.publish(new SlotClickChangedEvent(slot, before, after));
            }
        }
    }
}

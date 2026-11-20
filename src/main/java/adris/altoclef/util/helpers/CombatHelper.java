package adris.altoclef.util.helpers;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Combat related helpers.
 * <p>
 * In 26.3 tools and weapons are no longer distinct item subclasses; their attack damage
 * lives in the {@code ATTRIBUTE_MODIFIERS} data component.
 */
public final class CombatHelper {

    private CombatHelper() {
    }

    /**
     * @return the bonus attack damage granted by the given stack (0 if none).
     */
    public static float getAttackDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return 0;
        }
        float damage = 0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            Holder<Attribute> attribute = entry.attribute();
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                damage += (float) entry.modifier().amount();
            }
        }
        return damage;
    }
}

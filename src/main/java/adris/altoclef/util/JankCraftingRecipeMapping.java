package adris.altoclef.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * For crafting table/inventory recipe book crafting, we need to figure out the
 * {@link RecipeDisplayId} the server expects for a given crafting recipe.
 * <p>
 * 26.3 recipes no longer expose ingredients or a result directly; the client instead
 * receives {@link RecipeDisplayEntry} objects (id, display, craft requirements).
 */
public class JankCraftingRecipeMapping {

    /**
     * Finds the recipe book display id matching the given crafting recipe and output item.
     *
     * @param recipe The crafting recipe to check against.
     * @param output The output item of the recipe.
     * @return The matching display id, or empty if none was found.
     */
    public static Optional<RecipeDisplayId> getMinecraftMappedRecipe(CraftingRecipe recipe, Item output) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return Optional.empty();
        }

        List<ItemTarget> toSatisfy = Arrays.stream(recipe.getSlots())
                .filter(itemTarget -> itemTarget != null && !itemTarget.isEmpty())
                .collect(Collectors.toList());

        for (RecipeCollection collection : player.getRecipeBook().getCollections()) {
            for (RecipeDisplayEntry entry : collection.getRecipes()) {
                ItemStack result;
                try {
                    result = entry.display().result().resolveForFirstStack(ContextMap.EMPTY);
                } catch (RuntimeException e) {
                    // Some displays require a full registry context.
                    continue;
                }
                if (result.isEmpty() || result.getItem() != output) {
                    continue;
                }

                Optional<List<Ingredient>> requirements = entry.craftingRequirements();
                if (requirements.isEmpty()) {
                    continue;
                }

                List<ItemTarget> remaining = new ArrayList<>(toSatisfy);
                for (Ingredient ingredient : requirements.get()) {
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    for (int i = 0; i < remaining.size(); ++i) {
                        ItemTarget target = remaining.get(i);
                        if (ingredient.items().anyMatch(holder -> target.matches(holder.value()))) {
                            remaining.remove(i);
                            break;
                        }
                    }
                }

                if (remaining.isEmpty()) {
                    return Optional.of(entry.id());
                }
            }
        }
        return Optional.empty();
    }
}

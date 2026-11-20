package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.DamageTypeTags;

import java.util.Objects;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Helper functions to interpret entity state
 */
public class EntityHelper {
    public static final double ENTITY_GRAVITY = 0.08; // per second

    public static boolean isAngryAtPlayer(AltoClef mod, Entity entity) {
        return isGenerallyHostileToPlayer(mod, entity);
    }

    public static boolean isGenerallyHostileToPlayer(AltoClef mod, Entity entity) {
        if (entity instanceof OwnableEntity tameable && tameable.getOwner() != null
                && tameable.getOwner().equals(mod.getPlayer().getUUID())) return false;
        if (entity instanceof Slime slime && mod.getPlayer().closerThan(slime, 3)) return true;
        if (entity instanceof Raider mob && mod.getPlayer().closerThan(mob, 16)) return true;
        if (entity instanceof Warden warden && mod.getPlayer().closerThan(warden, 16)) return true;
        if (entity instanceof Enderman enderman && enderman.hasLineOfSight(mod.getPlayer()) && enderman.isAggressive())
            return true;
        if (entity instanceof Blaze blaze && mod.getPlayer().closerThan(blaze, 3)) return true;
        if (entity instanceof Slime || entity instanceof Raider || entity instanceof Warden
                || entity instanceof Enderman || entity instanceof Blaze) return false;
        if (entity instanceof Mob mob && !mob.isAggressive()) return false;
        return !isTradingPiglin(entity);
    }

    public static boolean isTradingPiglin(Entity entity) {
        if (entity instanceof Piglin pig) {
            {
                for (ItemStack stack : new ItemStack[]{pig.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND), pig.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND)}) {
                    if (stack.getItem().equals(Items.GOLD_INGOT)) {
                        // We're trading with this one, ignore it.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Calculate the resulting damage dealt to a player as a result of some damage.
     * If this player were to receive this damage, the player's health will be subtracted by the resulting value.
     */
    public static double calculateResultingPlayerDamage(Player player, DamageSource source, double damageAmount) {
        // Copied logic from `PlayerEntity.applyDamage`

        if (player.isInvulnerable())
            return 0;

        // Armor Base
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            damageAmount = CombatRules.getDamageAfterAbsorb(player, (float) damageAmount, source, (float) player.getArmorValue(), (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }

        // Enchantments & Potions
        if (!source.is(DamageTypeTags.BYPASSES_SHIELD)) {
            float k;
            if (player.hasEffect(MobEffects.RESISTANCE) && source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                //noinspection ConstantConditions
                k = (player.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                float j = 25 - k;
                double f = damageAmount * (double) j;
                double g = damageAmount;
                damageAmount = Math.max(f / 25.0F, 0.0F);
            }

            if (damageAmount <= 0.0) {
                damageAmount = 0.0;
            } else {
                k = EnchantmentHelper.getDamageProtection(player.level() instanceof net.minecraft.server.level.ServerLevel sl ? sl : null, player, source);
                if (k > 0) {
                    damageAmount = CombatRules.getDamageAfterMagicAbsorb((float) damageAmount, k);
                }
            }
        }

        // Absorption
        damageAmount = Math.max(damageAmount - player.getAbsorptionAmount(), 0.0F);
        return damageAmount;
    }
}

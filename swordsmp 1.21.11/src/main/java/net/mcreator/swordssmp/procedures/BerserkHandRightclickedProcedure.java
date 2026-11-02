package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.event.PlayerEvents;

public class BerserkHandRightclickedProcedure {
	public BerserkHandRightclickedProcedure() {
		PlayerEvents.END_PLAYER_TICK.register(entity -> {
			execute(entity.level(), entity);
		});
	}

	public static void execute(LevelAccessor world, Entity entity) {
		if (!(entity instanceof LivingEntity living) || living.getMainHandItem().getItem() != SwordssmpModItems.BERSERK_HAND)
			return;
		float health = living.getHealth();
		// The original ladder had no branch for health <= 0 (reachable only while dying) and left
		// the enchants untouched that tick - preserve that exactly instead of assigning a band.
		if (health <= 0)
			return;
		// health > 18 -> 1, (16,18] -> 2, ... , (2,4] -> 9, (0,2] -> 10 (matches the original ladder for every reachable value, incl. >20 max health)
		int level = Mth.clamp((int) ((20f - health) / 2f) + 1, 1, 10);
		Holder<Enchantment> knockback = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK);
		Holder<Enchantment> sharpness = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
		ItemStack stack = living.getMainHandItem();
		// set() replaces the level; ItemStack.enchant() is a max-semantics upsert (Mutable.upgrade) that can only
		// ratchet up - after one low-HP episode the enchants would stay at 10 forever (persists in NBT). Baseline
		// re-set the exact band level every tick in both directions via removeIf + re-add; set() is identical.
		EnchantmentHelper.updateEnchantments(stack, mutable -> {
			mutable.set(knockback, level);
			mutable.set(sharpness, level);
		});
	}
}

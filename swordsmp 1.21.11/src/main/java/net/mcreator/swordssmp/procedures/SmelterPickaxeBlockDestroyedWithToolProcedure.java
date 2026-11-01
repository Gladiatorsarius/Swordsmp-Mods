package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class SmelterPickaxeBlockDestroyedWithToolProcedure {
	public SmelterPickaxeBlockDestroyedWithToolProcedure() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockentity) -> {
			execute(world, pos.getX(), pos.getY(), pos.getZ(), player);
		});
	}

	// Runs after vanilla has fully broken the block, then upgrades this break's
	// fresh drops to their smelting results. Replacing the drops (not the break)
	// keeps the entire vanilla lifecycle intact: tool durability, ore XP, mined
	// statistics, and the tool-aware loot context (Fortune/Silk Touch).
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() != SwordssmpModItems.SMELTER_PICKAXE)
			return;
		if (!(world instanceof ServerLevel _level))
			return;
		BlockPos pos = BlockPos.containing(x, y, z);
		net.minecraft.world.phys.Vec3 center = pos.getCenter();
		boolean smeltedAny = false;
		// Only upgrade drops provably from THIS break: spawned this tick (age 0), within 0.6 blocks
		// of the block center (vanilla popResource scatters within ~0.51), and not player-thrown
		// (thrown items carry an owner; block drops do not). Adjacent breaks and tossed items pass.
		for (ItemEntity drop : _level.getEntitiesOfClass(ItemEntity.class, new AABB(pos),
				e -> e.getAge() == 0 && !e.getItem().isEmpty() && e.getOwner() == null && e.position().distanceToSqr(center) <= 0.36)) {
			ItemStack stack = drop.getItem();
			ItemStack smelted = getSmeltingResult(_level, stack);
			if (!smelted.isEmpty()) {
				smelted.setCount(stack.getCount());
				drop.setItem(smelted);
				smeltedAny = true;
			}
		}
		if (smeltedAny) {
			_level.sendParticles(ParticleTypes.FLAME, (x + 0.5), y, (z + 0.5), 180, 0.7, 0.15, 0.7, 0.05);
			ProcedureUtils.playBlockSound(world, x, y, z, "block.fire.extinguish", SoundSource.NEUTRAL, 50, 40);
		}
	}

	private static ItemStack getSmeltingResult(LevelAccessor level, ItemStack input) {
		SingleRecipeInput recipeInput = new SingleRecipeInput(input);
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel).map(recipe -> recipe.value().assemble(recipeInput, serverLevel.registryAccess()).copy()).orElse(ItemStack.EMPTY);
		}
		return ItemStack.EMPTY;
	}
}

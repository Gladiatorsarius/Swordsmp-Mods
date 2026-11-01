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
		boolean smeltedAny = false;
		// Only upgrade drops provably from THIS break. Bytecode-verified: Block.popResource places the
		// item entity's feet at x/z = center +/- uniform(0.25), y = center + uniform(-0.25,0.25) minus
		// half the item entity's height - so relative to the block center, the drop's bounding-box
		// CENTER sits within +/-0.25 on every axis exactly, independent of entity height. Any drop
		// from an adjacent block (including straight above/below) is at least 0.75 away on that axis.
		// The 0.26 bounds (small fp margin) therefore include every own drop and provably exclude
		// every adjacent one. The owner check excludes player-thrown items (block drops have none).
		for (ItemEntity drop : _level.getEntitiesOfClass(ItemEntity.class, new AABB(pos), e -> {
			if (e.getAge() != 0 || e.getItem().isEmpty() || e.getOwner() != null)
				return false;
			net.minecraft.world.phys.Vec3 c = e.getBoundingBox().getCenter();
			return Math.abs(c.x - (pos.getX() + 0.5)) <= 0.26 && Math.abs(c.y - (pos.getY() + 0.5)) <= 0.26 && Math.abs(c.z - (pos.getZ() + 0.5)) <= 0.26;
		})) {
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

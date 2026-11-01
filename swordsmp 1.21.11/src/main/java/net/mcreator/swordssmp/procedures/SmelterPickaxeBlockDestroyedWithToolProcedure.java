package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
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
	public static boolean eventResult = true;

	public SmelterPickaxeBlockDestroyedWithToolProcedure() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockentity) -> {
			execute(world, pos.getX(), pos.getY(), pos.getZ(), player);
			boolean result = eventResult;
			eventResult = true;
			return result;
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SwordssmpModItems.SMELTER_PICKAXE) {
			BlockPos pos = BlockPos.containing(x, y, z);
			ItemStack smelted = getSmeltingResult(world, new ItemStack(world.getBlockState(pos).getBlock()));
			if (!smelted.isEmpty()) {
				if (world instanceof ServerLevel _level) {
					ItemEntity entityToSpawn = new ItemEntity(_level, (x + 0.5), (y + 0.5), (z + 0.5), smelted);
					entityToSpawn.setPickUpDelay(10);
					_level.addFreshEntity(entityToSpawn);
					_level.sendParticles(ParticleTypes.FLAME, (x + 0.5), y, (z + 0.5), 180, 0.7, 0.15, 0.7, 0.05);
				}
				ProcedureUtils.playBlockSound(world, x, y, z, "block.fire.extinguish", SoundSource.NEUTRAL, 50, 40);
				world.destroyBlock(pos, false);
			} else {
				Block.dropResources(world.getBlockState(pos), world, pos, null);
				world.destroyBlock(pos, false);
			}
			// The block is fully handled here; cancel vanilla's own break so it
			// does not drop the same resources a second time.
			eventResult = false;
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

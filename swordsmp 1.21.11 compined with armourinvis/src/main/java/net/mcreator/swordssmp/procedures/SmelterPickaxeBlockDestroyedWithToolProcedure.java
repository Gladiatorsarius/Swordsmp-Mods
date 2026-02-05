package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
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
			if (!((getItemStackFromItemStackSlot(world, (new ItemStack((world.getBlockState(BlockPos.containing(x, y, z))).getBlock())))).getItem() == Blocks.AIR.asItem())) {
				if (world instanceof ServerLevel _level) {
					ItemEntity entityToSpawn_9 = new ItemEntity(_level, (x + 0.5), (y + 0.5), (z + 0.5), (getItemStackFromItemStackSlot(world, (new ItemStack((world.getBlockState(BlockPos.containing(x, y, z))).getBlock())))));
					entityToSpawn_9.setPickUpDelay(10);
					_level.addFreshEntity(entityToSpawn_9);
					_level.sendParticles(ParticleTypes.FLAME, (x + 0.5), y, (z + 0.5), 180, 0.7, 0.15, 0.7, 0.05);
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.fire.extinguish")), SoundSource.NEUTRAL, 50, 40);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.fire.extinguish")), SoundSource.NEUTRAL, 50, 40, false);
					}
				}
				{
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.removeBlock = true;
					_vars.markSyncDirty();
				}
			} else {
				{
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.removeBlock = false;
					_vars.markSyncDirty();
				}
			}
			if (entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES).removeBlock == true) {
				world.destroyBlock(BlockPos.containing(x, y, z), false);
			} else {
				{
					BlockPos _pos = BlockPos.containing(x, y, z);
					Block.dropResources(world.getBlockState(_pos), world, BlockPos.containing(x, y, z), null);
					world.destroyBlock(_pos, false);
				}
			}
		}
	}

	private static ItemStack getItemStackFromItemStackSlot(LevelAccessor level, ItemStack input) {
		SingleRecipeInput recipeInput = new SingleRecipeInput(input);
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel).map(recipe -> recipe.value().assemble(recipeInput, serverLevel.registryAccess()).copy()).orElse(ItemStack.EMPTY);
		}
		return ItemStack.EMPTY;
	}
}

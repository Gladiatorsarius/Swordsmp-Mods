package net.mcreator.swordssmp.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.mcreator.swordssmp.procedures.SmelterPickaxeBlockDestroyedWithToolProcedure;
import net.mcreator.swordssmp.procedures.ProcedureUtils;
import net.mcreator.swordssmp.init.SwordssmpModItems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockSmelterMixin {
	// Exact per-break interception for the Smelter Pickaxe. This static call IS the
	// vanilla drop path of Block.playerDestroy: it runs once per broken block, with
	// the actual breaking tool, after the block is gone and before any item exists.
	// (Fabric's AFTER event cannot do this job: it fires at Block.destroy inside
	// ServerPlayerGameMode.destroyBlock, BEFORE mineBlock/playerDestroy spawn drops,
	// so any AFTER-time scan is structurally blind to the break's own drops.)
	// Cancelling replaces exactly this break's drops - no timing or spatial filters.
	// Durability, mined stat and food exhaustion live outside this call and stay
	// vanilla; ore XP is re-applied via the same spawnAfterBreak call vanilla makes.
	@Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private static void swordssmp$smeltBrokenBlockDrops(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
		if (!(level instanceof ServerLevel serverLevel))
			return; // matches vanilla's own guard
		if (tool.getItem() != SwordssmpModItems.SMELTER_PICKAXE)
			return;
		if (blockEntity != null)
			return; // never touch containers: their contents spill separately and stay untouched
		// Baseline semantics: one smelting result of the block ITEM (recipe output
		// count, e.g. copper ore -> 1 copper ingot, clay -> 1 terracotta), ignoring
		// Fortune and Silk Touch, exactly as the original mod produced.
		ItemStack smelted = SmelterPickaxeBlockDestroyedWithToolProcedure.getSmeltingResult(serverLevel, new ItemStack(state.getBlock()));
		if (smelted.isEmpty())
			return; // not smeltable: vanilla drops stand
		ItemEntity result = new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, smelted);
		result.setPickUpDelay(10);
		serverLevel.addFreshEntity(result);
		state.spawnAfterBreak(serverLevel, pos, tool, true);
		serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180, 0.7, 0.15, 0.7, 0.05);
		ProcedureUtils.playBlockSound(serverLevel, pos.getX(), pos.getY(), pos.getZ(), "block.fire.extinguish", SoundSource.NEUTRAL, 50, 40);
		ci.cancel();
	}
}

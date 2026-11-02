package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.sounds.SoundSource;

public class DragonSlayerLivingEntityIsHitWithToolProcedure {

	public static void execute(LevelAccessor world, double x, double y, double z) {
		ProcedureUtils.playBlockSound(world, x, y, z, "entity.ender_dragon.flap", SoundSource.NEUTRAL, 1, 2);
	}
}

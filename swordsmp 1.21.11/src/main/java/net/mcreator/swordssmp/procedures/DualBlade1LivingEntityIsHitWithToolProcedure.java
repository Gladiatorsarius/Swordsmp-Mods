package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.sounds.SoundSource;

public class DualBlade1LivingEntityIsHitWithToolProcedure {

	public static void execute(LevelAccessor world, double x, double y, double z) {
		ProcedureUtils.playBlockSound(world, x, y, z, "block.anvil.land", SoundSource.NEUTRAL, 1, 2);
	}
}

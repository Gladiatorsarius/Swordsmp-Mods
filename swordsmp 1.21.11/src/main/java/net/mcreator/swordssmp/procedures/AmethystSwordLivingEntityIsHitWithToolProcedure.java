package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.sounds.SoundSource;

public class AmethystSwordLivingEntityIsHitWithToolProcedure {

	public static void execute(LevelAccessor world, double x, double y, double z) {
		ProcedureUtils.playBlockSound(world, x, y, z, "block.amethyst_cluster.hit", SoundSource.NEUTRAL, 50, 1);
	}
}

package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.sounds.SoundSource;

public class BreacherLivingEntityIsHitWithToolProcedure {

	public static void execute(LevelAccessor world, double x, double y, double z) {
		ProcedureUtils.playBlockSound(world, x, y, z, "item.shield.break", SoundSource.NEUTRAL, 1, 1);
	}
}

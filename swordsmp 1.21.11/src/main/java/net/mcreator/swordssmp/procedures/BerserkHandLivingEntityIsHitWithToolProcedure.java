package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.sounds.SoundSource;

public class BerserkHandLivingEntityIsHitWithToolProcedure {

	public static void execute(LevelAccessor world, double x, double y, double z) {
		ProcedureUtils.playBlockSound(world, x, y, z, "item.wolf_armor.break", SoundSource.NEUTRAL, 1, 2);
	}
}

package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.init.SwordssmpModItems;

public class LevitationWandRightclickedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SwordssmpModItems.LEVITATION_WAND) {
			for (int index0 = 0; index0 < 10; index0++) {
				if (world instanceof ServerLevel _level) {
					ProcedureUtils.runCommand(_level, x, y, z, "/execute at @p run tp @e[limit=10,sort=nearest,distance=1..15,type=!block_display,type=!item] ^ ^ ^10");
				}
				entity.hurt(new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.FELL_OUT_OF_WORLD)), 2);
				ProcedureUtils.playBlockSound(world, x, y, z, "entity.ender_eye.death", SoundSource.NEUTRAL, 1, 1);
			}
		}
	}
}

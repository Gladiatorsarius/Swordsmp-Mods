package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.event.PlayerEvents;

public class PhantomBladeToolInInventoryTickProcedure {
	public static boolean eventResult = true;

	public PhantomBladeToolInInventoryTickProcedure() {
		PlayerEvents.END_PLAYER_TICK.register(entity -> {
			execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SwordssmpModItems.PHANTOM_BLADE) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
				_entity.addEffect(new MobEffectInstance(MobEffects.SPEED, 1, 2, false, false));
				_entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 1, 2, false, false));
				_entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1, 1, false, false));
			}
			int phantomwings = RandomSource.create().nextInt(10) + 1;
			if (phantomwings == 2) {
				if (world instanceof ServerLevel _level) {
					LightningBolt entityToSpawn_6 = EntityType.LIGHTNING_BOLT.create(_level, EntitySpawnReason.TRIGGERED);
					entityToSpawn_6.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
					entityToSpawn_6.setVisualOnly(true);
					_level.addFreshEntity(entityToSpawn_6);
				}
			}
		}
	}
}

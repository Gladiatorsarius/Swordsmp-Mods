package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.init.SwordssmpModItems;

public class AncientVoidRelicLivingEntityIsHitWithToolProcedure {
	public static boolean eventResult = true;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		ItemStack mainHandStack = (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() != SwordssmpModItems.ANCIENT_VOID_RELIC) {
			return;
		}
		CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
		CompoundTag relicTag = customData != null ? customData.copyTag() : new CompoundTag();
		long now = 0L;
		if (world instanceof Level _level) {
			now = _level.getGameTime();
		}
		long activeUntil = relicTag.getLong("VoidRelicActiveUntil").orElse(0L);
		if (activeUntil > 0L && now <= activeUntil) {
			if (world instanceof ServerLevel _level) {
				entity.teleportTo(entity.getX(), -67, entity.getZ());
			}
		}
	}
}

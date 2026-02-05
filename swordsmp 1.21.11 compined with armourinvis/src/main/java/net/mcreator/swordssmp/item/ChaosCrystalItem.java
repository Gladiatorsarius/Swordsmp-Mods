package net.mcreator.swordssmp.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.procedures.ChaosCrystalItemInInventoryTickProcedure;

public class ChaosCrystalItem extends Item {
	public ChaosCrystalItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
		super.inventoryTick(itemstack, world, entity, equipmentSlot);
		ChaosCrystalItemInInventoryTickProcedure.execute(entity);
	}
}

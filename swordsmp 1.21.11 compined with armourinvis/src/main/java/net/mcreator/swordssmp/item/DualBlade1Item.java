package net.mcreator.swordssmp.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.procedures.DualBlade1ToolInHandTickProcedure;
import net.mcreator.swordssmp.procedures.DualBlade1LivingEntityIsHitWithToolProcedure;

public class DualBlade1Item extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 50000, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:dual_blade_1_repair_items")));

	public DualBlade1Item(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -2.4f));
	}

	@Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		super.hurtEnemy(itemstack, entity, sourceentity);
		DualBlade1LivingEntityIsHitWithToolProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}

	@Override
	public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
		super.inventoryTick(itemstack, world, entity, equipmentSlot);
		if (equipmentSlot == EquipmentSlot.MAINHAND)
			DualBlade1ToolInHandTickProcedure.execute(entity);
	}
}

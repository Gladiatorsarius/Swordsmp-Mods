package net.mcreator.swordssmp.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantments;
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

import net.mcreator.swordssmp.procedures.DragonSlayerLivingEntityIsHitWithToolProcedure;

public class DragonSlayerItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 12500, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:dragon_slayer_repair_items")));

	public DragonSlayerItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 9f, -2.6f));
	}

	@Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		super.hurtEnemy(itemstack, entity, sourceentity);
		DragonSlayerLivingEntityIsHitWithToolProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}

	@Override
	public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
		super.inventoryTick(itemstack, world, entity, equipmentSlot);
		var enchantments = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.SMITE), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.BANE_OF_ARTHROPODS), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.KNOCKBACK), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.LOOTING), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.SWEEPING_EDGE), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 10);
		itemstack.enchant(enchantments.getOrThrow(Enchantments.MENDING), 1);
	}
}

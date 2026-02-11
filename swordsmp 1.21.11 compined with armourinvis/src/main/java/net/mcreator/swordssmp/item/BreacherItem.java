package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.procedures.BreacherLivingEntityIsHitWithToolProcedure;

public class BreacherItem extends Item {
    // Stats: BlockTag, Durability, Mining Speed, Damage Bonus, Enchantability, Repair Tag
    private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1200, 4f, 0, 15, 
        TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:breacher_repair_items"))
    );

    public BreacherItem(Item.Properties properties) {
        // sword(Material, Damage Modifier, Speed Modifier)
        // Damage: 1 (Base) + 3 = 4 | Speed: 4 (Base) - 2.2 = 1.8
        super(properties.sword(TOOL_MATERIAL, 3f, -2.2f));
    }

    @Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		super.hurtEnemy(itemstack, entity, sourceentity);
		BreacherLivingEntityIsHitWithToolProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}
}
package net.mcreator.swordssmp.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.procedures.BerserkHandRightclickedProcedure;
import net.mcreator.swordssmp.procedures.BerserkHandLivingEntityIsHitWithToolProcedure;

public class BerserkHandItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 7210, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:berserk_hand_repair_items")));

	public BerserkHandItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 4f, -2.2f));
	}

	@Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		super.hurtEnemy(itemstack, entity, sourceentity);
		BerserkHandLivingEntityIsHitWithToolProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}

	@Override
	public InteractionResult use(Level world, Player entity, InteractionHand hand) {
		InteractionResult ar = super.use(world, entity, hand);
		BerserkHandRightclickedProcedure.execute(world, entity);
		return ar;
	}
}

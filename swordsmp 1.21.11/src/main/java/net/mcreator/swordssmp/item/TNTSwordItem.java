package net.mcreator.swordssmp.item;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.tags.BlockTags;

import net.mcreator.swordssmp.procedures.TNTSwordRightclickedOnBlockProcedure;

public class TNTSwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = ModToolMaterials.of(BlockTags.INCORRECT_FOR_STONE_TOOL, 2100, 2, "tnt_sword_repair_items");

	public TNTSwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -2.4f));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		super.useOn(context);
		TNTSwordRightclickedOnBlockProcedure.execute(context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getPlayer());
		return InteractionResult.SUCCESS;
	}
}

package net.mcreator.swordssmp.item;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.procedures.TNTSwordRightclickedOnBlockProcedure;

public class TNTSwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 2100, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:tnt_sword_repair_items")));

	public TNTSwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -3.15f));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		super.useOn(context);
		TNTSwordRightclickedOnBlockProcedure.execute(context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getPlayer());
		return InteractionResult.SUCCESS;
	}
}

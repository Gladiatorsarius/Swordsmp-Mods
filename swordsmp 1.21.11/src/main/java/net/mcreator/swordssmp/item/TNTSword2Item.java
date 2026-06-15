package net.mcreator.swordssmp.item;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.procedures.TNTSword2RightclickedOnBlockProcedure;

public class TNTSword2Item extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 2100, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:tnt_sword_repair_items")));

	public TNTSword2Item(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -2.4f));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		super.useOn(context);
		TNTSword2RightclickedOnBlockProcedure.execute(context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getPlayer());
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		TNTSword2RightclickedOnBlockProcedure.execute(world, player.getX(), player.getY(), player.getZ(), player);
		return InteractionResult.SUCCESS;
	}
}
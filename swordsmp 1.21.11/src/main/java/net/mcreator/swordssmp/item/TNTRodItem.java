package net.mcreator.swordssmp.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

import net.mcreator.swordssmp.procedures.TNTRodRightclickedProcedure;

public class TNTRodItem extends ModFishingRodItem {
	public TNTRodItem(Item.Properties properties) {
		super(properties.durability(5000).repairable(ModToolMaterials.repairTag("tnt_rod_repair_items")).enchantable(2));
	}

	@Override
	public InteractionResult use(Level world, Player entity, InteractionHand hand) {
		InteractionResult result = super.use(world, entity, hand);
		TNTRodRightclickedProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity);
		return result;
	}
}

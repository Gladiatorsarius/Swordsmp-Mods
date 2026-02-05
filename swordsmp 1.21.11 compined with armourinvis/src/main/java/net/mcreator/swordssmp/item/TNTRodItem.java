package net.mcreator.swordssmp.item;

import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.tags.TagKey;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.procedures.TNTRodRightclickedProcedure;
import net.mcreator.swordssmp.init.SwordssmpModItems;

public class TNTRodItem extends FishingRodItem {
	public TNTRodItem(Item.Properties properties) {
		super(properties.durability(5000).repairable(TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:tnt_rod_repair_items"))).enchantable(2));
	}

	@Override
	public InteractionResult use(Level world, Player entity, InteractionHand hand) {
		ItemStack itemStack = entity.getItemInHand(hand);
		if (entity.fishing != null) {
			if (!world.isClientSide()) {
				itemStack.hurtAndBreak(entity.fishing.retrieve(itemStack), (LivingEntity) entity, hand);
			}
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
			entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
		} else {
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
			if (world instanceof ServerLevel serverLevel) {
				int j = (int) (EnchantmentHelper.getFishingTimeReduction(serverLevel, itemStack, entity) * 20.0f);
				int k = EnchantmentHelper.getFishingLuckBonus(serverLevel, itemStack, entity);
				Projectile.spawnProjectile(new FishingHook(entity, world, k, j) {
					@Override
					protected boolean shouldStopFishing(Player entity) {
						if (entity.isRemoved() || !entity.isAlive() || !entity.getMainHandItem().is(SwordssmpModItems.TNT_ROD) && !entity.getOffhandItem().is(SwordssmpModItems.TNT_ROD) && this.distanceToSqr(entity) > 1024.0) {
							this.discard();
							return true;
						}
						return false;
					}
				}, serverLevel, itemStack);
			}
			entity.awardStat(Stats.ITEM_USED.get(this));
			entity.gameEvent(GameEvent.ITEM_INTERACT_START);
		}
		TNTRodRightclickedProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity);
		return InteractionResult.SUCCESS;
	}
}

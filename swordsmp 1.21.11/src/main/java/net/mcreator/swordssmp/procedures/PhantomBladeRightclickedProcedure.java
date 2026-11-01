package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class PhantomBladeRightclickedProcedure {
	public static boolean eventResult = true;

	public PhantomBladeRightclickedProcedure() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (hand == player.getUsedItemHand())
				execute(level, player);
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});
	}

	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		ItemStack offhandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY);
		if (offhandStack.getItem() == SwordssmpModItems.PHANTOM_BLADE) {
			CompoundTag cooldownTag = ProcedureUtils.getCustomTag(offhandStack);
			boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "PhantomBlade", entity);
			long now = ProcedureUtils.gameTime(world);
			long cooldownUntil = cooldownTag.getLong("PhantomBladeCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("PhantomBladeCooldownUntil");
				cooldownTag.remove("PhantomBladeCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				ProcedureUtils.saveCustomTag(offhandStack, cooldownTag);
			}
			// native cooldown guard for offhand
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(offhandStack))
				return;

			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
				_entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 1, false, false));
			}

			ProcedureUtils.startCooldown(offhandStack, cooldownTag, "PhantomBlade", entity, now, 600L);
		}
	}
}

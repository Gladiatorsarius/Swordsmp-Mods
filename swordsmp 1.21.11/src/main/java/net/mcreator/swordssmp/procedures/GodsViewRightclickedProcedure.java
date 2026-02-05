package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;

import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.SwordssmpMod;
import net.mcreator.swordssmp.init.SwordssmpModItems;

public class GodsViewRightclickedProcedure {
	public static boolean eventResult = true;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (!(entity instanceof Player player))
			return;
		ItemStack mainHandStack = player.getMainHandItem();
		if (mainHandStack.getItem() != SwordssmpModItems.GODS_VIEW)
			return;
		CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
		CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
		boolean tagChanged = false;
		String ownerId = cooldownTag.getString("GodsViewCooldownOwner").orElse("");
		if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
			cooldownTag.remove("GodsViewCooldownUntil");
			cooldownTag.remove("GodsViewCooldownOwner");
			tagChanged = true;
		}
		long now = 0L;
		if (world instanceof Level _level) {
			now = _level.getGameTime();
		}
		long cooldownUntil = cooldownTag.getLong("GodsViewCooldownUntil").orElse(0L);
		if (cooldownUntil > 0L) {
			if (now < cooldownUntil) {
				return;
			}
			cooldownTag.remove("GodsViewCooldownUntil");
			cooldownTag.remove("GodsViewCooldownOwner");
			tagChanged = true;
		}
		if (tagChanged) {
			mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
		}
		if (entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES).godsviewCooldown == 0) {
			if (world instanceof ServerLevel _level) {
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"/effect give @a[distance=0..80] minecraft:glowing 5 255");
			}
			if (world instanceof Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.beacon.activate")), SoundSource.PLAYERS, 3, 1);
				} else {
					_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.beacon.activate")), SoundSource.PLAYERS, 3, 1, false);
				}
			}
			if (world instanceof ServerLevel _level) {
				LightningBolt entityToSpawn_2 = EntityType.LIGHTNING_BOLT.create(_level, EntitySpawnReason.TRIGGERED);
				entityToSpawn_2.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
				entityToSpawn_2.setVisualOnly(true);
				_level.addFreshEntity(entityToSpawn_2);
			}
			{
				SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
				_vars.godsviewCooldown = 1;
				_vars.markSyncDirty();
			}
			// add native cooldown overlay (15 seconds)
			if (entity instanceof Player _player && !_player.level().isClientSide()) {
				_player.getCooldowns().addCooldown(_player.getMainHandItem(), 300);
			}

			cooldownTag.putLong("GodsViewCooldownUntil", now + 300L);
			cooldownTag.putString("GodsViewCooldownOwner", player.getStringUUID());
			mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			// reset variable after cooldown
			SwordssmpMod.queueServerWork(300, () -> {
				SwordssmpModVariables.PlayerVariables _vars2 = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
				_vars2.godsviewCooldown = 0;
				_vars2.markSyncDirty();
			});
		}
	}
}

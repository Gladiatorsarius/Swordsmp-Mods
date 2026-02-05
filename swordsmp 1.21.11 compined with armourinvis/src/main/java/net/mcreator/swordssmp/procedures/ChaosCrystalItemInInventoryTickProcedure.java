package net.mcreator.swordssmp.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.swordssmp.network.SwordssmpModVariables;

public class ChaosCrystalItemInInventoryTickProcedure {
	public static boolean eventResult = true;

	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
			_vars.WindBladeCooldown = 0;
			_vars.Djump = 0;
			_vars.WindBladeBautaReset = 0;
			_vars.TP = 1;
			_vars.ThunderCooldown = 0;
			_vars.phantomwings = 0;
			_vars.TNTCooldown = 0;
			_vars.GhostBladeDash = 0;
			_vars.VoidRelicTimer = 12000;
			_vars.WardenBlasterCharge = 0;
			_vars.DripstoneCooldown = 0;
			_vars.godsviewCooldown = 0;
			_vars.PhantomInvissCooldown = 0;
			_vars.markSyncDirty();
		}
	}
}

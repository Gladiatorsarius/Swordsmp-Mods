/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.mcreator.swordssmp.procedures.*;

@SuppressWarnings("InstantiationOfUtilityClass")
public class SwordssmpModProcedures {
	public static void load() {
		new ChorusSlayerRightclickedProcedure();
		new SmelterPickaxeBlockDestroyedWithToolProcedure();
		// new ThunderSwordLivingEntityIsHitWithToolProcedure(); // Disabled - use ThunderSwordRightclickedProcedure instead
		new ThunderSwordRightclickedProcedure();
		new PhantomBladeRightclickedProcedure();
		new PhantomBladeToolInInventoryTickProcedure();
		new TNTSwordRightclickedOnBlockProcedure();
		new WardenBlasterRightclickedProcedure();
		new EarthWaveSwordRightclickedProcedure();
		new GhostBladeRightclickedProcedure();
		new BerserkHandRightclickedProcedure();
		new TNTRodRightclickedProcedure();
		new TheAbominationRightclickedProcedure();
	}
}

package net.mcreator.swordssmp;

import net.mcreator.swordssmp.network.SwordssmpModVariables;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class SwordssmpModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Start of user code block mod constructor
		// End of user code block mod constructor

		ClientPlayNetworking.registerGlobalReceiver(SwordssmpModVariables.PlayerVariablesSyncMessage.TYPE, SwordssmpModVariables.PlayerVariablesSyncMessage::handleData);
		// Start of user code block mod init
		// End of user code block mod init
	}
	// Start of user code block mod methods
	// End of user code block mod methods
}

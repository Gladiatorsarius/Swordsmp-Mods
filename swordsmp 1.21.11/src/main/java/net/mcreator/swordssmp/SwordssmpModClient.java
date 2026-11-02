package net.mcreator.swordssmp;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.network.InvisibilityAmplifierSyncMessage;
import net.mcreator.swordssmp.network.InvisibilityAmplifierClientCache;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
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
		ClientPlayNetworking.registerGlobalReceiver(InvisibilityAmplifierSyncMessage.TYPE, (message, context) -> InvisibilityAmplifierClientCache.set(message.entityId(), message.amplifier()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> InvisibilityAmplifierClientCache.clear());
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> InvisibilityAmplifierClientCache.set(entity.getId(), -1));
		// Start of user code block mod init
		// End of user code block mod init
	}
	// Start of user code block mod methods
	// End of user code block mod methods
}

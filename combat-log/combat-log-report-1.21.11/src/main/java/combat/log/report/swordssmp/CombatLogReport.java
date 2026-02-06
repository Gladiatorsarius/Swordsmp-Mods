package combat.log.report.swordssmp;

import combat.log.report.swordssmp.config.ModConfig;
import combat.log.report.swordssmp.linking.PlayerLinkingManager;
import combat.log.report.swordssmp.linking.UnlinkCommand;
import combat.log.report.swordssmp.socket.SocketClient;
import combat.log.report.swordssmp.whitelist.WhitelistCommandHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CombatLogReport implements ModInitializer {
	public static final String MOD_ID = "combat-log-report";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Combat Log Report mod initialized!");
		LOGGER.info("Combat logging tracking is now active");
		LOGGER.info("Players who disconnect during combat will be reported");
		
		// Initialize game rules
		CombatLogGameRules.initialize();
		
		// Register unlink command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			UnlinkCommand.register(dispatcher);
		});
		
		// Register server start event to load config and connect to Discord bot
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
		
		// Register server stop event to disconnect
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
		
		// Register damage event to track combat
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			// Check if the entity being damaged is a player
			if (entity instanceof ServerPlayer victim) {
				// Check if the damage source is from another player
				if (source.getEntity() instanceof ServerPlayer attacker) {
					// Tag both players in combat
					CombatManager.getInstance().tagPlayer(attacker);
					CombatManager.getInstance().tagPlayer(victim);
				}
			}
			return true; // Allow the damage to proceed
		});
	}
	
	private void onServerStart(MinecraftServer server) {
		// Load configuration
		File configFile = new File("config/combat-log-report.json");
		ModConfig config = ModConfig.load(configFile);
		
		// Initialize player linking manager
		File configDir = new File("config");
		PlayerLinkingManager.initialize(configDir);
		LOGGER.info("Initialized player linking system");
		
		// Initialize socket client if enabled
		if (config.socket.enabled) {
			SocketClient socketClient = SocketClient.getInstance();
			socketClient.configure(config.socket.serverUrl);
			
			// Initialize whitelist command handler
			WhitelistCommandHandler whitelistHandler = new WhitelistCommandHandler(server, socketClient);
			socketClient.setWhitelistHandler(whitelistHandler);
			LOGGER.info("Initialized whitelist command handler");
			
			socketClient.connect();
			LOGGER.info("Attempting to connect to Discord bot at {}", config.socket.serverUrl);
		} else {
			LOGGER.info("Socket communication disabled in configuration");
		}
	}
	
	private void onServerStop(MinecraftServer server) {
		// Disconnect from Discord bot
		SocketClient socketClient = SocketClient.getInstance();
		if (socketClient.isConnected()) {
			LOGGER.info("Disconnecting from Discord bot...");
			socketClient.disconnect();
		}
	}
}
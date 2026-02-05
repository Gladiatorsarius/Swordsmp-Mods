package combat.log.report.swordssmp.config;

import combat.log.report.swordssmp.CombatLogReport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration for the combat log report mod
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;

    // Socket configuration
    public SocketConfig socket = new SocketConfig();
    
    // Punishment configuration
    public PunishmentConfig punishment = new PunishmentConfig();

    public static class SocketConfig {
        public boolean enabled = true;
        public String serverUrl = "ws://localhost:8080/combat-log";
        public int reconnectDelaySeconds = 30;
    }

    public static class PunishmentConfig {
        public long timeoutMinutes = 60;
        public boolean banWhilePending = true;
        public boolean killOnDenial = true;
    }

    public static ModConfig load(File configFile) {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, ModConfig.class);
                CombatLogReport.LOGGER.info("Loaded configuration from {}", configFile.getName());
                return instance;
            } catch (IOException e) {
                CombatLogReport.LOGGER.error("Failed to load config: {}", e.getMessage());
            }
        }
        
        // Create default config
        instance = new ModConfig();
        save(configFile, instance);
        return instance;
    }

    public static void save(File configFile, ModConfig config) {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
                CombatLogReport.LOGGER.info("Saved configuration to {}", configFile.getName());
            }
        } catch (IOException e) {
            CombatLogReport.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public static ModConfig getInstance() {
        return instance;
    }
}

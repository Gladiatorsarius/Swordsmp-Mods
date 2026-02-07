package name.modid;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.Display;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;

/**
 * Event handler for Item Display HP management
 */
public class ItemDisplayEventHandler {

    public static void register() {
        // When an entity loads, load HP data
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!ItemDisplayHPGamerule.isEnabled(world)) return;
            if (entity instanceof Display.ItemDisplay) {
                Display.ItemDisplay display = (Display.ItemDisplay) entity;
                ItemDisplayHPManager.loadFromNBT(display);
                // spawn proxy only for tagged displays on load
                ItemDisplayHPManager.spawnProxyIfTagged(display);
            }
        });

        // When an entity unloads, clean up
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (!ItemDisplayHPGamerule.isEnabled(world)) return;
            if (entity instanceof Display.ItemDisplay) {
                Display.ItemDisplay display = (Display.ItemDisplay) entity;
                ItemDisplayHPManager.unregisterDisplay(display);
            }
        });

        // Monitor proxies each server tick to detect proxy deaths
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                for (ServerLevel level : server.getAllLevels()) {
                    ItemDisplayHPManager.checkProxiesInLevel(level);
                }
            } catch (Throwable t) {
                // ignore
            }
        });
    }
}

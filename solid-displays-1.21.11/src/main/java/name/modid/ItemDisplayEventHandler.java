package name.modid;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.Display;

/**
 * Event-Handler für Item Display HP-Verwaltung
 */
public class ItemDisplayEventHandler {

    public static void register() {
        // Wenn ein Entity geladen wird, lade die HP-Daten
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Display.ItemDisplay) {
                Display.ItemDisplay display = (Display.ItemDisplay) entity;
                ItemDisplayHPManager.loadFromNBT(display);
            }
        });

        // Wenn ein Entity entladen wird, räume auf
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof Display.ItemDisplay) {
                Display.ItemDisplay display = (Display.ItemDisplay) entity;
                ItemDisplayHPManager.unregisterDisplay(display);
            }
        });
    }
}

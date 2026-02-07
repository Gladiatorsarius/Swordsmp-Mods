package name.modid;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.chat.Component;

/**
 * Manages HP values for Item Displays
 */
public class ItemDisplayHPManager {
    private static final int MIN_HP = 1;
    private static final int MAX_HP = 1000;
    
    private static final Map<UUID, ItemDisplayData> displayData = new HashMap<>();
    // mapping display UUID -> proxy armor stand UUID
    private static final Map<UUID, UUID> proxyMap = new HashMap<>();
    // reverse mapping proxy UUID -> display UUID
    private static final Map<UUID, UUID> proxyReverse = new HashMap<>();
    

    public static class ItemDisplayData {
        public float hp;
        public float maxHp;
        public ItemStack itemStack;

        public ItemDisplayData(float hp, ItemStack itemStack) {
            this.hp = hp;
            this.maxHp = hp;
            this.itemStack = itemStack.copy();
        }
    }

    /**
     * Sets the HP of an Item Display
     */
    public static void setHP(Display.ItemDisplay display, float hp) {
        if (hp < MIN_HP) hp = MIN_HP;
        if (hp > MAX_HP) hp = MAX_HP;

        float normalizedHp = hp;

        UUID uuid = display.getUUID();
        ItemDisplayData data = displayData.computeIfAbsent(uuid, 
            u -> new ItemDisplayData(normalizedHp, display.getItemStack()));
        data.hp = normalizedHp;
        
        // Save to NBT
        saveToNBT(display, (int) normalizedHp, (int) data.maxHp);
    }

    /**
     * Returns the current HP of an Item Display
     */
    public static float getHP(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        if (displayData.containsKey(uuid)) {
            return displayData.get(uuid).hp;
        }
        
        // Attempt to load from NBT
        ItemDisplayHpData dataAccess = (ItemDisplayHpData) display;
        if (dataAccess.solidDisplays$hasData()) {
            float hp = dataAccess.solidDisplays$getHp();
            float maxHp = dataAccess.solidDisplays$getMaxHp();
            ItemDisplayData data = new ItemDisplayData(hp, display.getItemStack());
            data.maxHp = maxHp;
            displayData.put(uuid, data);
            return hp;
        }
        
        return 0;
    }

    /**
     * Returns the maximum HP of an Item Display
     */
    public static float getMaxHP(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        if (displayData.containsKey(uuid)) {
            return displayData.get(uuid).maxHp;
        }
        
        ItemDisplayHpData dataAccess = (ItemDisplayHpData) display;
        if (dataAccess.solidDisplays$hasData()) {
            return dataAccess.solidDisplays$getMaxHp();
        }
        
        return getHP(display);
    }

    /**
     * Registers an Item Display with initial HP
     */
    public static void registerDisplay(Display.ItemDisplay display, float initialHP) {
        if (initialHP < MIN_HP) initialHP = MIN_HP;
        if (initialHP > MAX_HP) initialHP = MAX_HP;

        UUID uuid = display.getUUID();
        ItemDisplayData data = new ItemDisplayData(initialHP, display.getItemStack());
        displayData.put(uuid, data);
        saveToNBT(display, (int) initialHP, (int) initialHP);
        // Note: proxy ArmorStand is only spawned when the display has an explicit HP tag.
    }

    /**
     * Applies damage to an Item Display
     */
    public static void damageDisplay(Display.ItemDisplay display, float damage) {
        // Check if GameRule is enabled
        if (!ItemDisplayHPGamerule.isEnabled(display.level())) {
            return;
        }
        
        UUID uuid = display.getUUID();
        ItemDisplayData data = displayData.get(uuid);
        
        if (data == null) {
            float currentHP = getHP(display);
            // If display has no HP, initialize with default HP (100)
            if (currentHP == 0) {
                registerDisplay(display, 100.0f);
                data = displayData.get(uuid);
            } else {
                data = new ItemDisplayData(currentHP, display.getItemStack());
                displayData.put(uuid, data);
            }
        }

        float previousHp = data.hp;
        float newHp = previousHp - damage;
        data.hp = newHp;
        ItemStack itemToDrop = data != null ? data.itemStack : display.getItemStack();
        boolean willDrop = newHp <= 0 && itemToDrop != null && !itemToDrop.isEmpty();
        SolidDisplays.LOGGER.info("ItemDisplayHPManager: damage {} to display {} ({} -> {} / {}) willDrop={}", damage, uuid, previousHp, newHp, data.maxHp, willDrop);
        saveToNBT(display, (int) data.hp, (int) data.maxHp);

        if (newHp <= 0) {
            SolidDisplays.LOGGER.info("ItemDisplayHPManager: HP <= 0 for display {} — killing (willDrop={})...", uuid, willDrop);
            killDisplay(display);
        }
    }
    
    /**
     * Checks if a display has HP data
     */
    public static boolean hasHP(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        if (displayData.containsKey(uuid)) {
            return true;
        }
        
        ItemDisplayHpData dataAccess = (ItemDisplayHpData) display;
        return dataAccess.solidDisplays$hasData();
    }

    /**
     * Kills an Item Display and drops its contained item
     */
    public static void killDisplay(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        ItemDisplayData data = displayData.get(uuid);

        if (display.level() != null && !display.level().isClientSide()) {
            // Create ItemEntity at the display's position
            ItemStack itemStack = data != null ? data.itemStack : display.getItemStack();
            SolidDisplays.LOGGER.info("ItemDisplayHPManager: dropping item for display {}: {}", uuid, itemStack);

            if (itemStack != null && !itemStack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                    display.level(),
                    display.getX(),
                    display.getY(),
                    display.getZ(),
                    itemStack
                );
                display.level().addFreshEntity(itemEntity);
                SolidDisplays.LOGGER.info("ItemDisplayHPManager: spawned ItemEntity for display {}", uuid);
            } else {
                SolidDisplays.LOGGER.info("ItemDisplayHPManager: no item to drop for display {}", uuid);
            }
        }

        // Entferne das Display
        displayData.remove(uuid);
        // remove proxy if present
        try {
            UUID proxy = proxyMap.remove(uuid);
            if (proxy != null) {
                proxyReverse.remove(proxy);
                if (display.level() != null) {
                    Entity e = display.level().getEntity(proxy);
                    if (e != null) e.discard();
                }
            }
        } catch (Throwable t) {
            SolidDisplays.LOGGER.debug("ItemDisplayHPManager: failed to remove proxy for display {}: {}", uuid, t.toString());
        }
        
        SolidDisplays.LOGGER.info("ItemDisplayHPManager: discarding display {}", uuid);
        display.discard();
    }

    

    /**
     * Saves HP data to NBT
     */
    private static void saveToNBT(Display.ItemDisplay display, int hp, int maxHp) {
        ItemDisplayHpData dataAccess = (ItemDisplayHpData) display;
        dataAccess.solidDisplays$setData(hp, maxHp);
    }

    /**
     * Loads HP data from NBT
     */
    public static void loadFromNBT(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        ItemDisplayHpData dataAccess = (ItemDisplayHpData) display;
        if (dataAccess.solidDisplays$hasData()) {
            float hp = dataAccess.solidDisplays$getHp();
            float maxHp = dataAccess.solidDisplays$getMaxHp();
            ItemDisplayData data = new ItemDisplayData(hp, display.getItemStack());
            data.maxHp = maxHp;
            displayData.put(uuid, data);
            // no proxy handling when loading from NBT
        }
    }

    /**
     * Removes a display from the manager
     */
    public static void unregisterDisplay(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        displayData.remove(uuid);
        // cleanup proxy mapping as well
        try {
            UUID proxy = proxyMap.remove(uuid);
            if (proxy != null) proxyReverse.remove(proxy);
        } catch (Throwable t) {
            // ignore
        }
    }

    public static UUID getDisplayUuidForProxy(UUID proxyUuid) {
        return proxyReverse.get(proxyUuid);
    }

    public static Display.ItemDisplay getDisplayByUuid(net.minecraft.world.level.Level level, UUID displayUuid) {
        if (level == null) return null;
        Entity e = level.getEntity(displayUuid);
        if (e instanceof Display.ItemDisplay) return (Display.ItemDisplay) e;
        return null;
    }

    /**
     * Spawn an ArmorStand proxy for a display if that display has HP data.
     * This keeps the proxy creation explicit so only tagged displays gain a proxy.
     */
    public static void spawnProxyIfTagged(Display.ItemDisplay display) {
        try {
            ItemDisplayHpData dataAccess = (ItemDisplayHpData) display;
            if (!dataAccess.solidDisplays$hasData()) return; // only spawn for displays that have HP tag

            UUID uuid = display.getUUID();
            if (proxyMap.containsKey(uuid)) return; // already has proxy

            if (display.level() != null && !display.level().isClientSide()) {
                ArmorStand stand = new ArmorStand(display.level(), display.getX(), display.getY(), display.getZ());
                stand.setInvisible(true);
                stand.setNoGravity(true);
                stand.setInvulnerable(false);
                stand.setCustomName(Component.literal("display-proxy"));
                stand.setCustomNameVisible(false);
                display.level().addFreshEntity(stand);
                proxyMap.put(uuid, stand.getUUID());
                proxyReverse.put(stand.getUUID(), uuid);
            }
        } catch (Throwable t) {
            SolidDisplays.LOGGER.debug("ItemDisplayHPManager: spawnProxyIfTagged failed: {}", t.toString());
        }
    }

    /**
     * Spawn an ArmorStand proxy for a display that is explicitly marked as `solid` in NBT.
     * This will also ensure the display is registered with a default HP so that when
     * the proxy dies the manager can drop the display's item and discard the display.
     */
    public static void spawnProxyForSolid(Display.ItemDisplay display) {
        try {
            UUID uuid = display.getUUID();
            if (proxyMap.containsKey(uuid)) return; // already has proxy

            if (display.level() != null && !display.level().isClientSide()) {
                // ensure a runtime registration so we have the ItemStack to drop later
                if (!displayData.containsKey(uuid)) {
                    // default HP 100 for solid displays
                    registerDisplay(display, 100.0f);
                }

                ArmorStand stand = new ArmorStand(display.level(), display.getX(), display.getY(), display.getZ());
                stand.setInvisible(true);
                stand.setNoGravity(true);
                stand.setInvulnerable(false);
                // mark the armor stand so it's identifiable in-world
                stand.setCustomName(Component.literal("solid-display-proxy"));
                stand.setCustomNameVisible(false);
                display.level().addFreshEntity(stand);
                proxyMap.put(uuid, stand.getUUID());
                proxyReverse.put(stand.getUUID(), uuid);
            }
        } catch (Throwable t) {
            SolidDisplays.LOGGER.debug("ItemDisplayHPManager: spawnProxyForSolid failed: {}", t.toString());
        }
    }

    /**
     * Checks proxies in a given level and if a proxy is missing while its display is still present,
     * treat that as proxy death and kill the linked display (dropping the item).
     */
    public static void checkProxiesInLevel(net.minecraft.world.level.Level level) {
        if (level == null || level.isClientSide()) return;
        // iterate over a copy of proxyReverse to avoid concurrent modification
        var entries = new java.util.ArrayList<>(proxyReverse.entrySet());
        for (var e : entries) {
            UUID proxyUuid = e.getKey();
            UUID displayUuid = e.getValue();
            Entity proxy = level.getEntity(proxyUuid);
            if (proxy == null) {
                // proxy missing; if display exists, consider it died
                Entity maybeDisplay = level.getEntity(displayUuid);
                if (maybeDisplay instanceof Display.ItemDisplay display) {
                    SolidDisplays.LOGGER.info("ItemDisplayHPManager: proxy {} missing — killing linked display {}", proxyUuid, displayUuid);
                    killDisplay(display);
                } else {
                    // cleanup stale mapping
                    proxyReverse.remove(proxyUuid);
                    proxyMap.remove(displayUuid);
                }
            }
        }
    }
}

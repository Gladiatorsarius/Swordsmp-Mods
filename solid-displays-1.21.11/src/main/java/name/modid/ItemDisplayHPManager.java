package name.modid;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet die HP-Werte von Item Displays
 */
public class ItemDisplayHPManager {
    private static final int MIN_HP = 1;
    private static final int MAX_HP = 1000;
    
    private static final Map<UUID, ItemDisplayData> displayData = new HashMap<>();

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
     * Setzt die HP eines Item Displays
     */
    public static void setHP(Display.ItemDisplay display, float hp) {
        if (hp < MIN_HP) hp = MIN_HP;
        if (hp > MAX_HP) hp = MAX_HP;

        float normalizedHp = hp;

        UUID uuid = display.getUUID();
        ItemDisplayData data = displayData.computeIfAbsent(uuid, 
            u -> new ItemDisplayData(normalizedHp, display.getItemStack()));
        data.hp = normalizedHp;
        
        // Speichere in NBT
        saveToNBT(display, (int) normalizedHp, (int) data.maxHp);
    }

    /**
     * Gibt die aktuelle HP eines Item Displays zurück
     */
    public static float getHP(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        if (displayData.containsKey(uuid)) {
            return displayData.get(uuid).hp;
        }
        
        // Versuche aus NBT zu laden
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
     * Gibt die maximalen HP eines Item Displays zurück
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
     * Registriert ein Item Display mit initialen HP
     */
    public static void registerDisplay(Display.ItemDisplay display, float initialHP) {
        if (initialHP < MIN_HP) initialHP = MIN_HP;
        if (initialHP > MAX_HP) initialHP = MAX_HP;

        UUID uuid = display.getUUID();
        ItemDisplayData data = new ItemDisplayData(initialHP, display.getItemStack());
        displayData.put(uuid, data);
        saveToNBT(display, (int) initialHP, (int) initialHP);
    }

    /**
     * Sendet Schaden an ein Item Display
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

        data.hp -= damage;
        saveToNBT(display, (int) data.hp, (int) data.maxHp);

        if (data.hp <= 0) {
            killDisplay(display);
        }
    }
    
    /**
     * Prüft ob ein Display HP hat
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
     * Tötet ein Item Display und lässt das Item fallen
     */
    public static void killDisplay(Display.ItemDisplay display) {
        UUID uuid = display.getUUID();
        ItemDisplayData data = displayData.get(uuid);

        if (display.level() != null && !display.level().isClientSide()) {
            // Erstelle ItemEntity an der Position des Displays
            ItemStack itemStack = data != null ? data.itemStack : display.getItemStack();
            
            if (!itemStack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                    display.level(),
                    display.getX(),
                    display.getY(),
                    display.getZ(),
                    itemStack
                );
                display.level().addFreshEntity(itemEntity);
            }
        }

        // Entferne das Display
        displayData.remove(uuid);
        display.discard();
    }

    /**
     * Speichert HP-Daten in NBT
     */
    private static void saveToNBT(Display.ItemDisplay display, int hp, int maxHp) {
        ItemDisplayHpData dataAccess = (ItemDisplayHpData) display;
        dataAccess.solidDisplays$setData(hp, maxHp);
    }

    /**
     * Lädt HP-Daten aus NBT
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
        }
    }

    /**
     * Entfernt ein Display aus dem Manager
     */
    public static void unregisterDisplay(Display.ItemDisplay display) {
        displayData.remove(display.getUUID());
    }
}

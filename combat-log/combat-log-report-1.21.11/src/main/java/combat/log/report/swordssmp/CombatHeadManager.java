package combat.log.report.swordssmp;

import combat.log.report.swordssmp.incident.CombatLogIncident;
import combat.log.report.swordssmp.incident.IncidentManager;
import combat.log.report.swordssmp.incident.IncidentStatus;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatHeadManager {
    private static final CombatHeadManager INSTANCE = new CombatHeadManager();
    
    // Track head location -> player UUID -> incident ID
    private final Map<BlockPos, UUID> headLocations = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> headIncidents = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> headCreationTime = new ConcurrentHashMap<>();
    private final Map<BlockPos, Set<UUID>> headOpponents = new ConcurrentHashMap<>();
    // Store inventory data for each head location
    private final Map<UUID, List<ItemStack>> storedInventories = new ConcurrentHashMap<>();
    
    private static final long ACCESS_RESTRICTION_TIME = 30 * 60 * 1000; // 30 minutes in milliseconds
    
    private CombatHeadManager() {}
    
    public static CombatHeadManager getInstance() {
        return INSTANCE;
    }
    
    public void createCombatLogHead(ServerPlayer player, UUID incidentId, Set<UUID> opponents) {
        BlockPos playerPos = player.blockPosition();
        ServerLevel world = (ServerLevel) player.level();
        
        // Store player's inventory BEFORE creating head
        storePlayerInventory(player);
        
        // Find suitable location for head (on ground)
        BlockPos headPos = findSuitableHeadLocation(world, playerPos);
        if (headPos == null) {
            CombatLogReport.LOGGER.error("Could not find suitable location for combat log head for player {}", player.getName().getString());
            return;
        }
        
        // Place player head
        BlockState headState = Blocks.PLAYER_HEAD.defaultBlockState();
        world.setBlock(headPos, headState, 3);
        
        // Note: Setting owner profile would require Minecraft 1.21 specific API
        // For now, heads will be generic - can be enhanced later
        
        // Track head
        headLocations.put(headPos, player.getUUID());
        headIncidents.put(player.getUUID(), incidentId);
        headCreationTime.put(headPos, System.currentTimeMillis());
        headOpponents.put(headPos, opponents);
        
        CombatLogReport.LOGGER.info("Created combat log head for player {} at position {} with inventory stored", 
            player.getName().getString(), headPos);
    }
    
    private BlockPos findSuitableHeadLocation(ServerLevel world, BlockPos startPos) {
        // Try at player position first
        BlockPos groundPos = startPos;
        
        // Find ground level
        while (groundPos.getY() > world.getMinY() && world.getBlockState(groundPos.below()).isAir()) {
            groundPos = groundPos.below();
        }
        
        // Check if there's space for head
        if (!world.getBlockState(groundPos).isAir()) {
            groundPos = groundPos.above();
        }
        
        if (world.getBlockState(groundPos).isAir() && !world.getBlockState(groundPos.below()).isAir()) {
            return groundPos;
        }
        
        // Try positions around player
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockPos testPos = startPos.offset(x, y, z);
                    if (world.getBlockState(testPos).isAir() && !world.getBlockState(testPos.below()).isAir()) {
                        return testPos;
                    }
                }
            }
        }
        
        return null;
    }
    
    public boolean isHeadLocation(BlockPos pos) {
        return headLocations.containsKey(pos);
    }
    
    public UUID getHeadOwner(BlockPos pos) {
        return headLocations.get(pos);
    }
    
    public UUID getIncidentId(UUID playerId) {
        return headIncidents.get(playerId);
    }
    
    public boolean canAccess(BlockPos pos, ServerPlayer accessor) {
        UUID accessorId = accessor.getUUID();
        Long creationTime = headCreationTime.get(pos);
        
        if (creationTime == null) {
            return false;
        }
        
        // Check ticket status first - if ticket is not closed/resolved, head is locked
        UUID ownerId = headLocations.get(pos);
        if (ownerId != null) {
            UUID incidentId = headIncidents.get(ownerId);
            if (incidentId != null) {
                CombatLogIncident incident = IncidentManager.getInstance().getIncident(incidentId);
                if (incident != null) {
                    IncidentStatus status = incident.getStatus();
                    // Only allow access if ticket is resolved (denied/auto-denied) - approved tickets remove the head
                    if (status == IncidentStatus.PENDING || status == IncidentStatus.CLIP_UPLOADED) {
                        // Ticket still pending - head is locked
                        return false;
                    }
                    if (status == IncidentStatus.APPROVED) {
                        return false;
                    }
                    if (status == IncidentStatus.DENIED || status == IncidentStatus.AUTO_DENIED) {
                        return true;
                    }
                }
            }
        }
        
        long currentTime = System.currentTimeMillis();
        long timeSinceCreation = currentTime - creationTime;
        
        // After 30 minutes, everyone can access
        if (timeSinceCreation >= ACCESS_RESTRICTION_TIME) {
            return true;
        }
        
        // Before 30 minutes, only combat opponents can access
        // Note: OPs would need permission level check - simplified for compatibility
        Set<UUID> opponents = headOpponents.get(pos);
        return opponents != null && opponents.contains(accessorId);
    }

    public boolean dropStoredInventory(BlockPos pos, ServerLevel world) {
        UUID ownerId = headLocations.get(pos);
        if (ownerId == null) {
            return false;
        }

        List<ItemStack> items = storedInventories.remove(ownerId);
        if (items == null || items.isEmpty()) {
            return false;
        }

        double dropX = pos.getX() + 0.5;
        double dropY = pos.getY() + 1.0;
        double dropZ = pos.getZ() + 0.5;

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ItemEntity entity = new ItemEntity(world, dropX, dropY, dropZ, stack.copy());
                world.addFreshEntity(entity);
            }
        }

        CombatLogReport.LOGGER.info("Dropped stored inventory for combat log head at {}", pos);
        return true;
    }
    
    public void removeHead(BlockPos pos) {
        UUID playerId = headLocations.remove(pos);
        if (playerId != null) {
            headIncidents.remove(playerId);
        }
        headCreationTime.remove(pos);
        headOpponents.remove(pos);
    }
    
    /**
     * Store player's inventory (all slots)
     */
    private void storePlayerInventory(ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>();
        Inventory inventory = player.getInventory();
        
        // Store all inventory slots (main inventory + armor + offhand)
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                items.add(stack.copy()); // Create a copy to preserve the item
            } else {
                items.add(ItemStack.EMPTY);
            }
        }
        
        storedInventories.put(player.getUUID(), items);
        
        // Clear player's inventory
        inventory.clearContent();
        
        CombatLogReport.LOGGER.info("Stored inventory for player {} ({} items)", 
            player.getName().getString(), items.stream().filter(stack -> !stack.isEmpty()).count());
    }
    
    /**
     * Restore player's inventory from storage
     */
    public boolean restorePlayerInventory(ServerPlayer player) {
        UUID playerId = player.getUUID();
        List<ItemStack> items = storedInventories.get(playerId);
        
        if (items == null || items.isEmpty()) {
            CombatLogReport.LOGGER.warn("No stored inventory found for player {}", player.getName().getString());
            return false;
        }
        
        Inventory inventory = player.getInventory();
        
        // Restore all items
        for (int i = 0; i < Math.min(items.size(), inventory.getContainerSize()); i++) {
            ItemStack stack = items.get(i);
            inventory.setItem(i, stack);
        }
        
        // Remove from storage
        storedInventories.remove(playerId);
        
        CombatLogReport.LOGGER.info("Restored inventory for player {} ({} items)", 
            player.getName().getString(), items.stream().filter(stack -> !stack.isEmpty()).count());
        
        return true;
    }
    
    /**
     * Remove head and restore inventory (for approved tickets)
     */
    public void removeHeadAndRestoreInventory(ServerPlayer player, UUID playerId) {
        // Find head location for this player
        BlockPos headPos = null;
        for (Map.Entry<BlockPos, UUID> entry : headLocations.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                headPos = entry.getKey();
                break;
            }
        }
        
        if (headPos != null) {
            // Remove the physical head block
            ServerLevel world = (ServerLevel) player.level();
            if (world.getBlockState(headPos).is(Blocks.PLAYER_HEAD)) {
                world.removeBlock(headPos, false);
                CombatLogReport.LOGGER.info("Removed combat log head at {}", headPos);
            }
            
            // Remove from tracking
            removeHead(headPos);
        }
        
        // Restore inventory
        restorePlayerInventory(player);
    }
    
    /**
     * Check if player has stored inventory
     */
    public boolean hasStoredInventory(UUID playerId) {
        return storedInventories.containsKey(playerId);
    }
    
    /**
     * Open combat head UI for a player
     */
    public void openCombatHeadUI(ServerPlayer player, BlockPos headPos) {
        UUID ownerId = headLocations.get(headPos);
        if (ownerId == null) {
            player.sendSystemMessage(Component.literal("§cCombat log head not found!"));
            return;
        }
        
        // Check if player can access
        if (!canAccess(headPos, player)) {
            player.displayClientMessage(
                Component.literal("§c§lYou cannot access this combat log head yet!"),
                true
            );
            return;
        }
        
        // Get stored inventory
        List<ItemStack> items = storedInventories.get(ownerId);
        if (items == null || items.isEmpty()) {
            player.sendSystemMessage(Component.literal("§eThis combat log head has no items stored."));
            return;
        }
        
        // Create container with items
        final SimpleContainer container = new SimpleContainer(41);
        for (int i = 0; i < Math.min(items.size(), 41); i++) {
            container.setItem(i, items.get(i));
        }
        
        final UUID finalOwnerId = ownerId;
        
        // Open chest-like UI
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
            (syncId, playerInventory, playerEntity) -> 
                net.minecraft.world.inventory.ChestMenu.sixRows(syncId, playerInventory, container),
            Component.literal("Combat Log Head - " + getHeadOwnerName(finalOwnerId))
        ));
        
        CombatLogReport.LOGGER.info("Opened combat head UI for player {} at position {}", 
            player.getName().getString(), headPos);
    }
    
    private String getHeadOwnerName(UUID ownerId) {
        UUID incidentId = headIncidents.get(ownerId);
        if (incidentId != null) {
            CombatLogIncident incident = IncidentManager.getInstance().getIncident(incidentId);
            if (incident != null) {
                return incident.getPlayerName();
            }
        }
        return ownerId.toString().substring(0, 8);
    }
}

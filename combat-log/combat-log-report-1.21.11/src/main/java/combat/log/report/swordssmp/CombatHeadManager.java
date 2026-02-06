package combat.log.report.swordssmp;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    
    private static final long ACCESS_RESTRICTION_TIME = 30 * 60 * 1000; // 30 minutes in milliseconds
    
    private CombatHeadManager() {}
    
    public static CombatHeadManager getInstance() {
        return INSTANCE;
    }
    
    public void createCombatLogHead(ServerPlayer player, UUID incidentId, Set<UUID> opponents) {
        BlockPos playerPos = player.blockPosition();
        ServerLevel world = (ServerLevel) player.level();
        
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
        
        CombatLogReport.LOGGER.info("Created combat log head for player {} at position {}", 
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
    
    public void removeHead(BlockPos pos) {
        UUID playerId = headLocations.remove(pos);
        if (playerId != null) {
            headIncidents.remove(playerId);
        }
        headCreationTime.remove(pos);
        headOpponents.remove(pos);
    }
}

package org.allaymc.blocklocker.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a protected/locked block in the world.
 * Stores owner information, trusted players, and protection settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtectedBlock {

    private String worldName;
    private int dimensionId;
    private int x;
    private int y;
    private int z;
    private UUID ownerUuid;
    private String ownerName;
    private long createdAt;
    private List<UUID> trustedPlayers;
    private boolean allowRedstone;
    private boolean allowHoppers;

    public ProtectedBlock(String worldName, int dimensionId, int x, int y, int z, UUID ownerUuid, String ownerName) {
        this.worldName = worldName;
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.createdAt = System.currentTimeMillis();
        this.trustedPlayers = new ArrayList<>();
        this.allowRedstone = false;
        this.allowHoppers = false;
    }

    /**
     * Get the unique key for this block location.
     */
    public String getLocationKey() {
        return worldName + ":" + dimensionId + ":" + x + ":" + y + ":" + z;
    }

    /**
     * Check if a player is the owner of this block.
     */
    public boolean isOwner(UUID playerUuid) {
        return ownerUuid.equals(playerUuid);
    }

    /**
     * Check if a player is trusted (has access) to this block.
     */
    public boolean isTrusted(UUID playerUuid) {
        return trustedPlayers.contains(playerUuid);
    }

    /**
     * Check if a player has access to this block (owner or trusted).
     */
    public boolean hasAccess(UUID playerUuid) {
        return isOwner(playerUuid) || isTrusted(playerUuid);
    }

    /**
     * Add a trusted player.
     */
    public void addTrustedPlayer(UUID playerUuid) {
        if (!trustedPlayers.contains(playerUuid)) {
            trustedPlayers.add(playerUuid);
        }
    }

    /**
     * Remove a trusted player.
     */
    public void removeTrustedPlayer(UUID playerUuid) {
        trustedPlayers.remove(playerUuid);
    }
}

package org.allaymc.blocklocker.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.allaymc.blocklocker.BlockLockerPlugin;
import org.allaymc.blocklocker.data.ProtectedBlock;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all protected blocks, including loading, saving, and lookup.
 */
public class ProtectionManager {

    private final BlockLockerPlugin plugin;
    private final Gson gson;
    private final Path dataFile;

    // Map of location key -> ProtectedBlock
    @Getter
    private final Map<String, ProtectedBlock> protectedBlocks;

    // Cache of players who are currently in "lock mode"
    private final Map<UUID, Boolean> lockModePlayers;

    // Cache of players who are currently in "unlock mode"
    private final Map<UUID, Boolean> unlockModePlayers;

    // Cache of players who are currently in "trust mode"
    private final Map<UUID, UUID> trustModePlayers; // Player -> Target to trust

    public ProtectionManager(BlockLockerPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
        this.dataFile = plugin.getPluginContainer().dataFolder().resolve("protected_blocks.json");
        this.protectedBlocks = new ConcurrentHashMap<>();
        this.lockModePlayers = new ConcurrentHashMap<>();
        this.unlockModePlayers = new ConcurrentHashMap<>();
        this.trustModePlayers = new ConcurrentHashMap<>();

        loadData();
    }

    /**
     * Load protected blocks from disk.
     */
    private void loadData() {
        File file = dataFile.toFile();
        if (!file.exists()) {
            plugin.getPluginLogger().info("No existing protection data found. Starting fresh.");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            List<ProtectedBlock> blocks = gson.fromJson(reader, new TypeToken<List<ProtectedBlock>>() {}.getType());
            if (blocks != null) {
                for (ProtectedBlock block : blocks) {
                    protectedBlocks.put(block.getLocationKey(), block);
                }
            }
            plugin.getPluginLogger().info("Loaded " + protectedBlocks.size() + " protected blocks.");
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to load protection data: " + e.getMessage());
        }
    }

    /**
     * Save all protected blocks to disk.
     */
    public synchronized void saveAll() {
        try {
            File file = dataFile.toFile();
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(new ArrayList<>(protectedBlocks.values()), writer);
            }
            plugin.getPluginLogger().info("Saved " + protectedBlocks.size() + " protected blocks.");
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save protection data: " + e.getMessage());
        }
    }

    /**
     * Protect a block at the given location.
     */
    public void protectBlock(String worldName, int x, int y, int z, UUID ownerUuid, String ownerName) {
        ProtectedBlock block = new ProtectedBlock(worldName, x, y, z, ownerUuid, ownerName);
        protectedBlocks.put(block.getLocationKey(), block);
        saveAll();
    }

    /**
     * Remove protection from a block.
     */
    public void unprotectBlock(String worldName, int x, int y, int z) {
        String key = worldName + ":" + x + ":" + y + ":" + z;
        protectedBlocks.remove(key);
        saveAll();
    }

    /**
     * Get protection info for a block.
     */
    public ProtectedBlock getProtection(String worldName, int x, int y, int z) {
        String key = worldName + ":" + x + ":" + y + ":" + z;
        return protectedBlocks.get(key);
    }

    /**
     * Check if a block is protected.
     */
    public boolean isProtected(String worldName, int x, int y, int z) {
        return getProtection(worldName, x, y, z) != null;
    }

    /**
     * Check if a player can access a protected block.
     */
    public boolean canAccess(String worldName, int x, int y, int z, UUID playerUuid) {
        ProtectedBlock block = getProtection(worldName, x, y, z);
        if (block == null) {
            return true; // Not protected, anyone can access
        }
        return block.hasAccess(playerUuid);
    }

    /**
     * Check if a player is the owner of a protected block.
     */
    public boolean isOwner(String worldName, int x, int y, int z, UUID playerUuid) {
        ProtectedBlock block = getProtection(worldName, x, y, z);
        if (block == null) {
            return false;
        }
        return block.isOwner(playerUuid);
    }

    /**
     * Get all protections owned by a player.
     */
    public List<ProtectedBlock> getPlayerProtections(UUID playerUuid) {
        List<ProtectedBlock> result = new ArrayList<>();
        for (ProtectedBlock block : protectedBlocks.values()) {
            if (block.isOwner(playerUuid)) {
                result.add(block);
            }
        }
        return result;
    }

    /**
     * Add a trusted player to a block.
     */
    public void addTrustedPlayer(String worldName, int x, int y, int z, UUID trustedUuid) {
        ProtectedBlock block = getProtection(worldName, x, y, z);
        if (block != null) {
            block.addTrustedPlayer(trustedUuid);
            saveAll();
        }
    }

    /**
     * Remove a trusted player from a block.
     */
    public void removeTrustedPlayer(String worldName, int x, int y, int z, UUID trustedUuid) {
        ProtectedBlock block = getProtection(worldName, x, y, z);
        if (block != null) {
            block.removeTrustedPlayer(trustedUuid);
            saveAll();
        }
    }

    // Lock mode management
    public void enableLockMode(UUID playerUuid) {
        lockModePlayers.put(playerUuid, true);
        unlockModePlayers.remove(playerUuid);
        trustModePlayers.remove(playerUuid);
    }

    public void disableLockMode(UUID playerUuid) {
        lockModePlayers.remove(playerUuid);
    }

    public boolean isInLockMode(UUID playerUuid) {
        return lockModePlayers.containsKey(playerUuid);
    }

    // Unlock mode management
    public void enableUnlockMode(UUID playerUuid) {
        unlockModePlayers.put(playerUuid, true);
        lockModePlayers.remove(playerUuid);
        trustModePlayers.remove(playerUuid);
    }

    public void disableUnlockMode(UUID playerUuid) {
        unlockModePlayers.remove(playerUuid);
    }

    public boolean isInUnlockMode(UUID playerUuid) {
        return unlockModePlayers.containsKey(playerUuid);
    }

    // Trust mode management
    public void enableTrustMode(UUID playerUuid, UUID targetUuid) {
        trustModePlayers.put(playerUuid, targetUuid);
        lockModePlayers.remove(playerUuid);
        unlockModePlayers.remove(playerUuid);
    }

    public void disableTrustMode(UUID playerUuid) {
        trustModePlayers.remove(playerUuid);
    }

    public boolean isInTrustMode(UUID playerUuid) {
        return trustModePlayers.containsKey(playerUuid);
    }

    public UUID getTrustTarget(UUID playerUuid) {
        return trustModePlayers.get(playerUuid);
    }

    /**
     * Clear all mode settings for a player.
     */
    public void clearModes(UUID playerUuid) {
        lockModePlayers.remove(playerUuid);
        unlockModePlayers.remove(playerUuid);
        trustModePlayers.remove(playerUuid);
    }
}

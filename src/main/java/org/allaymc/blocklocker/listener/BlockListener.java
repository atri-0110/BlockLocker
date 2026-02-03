package org.allaymc.blocklocker.listener;

import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.block.BlockBreakEvent;
import org.allaymc.api.eventbus.event.player.PlayerInteractBlockEvent;
import org.allaymc.api.permission.Tristate;
import org.allaymc.api.world.Dimension;
import org.allaymc.blocklocker.data.ProtectedBlock;
import org.allaymc.blocklocker.manager.ProtectionManager;
import org.allaymc.blocklocker.util.BlockUtils;

import java.util.UUID;

/**
 * Event listener for block protection functionality.
 */
public class BlockListener {

    private final ProtectionManager protectionManager;

    public BlockListener(ProtectionManager protectionManager) {
        this.protectionManager = protectionManager;
    }

    /**
     * Handle block interactions (right-clicking).
     */
    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractBlockEvent event) {
        EntityPlayer player = event.getPlayer();

        // Check for bypass permission
        if (player.hasPermission("blocklocker.bypass") == Tristate.TRUE) {
            return;
        }

        // Get the location from the player (current dimension)
        Dimension dimension = player.getDimension();
        var interactInfo = event.getInteractInfo();
        var pos = interactInfo.clickedBlockPos();

        String worldName = player.getWorld().getWorldData().getDisplayName();
        int dimensionId = dimension.getDimensionInfo().dimensionId();
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();

        // Check if player is in lock mode
        if (protectionManager.isInLockMode(player.getUniqueId())) {
            event.setCancelled(true);
            handleLockMode(player, dimension, dimensionId, x, y, z);
            return;
        }

        // Check if player is in unlock mode
        if (protectionManager.isInUnlockMode(player.getUniqueId())) {
            event.setCancelled(true);
            handleUnlockMode(player, worldName, dimensionId, x, y, z);
            return;
        }

        // Check if player is in trust mode
        if (protectionManager.isInTrustMode(player.getUniqueId())) {
            event.setCancelled(true);
            handleTrustMode(player, worldName, dimensionId, x, y, z);
            return;
        }

        // Check if block is protected
        ProtectedBlock protection = protectionManager.getProtection(worldName, dimensionId, x, y, z);
        if (protection != null) {
            // Allow owner and trusted players
            if (!protection.hasAccess(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("§cThis block is locked by " + protection.getOwnerName());
            }
        }
    }

    /**
     * Handle block breaks.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer player)) {
            return;
        }

        // Check for bypass permission
        if (player.hasPermission("blocklocker.bypass") == Tristate.TRUE) {
            return;
        }

        var block = event.getBlock();
        var pos = block.getPosition();

        String worldName = block.getDimension().getWorld().getWorldData().getDisplayName();
        int dimensionId = block.getDimension().getDimensionInfo().dimensionId();
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();

        ProtectedBlock protection = protectionManager.getProtection(worldName, dimensionId, x, y, z);
        if (protection != null) {
            // Only owner can break protected blocks
            if (!protection.isOwner(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot break a block locked by " + protection.getOwnerName());
            } else {
                // Owner breaking their own block - remove protection
                protectionManager.unprotectBlock(worldName, dimensionId, x, y, z);
                player.sendMessage("§aProtection removed from block.");
            }
        }
    }

    /**
     * Handle lock mode interaction.
     */
    private void handleLockMode(EntityPlayer player, Dimension dimension, int dimensionId, int x, int y, int z) {
        String blockId = dimension.getBlockState(x, y, z).getBlockType().getIdentifier().toString();

        if (!BlockUtils.isProtectableBlock(blockId)) {
            player.sendMessage("§cThis block cannot be locked. Only containers, doors, and valuable blocks can be protected.");
            protectionManager.disableLockMode(player.getUniqueId());
            return;
        }

        String worldName = player.getWorld().getWorldData().getDisplayName();

        // Check if already protected
        if (protectionManager.isProtected(worldName, dimensionId, x, y, z)) {
            player.sendMessage("§cThis block is already locked.");
            protectionManager.disableLockMode(player.getUniqueId());
            return;
        }

        // Get player display name
        String playerName = player.getController() != null
                ? player.getController().getOriginName()
                : player.getDisplayName();

        // Protect the block
        protectionManager.protectBlock(worldName, dimensionId, x, y, z, player.getUniqueId(), playerName);
        player.sendMessage("§aBlock locked successfully! Only you and trusted players can access it.");

        protectionManager.disableLockMode(player.getUniqueId());
    }

    /**
     * Handle unlock mode interaction.
     */
    private void handleUnlockMode(EntityPlayer player, String worldName, int dimensionId, int x, int y, int z) {
        ProtectedBlock protection = protectionManager.getProtection(worldName, dimensionId, x, y, z);

        if (protection == null) {
            player.sendMessage("§cThis block is not locked.");
            protectionManager.disableUnlockMode(player.getUniqueId());
            return;
        }

        if (!protection.isOwner(player.getUniqueId())) {
            player.sendMessage("§cOnly the owner can unlock this block.");
            protectionManager.disableUnlockMode(player.getUniqueId());
            return;
        }

        protectionManager.unprotectBlock(worldName, dimensionId, x, y, z);
        player.sendMessage("§aBlock unlocked successfully!");

        protectionManager.disableUnlockMode(player.getUniqueId());
    }

    /**
     * Handle trust mode interaction.
     */
    private void handleTrustMode(EntityPlayer player, String worldName, int dimensionId, int x, int y, int z) {
        ProtectedBlock protection = protectionManager.getProtection(worldName, dimensionId, x, y, z);

        if (protection == null) {
            player.sendMessage("§cThis block is not locked.");
            protectionManager.disableTrustMode(player.getUniqueId());
            return;
        }

        if (!protection.isOwner(player.getUniqueId())) {
            player.sendMessage("§cOnly the owner can add trusted players.");
            protectionManager.disableTrustMode(player.getUniqueId());
            return;
        }

        UUID targetUuid = protectionManager.getTrustTarget(player.getUniqueId());
        if (targetUuid == null) {
            player.sendMessage("§cError: No target player specified.");
            protectionManager.disableTrustMode(player.getUniqueId());
            return;
        }

        // Check if already trusted
        if (protection.isTrusted(targetUuid)) {
            // Untrust (remove)
            protectionManager.removeTrustedPlayer(worldName, dimensionId, x, y, z, targetUuid);
            player.sendMessage("§aPlayer removed from trusted list.");
        } else {
            // Trust (add)
            protectionManager.addTrustedPlayer(worldName, dimensionId, x, y, z, targetUuid);
            player.sendMessage("§aPlayer added to trusted list.");
        }

        protectionManager.disableTrustMode(player.getUniqueId());
    }
}

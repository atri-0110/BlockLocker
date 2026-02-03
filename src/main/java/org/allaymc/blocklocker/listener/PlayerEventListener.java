package org.allaymc.blocklocker.listener;

import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.server.PlayerQuitEvent;
import org.allaymc.blocklocker.BlockLockerPlugin;

/**
 * Player event listener for cleaning up player data.
 */
public class PlayerEventListener {

    private final BlockLockerPlugin plugin;

    public PlayerEventListener(BlockLockerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Clean up player mode settings when they disconnect.
     * This prevents memory leaks from lock/unlock/trust mode states.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (player.getLoginData() == null) {
            return;
        }

        var uuid = player.getLoginData().getUuid();
        plugin.getProtectionManager().cleanupPlayer(uuid);
    }
}

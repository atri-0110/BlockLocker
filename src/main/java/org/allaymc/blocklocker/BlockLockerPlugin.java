package org.allaymc.blocklocker;

import lombok.Getter;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.blocklocker.command.BlockLockerCommand;
import org.allaymc.blocklocker.listener.BlockListener;
import org.allaymc.blocklocker.manager.ProtectionManager;

/**
 * BlockLocker - A block protection and locking system for AllayMC servers.
 * Allows players to lock chests, doors, furnaces, and other containers to prevent theft.
 *
 * @author atri-0110
 * @version 0.1.0
 */
public class BlockLockerPlugin extends Plugin {

    @Getter
    private static BlockLockerPlugin instance;

    @Getter
    private ProtectionManager protectionManager;

    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("BlockLocker is loading...");
    }

    @Override
    public void onEnable() {
        // Initialize protection manager
        this.protectionManager = new ProtectionManager(this);

        // Register commands
        Registries.COMMANDS.register(new BlockLockerCommand());

        // Register event listeners
        Server.getInstance().getEventBus().registerListener(new BlockListener(protectionManager));

        this.pluginLogger.info("BlockLocker has been enabled! Players can now lock their blocks.");
    }

    @Override
    public void onDisable() {
        if (protectionManager != null) {
            protectionManager.saveAll();
        }
        this.pluginLogger.info("BlockLocker has been disabled.");
    }
}

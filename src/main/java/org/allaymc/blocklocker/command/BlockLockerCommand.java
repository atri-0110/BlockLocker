package org.allaymc.blocklocker.command;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.permission.Tristate;
import org.allaymc.api.player.Player;
import org.allaymc.api.server.Server;
import org.allaymc.blocklocker.BlockLockerPlugin;
import org.allaymc.blocklocker.data.ProtectedBlock;
import org.allaymc.blocklocker.manager.ProtectionManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main command handler for BlockLocker plugin.
 */
public class BlockLockerCommand extends Command {

    public BlockLockerCommand() {
        super("blocklocker", "Block protection commands", "blocklocker.use");
        aliases.add("bl");
        aliases.add("lock");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
            // /blocklocker lock
            .key("lock")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return context.fail();
                }

                if (player.hasPermission("blocklocker.use") != Tristate.TRUE) {
                    player.sendMessage("§cYou don't have permission to use this command!");
                    return context.fail();
                }

                ProtectionManager manager = BlockLockerPlugin.getInstance().getProtectionManager();
                manager.enableLockMode(player.getUniqueId());
                player.sendMessage("§aLock mode enabled! Right-click a block to lock it.");
                player.sendMessage("§7Only chests, doors, furnaces, and containers can be locked.");
                return context.success();
            })
            .root()
            // /blocklocker unlock
            .key("unlock")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return context.fail();
                }

                if (player.hasPermission("blocklocker.use") != Tristate.TRUE) {
                    player.sendMessage("§cYou don't have permission to use this command!");
                    return context.fail();
                }

                ProtectionManager manager = BlockLockerPlugin.getInstance().getProtectionManager();
                manager.enableUnlockMode(player.getUniqueId());
                player.sendMessage("§aUnlock mode enabled! Right-click a locked block to unlock it.");
                return context.success();
            })
            .root()
            // /blocklocker trust <player>
            .key("trust")
            .str("target")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return context.fail();
                }

                if (player.hasPermission("blocklocker.use") != Tristate.TRUE) {
                    player.sendMessage("§cYou don't have permission to use this command!");
                    return context.fail();
                }

                String targetName = context.getResult(1);
                return handleTrust(player, targetName, true, context);
            })
            .root()
            // /blocklocker untrust <player>
            .key("untrust")
            .str("target")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return context.fail();
                }

                if (player.hasPermission("blocklocker.use") != Tristate.TRUE) {
                    player.sendMessage("§cYou don't have permission to use this command!");
                    return context.fail();
                }

                String targetName = context.getResult(1);
                return handleTrust(player, targetName, false, context);
            })
            .root()
            // /blocklocker info
            .key("info")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return context.fail();
                }

                ProtectionManager manager = BlockLockerPlugin.getInstance().getProtectionManager();
                sendInfo(player, manager);
                return context.success();
            })
            .root()
            // /blocklocker list
            .key("list")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return context.fail();
                }

                ProtectionManager manager = BlockLockerPlugin.getInstance().getProtectionManager();
                sendInfo(player, manager);
                return context.success();
            })
            .root()
            // /blocklocker help
            .key("help")
            .exec(context -> {
                sendHelp(context.getSender());
                return context.success();
            })
            .root()
            // /blocklocker (no args) - show help
            .exec(context -> {
                sendHelp(context.getSender());
                return context.success();
            });
    }

    private org.allaymc.api.command.CommandResult handleTrust(EntityPlayer player, String targetName, boolean isTrust,
                                                               org.allaymc.api.command.tree.CommandContext context) {
        ProtectionManager manager = BlockLockerPlugin.getInstance().getProtectionManager();

        // Find target player using PlayerManager
        Player targetPlayer = Server.getInstance().getPlayerManager().getPlayerByName(targetName);
        
        if (targetPlayer == null) {
            player.sendMessage("§cPlayer '" + targetName + "' not found. They must be online to trust them.");
            return context.fail();
        }

        UUID targetUuid = targetPlayer.getLoginData().getUuid();

        if (targetUuid.equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot trust yourself!");
            return context.fail();
        }

        // Enable trust mode
        manager.enableTrustMode(player.getUniqueId(), targetUuid);
        player.sendMessage("§aTrust mode enabled! Right-click a locked block to " +
                (isTrust ? "add" : "remove") + " " + targetName + ".");
        return context.success();
    }

    private void sendInfo(EntityPlayer player, ProtectionManager manager) {
        List<ProtectedBlock> protections = manager.getPlayerProtections(player.getUniqueId());

        player.sendMessage("§6===== BlockLocker Info =====");
        player.sendMessage("§eYour protected blocks: §f" + protections.size());

        if (protections.isEmpty()) {
            player.sendMessage("§7You don't have any locked blocks.");
            player.sendMessage("§7Use §f/blocklocker lock §7to lock a block.");
        } else {
            player.sendMessage("§7Your locked blocks:");
            int count = 0;
            for (ProtectedBlock block : protections) {
                if (count >= 10) {
                    player.sendMessage("§7... and " + (protections.size() - 10) + " more");
                    break;
                }
                player.sendMessage(String.format("§8- §7%s at %d, %d, %d (%s trusted)",
                        block.getWorldName(), block.getX(), block.getY(), block.getZ(),
                        block.getTrustedPlayers().size()));
                count++;
            }
        }

        player.sendMessage("§6==========================");
    }

    private void sendHelp(org.allaymc.api.command.CommandSender sender) {
        sender.sendMessage("§6===== BlockLocker Commands =====");
        sender.sendMessage("§e/blocklocker lock §7- Enable lock mode, then right-click a block");
        sender.sendMessage("§e/blocklocker unlock §7- Enable unlock mode, then right-click a block");
        sender.sendMessage("§e/blocklocker trust <player> §7- Enable trust mode to add a player");
        sender.sendMessage("§e/blocklocker untrust <player> §7- Enable trust mode to remove a player");
        sender.sendMessage("§e/blocklocker info §7- Show your protection statistics");
        sender.sendMessage("§e/blocklocker list §7- List your protected blocks");
        sender.sendMessage("§e/blocklocker help §7- Show this help message");
        sender.sendMessage("§6================================");
        sender.sendMessage("§7Protectable blocks: chests, doors, furnaces, hoppers, dispensers, barrels, anvils, enchanting tables, beacons");
    }
}

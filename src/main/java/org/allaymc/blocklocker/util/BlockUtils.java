package org.allaymc.blocklocker.util;

import org.allaymc.api.block.type.BlockTypes;

/**
 * Utility class for block-related operations.
 */
public class BlockUtils {

    /**
     * Check if a block can be protected (locked).
     * Only certain block types can be locked for balance and gameplay reasons.
     */
    public static boolean isProtectableBlock(String blockId) {
        if (blockId == null) return false;

        // Chests
        if (blockId.contains("chest")) return true;

        // Doors
        if (blockId.contains("door")) return true;

        // Furnaces and brewing
        if (blockId.contains("furnace")) return true;
        if (blockId.contains("brewing_stand")) return true;

        // Hoppers and droppers
        if (blockId.contains("hopper")) return true;
        if (blockId.contains("dropper")) return true;
        if (blockId.contains("dispenser")) return true;

        // Barrels
        if (blockId.contains("barrel")) return true;

        // Shulker boxes
        if (blockId.contains("shulker_box")) return true;

        // Anvils
        if (blockId.contains("anvil")) return true;

        // Enchanting table
        if (blockId.contains("enchanting_table")) return true;

        // Beacon
        if (blockId.contains("beacon")) return true;

        // Ender chest
        if (blockId.contains("ender_chest")) return true;

        // Trapped chest
        if (blockId.contains("trapped_chest")) return true;

        return false;
    }

    /**
     * Get a friendly display name for a block ID.
     */
    public static String getBlockDisplayName(String blockId) {
        if (blockId == null) return "Unknown";

        // Remove namespace if present
        String name = blockId;
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }

        // Replace underscores with spaces and title case
        name = name.replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
}

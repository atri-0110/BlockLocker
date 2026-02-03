# BlockLocker

A block protection and locking system for AllayMC servers that allows players to lock their chests, doors, and valuable blocks to prevent theft and griefing.

## Features

- **Block Protection**: Lock chests, doors, furnaces, hoppers, dispensers, and other valuable blocks
- **Trust System**: Add trusted players who can access your locked blocks
- **Easy Management**: Simple lock/unlock commands with click-to-interact workflow
- **Visual Feedback**: Clear messages and indicators for protected blocks
- **Persistent Storage**: All protection data is saved to JSON files
- **Cross-Dimension Support**: Works in Overworld, Nether, and End dimensions

## Protectable Blocks

The following blocks can be locked:
- Chests (including trapped chests and ender chests)
- Doors (all types)
- Furnaces (including blast furnaces and smokers)
- Brewing stands
- Hoppers
- Dispensers and droppers
- Barrels
- Shulker boxes
- Anvils
- Enchanting tables
- Beacons

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/blocklocker lock` | `blocklocker.use` | Enable lock mode - right-click a block to lock it |
| `/blocklocker unlock` | `blocklocker.use` | Enable unlock mode - right-click a block to unlock it |
| `/blocklocker trust <player>` | `blocklocker.use` | Enable trust mode to add a player to a block |
| `/blocklocker untrust <player>` | `blocklocker.use` | Enable trust mode to remove a player from a block |
| `/blocklocker info` | `blocklocker.use` | Show your protection statistics |
| `/blocklocker list` | `blocklocker.use` | List all your protected blocks |
| `/blocklocker help` | `blocklocker.use` | Show help message |

**Aliases**: `/bl`, `/lock`

## How to Use

### Locking a Block

1. Run `/blocklocker lock` (or `/lock lock`)
2. Right-click on a protectable block (chest, door, etc.)
3. The block is now locked - only you and trusted players can access it

### Unlocking a Block

1. Run `/blocklocker unlock`
2. Right-click on one of your locked blocks
3. The protection is removed

### Trusting a Player

1. Run `/blocklocker trust <playername>`
2. Right-click on a locked block
3. That player can now access the block

### Untrusting a Player

1. Run `/blocklocker untrust <playername>`
2. Right-click on a locked block
3. That player can no longer access the block

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `blocklocker.use` | Access to all BlockLocker commands | Everyone |
| `blocklocker.bypass` | Bypass all protections (admin) | OP only |
| `blocklocker.admin` | Access to admin commands | OP only |

## Installation

1. Download the latest `BlockLocker-0.1.0-shaded.jar` from [Releases](https://github.com/atri-0110/BlockLocker/releases)
2. Place the JAR file in your server's `plugins/` directory
3. Start or restart the server
4. The plugin will create a `protected_blocks.json` file in `plugins/BlockLocker/`

## Building from Source

```bash
./gradlew shadowJar -Dorg.gradle.jvmargs="-Xmx3g"
```

The compiled JAR will be in `build/libs/BlockLocker-0.1.0-shaded.jar`

## Configuration

Protection data is stored in `plugins/BlockLocker/protected_blocks.json` in JSON format.

## Requirements

- AllayMC Server with API 0.24.0 or higher
- Java 21 or higher

## Technical Details

- Thread-safe protection storage using ConcurrentHashMap
- Automatic data saving when blocks are locked/unlocked
- Efficient location-based lookup for quick access checks
- Handles all container access events including hoppers and redstone

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

- **atri-0110** - [GitHub](https://github.com/atri-0110)

## Support

For support, feature requests, or bug reports, please open an issue on [GitHub](https://github.com/atri-0110/BlockLocker/issues).

## Credits

- Built for [AllayMC](https://github.com/AllayMC/Allay) Server Software
- Inspired by the need for simple, effective block protection on Minecraft servers

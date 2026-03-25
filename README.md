# SoulStreak Plugin for Minecraft 1.21.11 Paper

A PvP streak system with tier-based bonuses and heart management for Minecraft Paper servers.

## Features

- **Kill Streak System**: Track player kills with persistent data storage
- **Tier-Based Rewards**: Unlock bonuses as you build your streak
- **Heart Management**: Craft and withdraw hearts for health management
- **Persistent Data**: Heart and streak data survive server restarts

## Streak Tiers

| Kills | Tier | Benefits |
|-------|------|----------|
| 0-2 | No Streak | Default health (20 hearts) |
| 3-4 | Tier 1 | Entry milestone |
| 5-9 | Tier 2 | +2 hearts + Fire Resistance (24 total) |
| 10-14 | Tier 3 | +3 hearts + Fire Resistance + Strength 1 (26 total) |
| 15+ | Tier 4 | +5 hearts + Fire Resistance + Strength 1 + Speed 1 (30 total) |

## Heart Mechanics

- **Heart Crafting**: Craft hearts using Diamond + Redstone
- **Heart Consumption**: Right-click hearts to gain +1 heart
- **Heart Withdrawal**: Withdraw bonus hearts as items with `/streak withdraw`
- **Persistent Storage**: Heart data survives server restarts and relogs

## Commands

- `/streak` - Check your current kill streak and tier
- `/streak help` - Show available commands
- `/streak withdraw` - Withdraw bonus hearts as items (requires permission)

## Permissions
- `soulstreak.use` - Use basic streak commands (default: true)
- `soulstreak.withdraw` - Withdraw hearts as items (default: op)
- `soulstreak.admin` - Use administrative commands (default: op)

## Installation

1. **Build the plugin**:
   ```bash
   mvn clean package
   ```

2. **Install the plugin**:
   - Copy `target/soulstreak-1.0.0.jar` to your server's `plugins/` folder
   - Restart the server or run `/reload`

3. **Requirements**:
   - Minecraft 1.21.11 Paper server
   - Java 21 or higher

## Configuration

The plugin includes configurable settings in `config.yml`:
- `streak-loss-on-death`: Amount of streak lost on death (default: 3)
- `lose-heart-on-no-streak-death`: Whether players lose hearts on death with no streak (default: true)
- `max-hearts`: Maximum hearts players can have (default: 10)
- `natural-deaths-affect-nothing`: Whether natural deaths affect streak/hearts (default: true)

## Technical Details

- Uses Bukkit's Persistent Data API for streak storage
- Compatible with Minecraft 1.21.11 API
- Built with Maven for easy compilation
- No external dependencies required

## License

This plugin is open source. Feel free to modify and distribute.

## Support

For issues or feature requests, please contact the plugin author.

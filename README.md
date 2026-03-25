# StreakSoul Plugin for Minecraft 1.21.11 Paper

A PvP streak system with tier-based bonuses and soul drops for Minecraft Paper servers.

## Features

- **Kill Streak System**: Track player kills with persistent data storage
- **Tier-Based Rewards**: Unlock bonuses as you build your streak
- **Soul Drops**: Defeat high-tier players to collect their souls
- **Right-Click Souls**: Consume souls for instant streak bonuses

## Streak Tiers

| Kills | Tier | Benefits |
|-------|------|----------|
| 0-2 | No Streak | Default health (20 hearts) |
| 3-4 | Tier 1 | +2 hearts (24 total) |
| 5-9 | Tier 2 | +3 hearts + Fire Resistance (26 total) |
| 10+ | God Tier | +5 hearts + Fire Resistance + Strength (30 total) |

## Soul Mechanics

- Players with 10+ kills drop a **Green Flame Soul** when killed
- Souls are Phantom Membrane items with custom lore
- Right-click a soul to gain **+3 instant kills** to your streak
- Souls are stackable and consumable

## Commands

- `/streak` - Check your current kill streak and tier

## Installation

1. **Build the plugin**:
   ```bash
   mvn clean package
   ```

2. **Install the plugin**:
   - Copy `target/streaksoul-1.0.0.jar` to your server's `plugins/` folder
   - Restart the server or run `/reload`

3. **Requirements**:
   - Minecraft 1.21.1 Paper server
   - Java 21 or higher

## Configuration

The plugin includes basic permissions in `plugin.yml`:
- `streaksoul.use` - Use streak commands (default: true)
- `streaksoul.admin` - Administrative permissions (default: op)

## Technical Details

- Uses Bukkit's Persistent Data API for streak storage
- Compatible with Minecraft 1.21.1 API
- Built with Maven for easy compilation
- No external dependencies required

## License

This plugin is open source. Feel free to modify and distribute.

## Support

For issues or feature requests, please contact the plugin author.

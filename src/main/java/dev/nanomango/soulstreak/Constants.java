package dev.nanomango.soulstreak;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

/**
 * Constants class for SoulStreak plugin
 * Contains all magic numbers, string literals, and configuration values
 */
public final class Constants {
    
    // Health constants
    public static final double DEFAULT_HEALTH = 20.0;
    public static final double MIN_HEALTH = 4.0;
    public static final double ONE_HEART = 2.0;
    
    // Streak constants
    public static final int MAX_STREAK = 15;
    public static final int DEFAULT_STREAK_LOSS = 3;
    public static final int TIER_1_KILLS = 3;
    public static final int TIER_2_KILLS = 5;
    public static final int TIER_3_KILLS = 10;
    public static final int TIER_4_KILLS = 15;
    
    // Tier health values
    public static final double TIER_2_HEALTH = 24.0; // +2 hearts
    public static final double TIER_3_HEALTH = 26.0; // +3 hearts
    public static final double TIER_4_HEALTH = 30.0; // +5 hearts
    
    // Heart loss probabilities
    public static final double ONE_HEART_LOSS_CHANCE = 0.35;
    public static final double TWO_HEARTS_LOSS_CHANCE = 0.65;
    
    // Material constants
    public static final Material HEART_MATERIAL = Material.RED_DYE;
    public static final Material CRAFTING_DIAMOND = Material.DIAMOND;
    public static final Material CRAFTING_REDSTONE = Material.REDSTONE;
    
    // Potion effect types
    public static final PotionEffectType[] TIER_EFFECTS = {
        PotionEffectType.FIRE_RESISTANCE,
        PotionEffectType.STRENGTH,
        PotionEffectType.SPEED
    };
    
    // Message prefixes and colors
    public static final String ERROR_PREFIX = ChatColor.RED + "";
    public static final String SUCCESS_PREFIX = ChatColor.GREEN + "";
    public static final String INFO_PREFIX = ChatColor.YELLOW + "";
    public static final String TIER_1_COLOR = ChatColor.GREEN + "";
    public static final String TIER_2_COLOR = ChatColor.AQUA + "";
    public static final String TIER_3_COLOR = ChatColor.DARK_RED + "" + ChatColor.BOLD;
    public static final String TIER_4_COLOR = ChatColor.GOLD + "" + ChatColor.BOLD;
    public static final String NO_STREAK_COLOR = ChatColor.GRAY + "";
    
    // Message templates
    public static final String NO_PERMISSION = "You don't have permission to use this command.";
    public static final String PLAYERS_ONLY = "This command can only be used by players.";
    public static final String PLAYER_NOT_FOUND = "Player '%s' not found.";
    public static final String INVALID_NUMBER = "Invalid number format.";
    public static final String POSITIVE_NUMBER_REQUIRED = "Amount must be a positive number.";
    public static final String UNKNOWN_SUBCOMMAND = "Unknown subcommand. Use /streak help for available commands.";
    
    // Command names
    public static final String MAIN_COMMAND = "streak";
    public static final String HELP_SUBCOMMAND = "help";
    public static final String WITHDRAW_SUBCOMMAND = "withdraw";
    public static final String GIVE_SUBCOMMAND = "give";
    public static final String SETSTREAK_SUBCOMMAND = "setstreak";
    public static final String ADDHEARTS_SUBCOMMAND = "addhearts";
    public static final String TOGGLE_SUBCOMMAND = "toggle";
    public static final String CONFIG_SUBCOMMAND = "config";
    public static final String RELOAD_SUBCOMMAND = "reload";
    public static final String RESET_SUBCOMMAND = "reset";
    
    // Reset types
    public static final String RESET_EFFECTS = "effects";
    public static final String RESET_STREAK = "streak";
    public static final String RESET_HEARTS = "hearts";
    public static final String RESET_ALL = "all";
    
    // Toggle settings
    public static final String TOGGLE_NATURAL_DEATHS = "naturaldeaths";
    public static final String TOGGLE_HEART_LOSS = "heartloss";
    
    // Config keys
    public static final String CONFIG_STREAK_LOSS = "streak-loss-on-death";
    public static final String CONFIG_LOSE_HEART = "lose-heart-on-no-streak-death";
    public static final String CONFIG_MAX_HEARTS = "max-hearts";
    public static final String CONFIG_NATURAL_DEATHS = "natural-deaths-affect-nothing";
    
    // Item names and lore
    public static final String HEART_NAME = ChatColor.RED + "" + ChatColor.BOLD + "Heart";
    public static final String BONUS_HEART_NAME = ChatColor.RED + "" + ChatColor.BOLD + "Bonus Heart";
    public static final String HEART_LORE_1 = ChatColor.GRAY + "A precious heart that can restore";
    public static final String HEART_LORE_2 = ChatColor.GRAY + "1 heart when consumed.";
    public static final String HEART_LORE_3 = ChatColor.WHITE + "Right-click to use.";
    public static final String BONUS_HEART_LORE_1 = ChatColor.GRAY + "A withdrawn bonus heart.";
    public static final String BONUS_HEART_LORE_2 = ChatColor.YELLOW + "Right-click to restore 2 health points.";
    
    // Tab update intervals (ticks)
    public static final long TAB_UPDATE_INTERVAL = 20L; // 1 second
    
    // Recipe pattern
    public static final String HEART_RECIPE_PATTERN = " d drd  d ";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}

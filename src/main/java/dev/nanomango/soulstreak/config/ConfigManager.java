package dev.nanomango.soulstreak.config;

import org.bukkit.configuration.file.FileConfiguration;
import dev.nanomango.soulstreak.SoulStreak;
import dev.nanomango.soulstreak.Constants;

/**
 * Configuration manager for SoulStreak plugin
 * Handles loading, saving, and validating configuration values
 */
public class ConfigManager {
    
    private final SoulStreak plugin;
    private int streakLossOnDeath;
    private boolean loseHeartOnNoStreakDeath;
    private int maxHearts;
    private boolean naturalDeathsAffectNothing;
    
    public ConfigManager(SoulStreak plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }
    
    /**
     * Loads configuration from file
     */
    public void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();
        
        streakLossOnDeath = config.getInt(Constants.CONFIG_STREAK_LOSS, Constants.DEFAULT_STREAK_LOSS);
        loseHeartOnNoStreakDeath = config.getBoolean(Constants.CONFIG_LOSE_HEART, true);
        maxHearts = config.getInt(Constants.CONFIG_MAX_HEARTS, 10);
        naturalDeathsAffectNothing = config.getBoolean(Constants.CONFIG_NATURAL_DEATHS, true);
        
        // Validate and clamp values
        streakLossOnDeath = Math.max(0, Math.min(streakLossOnDeath, Constants.MAX_STREAK));
        maxHearts = Math.max(1, Math.min(maxHearts, 20));
    }
    
    /**
     * Saves current configuration to file
     */
    public void saveConfiguration() {
        FileConfiguration config = plugin.getConfig();
        
        config.set(Constants.CONFIG_STREAK_LOSS, streakLossOnDeath);
        config.set(Constants.CONFIG_LOSE_HEART, loseHeartOnNoStreakDeath);
        config.set(Constants.CONFIG_MAX_HEARTS, maxHearts);
        config.set(Constants.CONFIG_NATURAL_DEATHS, naturalDeathsAffectNothing);
        
        plugin.saveConfig();
    }
    
    /**
     * Sets a configuration value by key
     */
    public boolean setConfigValue(String key, String value) {
        try {
            switch (key.toLowerCase()) {
                case Constants.CONFIG_STREAK_LOSS:
                    int val = Integer.parseInt(value);
                    if (val >= 0 && val <= Constants.MAX_STREAK) {
                        streakLossOnDeath = val;
                        return true;
                    }
                    return false;
                    
                case Constants.CONFIG_LOSE_HEART:
                    loseHeartOnNoStreakDeath = Boolean.parseBoolean(value);
                    return true;
                    
                case Constants.CONFIG_MAX_HEARTS:
                    int hearts = Integer.parseInt(value);
                    if (hearts >= 1 && hearts <= 20) {
                        maxHearts = hearts;
                        return true;
                    }
                    return false;
                    
                case Constants.CONFIG_NATURAL_DEATHS:
                    naturalDeathsAffectNothing = Boolean.parseBoolean(value);
                    return true;
                    
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Getters
    public int getStreakLossOnDeath() { return streakLossOnDeath; }
    public boolean isLoseHeartOnNoStreakDeath() { return loseHeartOnNoStreakDeath; }
    public int getMaxHearts() { return maxHearts; }
    public boolean isNaturalDeathsAffectNothing() { return naturalDeathsAffectNothing; }
    
    // Setters
    public void setStreakLossOnDeath(int streakLossOnDeath) { this.streakLossOnDeath = streakLossOnDeath; }
    public void setLoseHeartOnNoStreakDeath(boolean loseHeartOnNoStreakDeath) { this.loseHeartOnNoStreakDeath = loseHeartOnNoStreakDeath; }
    public void setMaxHearts(int maxHearts) { this.maxHearts = maxHearts; }
    public void setNaturalDeathsAffectNothing(boolean naturalDeathsAffectNothing) { this.naturalDeathsAffectNothing = naturalDeathsAffectNothing; }
}

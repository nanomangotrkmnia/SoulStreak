package dev.nanomango.soulstreak.tiers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import dev.nanomango.soulstreak.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player tier effects and efficiently updates only changed effects
 */
public class TierManager {
    
    // Cache of current player tiers to detect changes
    private final Map<Player, Integer> playerTiers = new ConcurrentHashMap<>();
    // Cache of current player effects to avoid unnecessary updates
    private final Map<Player, Map<PotionEffectType, Integer>> playerEffects = new ConcurrentHashMap<>();
    
    /**
     * Applies tier effects to a player, only updating what has changed
     */
    public void applyTierEffects(Player player, int streak) {
        int newTier = calculateTier(streak);
        int currentTier = playerTiers.getOrDefault(player, 0);
        
        if (newTier == currentTier) {
            return; // No tier change, no need to update effects
        }
        
        // Update tier cache
        playerTiers.put(player, newTier);
        
        // Calculate target effects for new tier
        Map<PotionEffectType, Integer> targetEffects = calculateTargetEffects(newTier);
        Map<PotionEffectType, Integer> currentEffects = playerEffects.getOrDefault(player, new HashMap<>());
        
        // Remove effects that are no longer needed
        for (PotionEffectType effectType : currentEffects.keySet()) {
            if (!targetEffects.containsKey(effectType)) {
                player.removePotionEffect(effectType);
            }
        }
        
        // Add or update effects that have changed
        for (Map.Entry<PotionEffectType, Integer> entry : targetEffects.entrySet()) {
            PotionEffectType effectType = entry.getKey();
            int amplifier = entry.getValue();
            
            if (!currentEffects.containsKey(effectType) || !currentEffects.get(effectType).equals(amplifier)) {
                player.addPotionEffect(new PotionEffect(effectType, -1, amplifier, false, false));
            }
        }
        
        // Update effects cache
        playerEffects.put(player, targetEffects);
        
        // Update player health based on tier
        updatePlayerHealth(player, newTier);
        
        // Send tier message
        sendTierMessage(player, newTier);
    }
    
    /**
     * Calculates the tier based on streak
     */
    private int calculateTier(int streak) {
        if (streak >= Constants.TIER_4_KILLS) return 4;
        if (streak >= Constants.TIER_3_KILLS) return 3;
        if (streak >= Constants.TIER_2_KILLS) return 2;
        if (streak >= Constants.TIER_1_KILLS) return 1;
        return 0;
    }
    
    /**
     * Calculates the target effects for a given tier
     */
    private Map<PotionEffectType, Integer> calculateTargetEffects(int tier) {
        Map<PotionEffectType, Integer> effects = new HashMap<>();
        
        switch (tier) {
            case 4: // Tier 4
                effects.put(PotionEffectType.FIRE_RESISTANCE, 0);
                effects.put(PotionEffectType.STRENGTH, 1);
                effects.put(PotionEffectType.SPEED, 0);
                break;
            case 3: // Tier 3
                effects.put(PotionEffectType.FIRE_RESISTANCE, 0);
                effects.put(PotionEffectType.STRENGTH, 1);
                break;
            case 2: // Tier 2
                effects.put(PotionEffectType.FIRE_RESISTANCE, 0);
                break;
            case 1: // Tier 1
                // No effects, just milestone
                break;
            default:
                // No effects
                break;
        }
        
        return effects;
    }
    
    /**
     * Updates player health based on tier
     */
    private void updatePlayerHealth(Player player, int tier) {
        double targetHealth;
        
        switch (tier) {
            case 4:
                targetHealth = Constants.TIER_4_HEALTH;
                break;
            case 3:
                targetHealth = Constants.TIER_3_HEALTH;
                break;
            case 2:
                targetHealth = Constants.TIER_2_HEALTH;
                break;
            default:
                targetHealth = Constants.DEFAULT_HEALTH;
                break;
        }
        
        // Only update if health needs to change
        if (player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
            double currentHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            if (Math.abs(currentHealth - targetHealth) > 0.1) {
                player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(targetHealth);
            }
        }
    }
    
    /**
     * Sends tier-up message to player
     */
    private void sendTierMessage(Player player, int tier) {
        String message;
        switch (tier) {
            case 4:
                message = Constants.TIER_4_COLOR + "TIER 4: Strength 1, Speed 1, Fire Resistance & +5 Hearts!";
                break;
            case 3:
                message = Constants.TIER_3_COLOR + "TIER 3: Strength 1, Fire Resistance & +3 Hearts!";
                break;
            case 2:
                message = Constants.TIER_2_COLOR + "Tier 2: Fire Resistance & +2 Hearts!";
                break;
            case 1:
                message = Constants.TIER_1_COLOR + "Tier 1: Reached 3 kills!";
                break;
            default:
                return; // No message for tier 0
        }
        player.sendMessage(message);
    }
    
    /**
     * Gets the tier name for display purposes
     */
    public String getTierName(int streak) {
        int tier = calculateTier(streak);
        
        switch (tier) {
            case 4: return Constants.TIER_4_COLOR + "[Tier 4]";
            case 3: return Constants.TIER_3_COLOR + "[Tier 3]";
            case 2: return Constants.TIER_2_COLOR + "[Tier 2]";
            case 1: return Constants.TIER_1_COLOR + "[Tier 1]";
            default:
                return streak >= 1 ? Constants.NO_STREAK_COLOR + "[Streaking]" : Constants.NO_STREAK_COLOR + "[No Streak]";
        }
    }
    
    /**
     * Clears player data when they leave
     */
    public void clearPlayerData(Player player) {
        playerTiers.remove(player);
        playerEffects.remove(player);
    }
    
    /**
     * Resets all effects for a player
     */
    public void resetPlayerEffects(Player player) {
        playerTiers.remove(player);
        playerEffects.remove(player);
        
        for (PotionEffectType effectType : Constants.TIER_EFFECTS) {
            player.removePotionEffect(effectType);
        }
    }
}

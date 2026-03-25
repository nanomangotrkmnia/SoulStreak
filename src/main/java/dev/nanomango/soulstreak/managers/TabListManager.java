package dev.nanomango.soulstreak.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;
import dev.nanomango.soulstreak.tiers.TierManager;
import dev.nanomango.soulstreak.Constants;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimized tab list manager that only updates when necessary
 */
public class TabListManager {
    
    private final TierManager tierManager;
    private final Set<Player> dirtyPlayers = ConcurrentHashMap.newKeySet();
    private final Object tabUpdateTask;
    
    public TabListManager(TierManager tierManager, Object tabUpdateTask) {
        this.tierManager = tierManager;
        this.tabUpdateTask = tabUpdateTask;
    }
    
    /**
     * Marks a player's tab list as needing an update
     */
    public void markDirty(Player player) {
        dirtyPlayers.add(player);
    }
    
    /**
     * Updates tab lists for all dirty players only
     */
    public void updateDirtyTabLists() {
        if (dirtyPlayers.isEmpty()) {
            return;
        }
        
        // Remove offline players from dirty set
        dirtyPlayers.removeIf(player -> !player.isOnline());
        
        // Update only dirty players
        for (Player player : dirtyPlayers) {
            updatePlayerTabList(player);
        }
        
        // Clear the dirty set
        dirtyPlayers.clear();
    }
    
    /**
     * Updates tab list for a specific player
     */
    private void updatePlayerTabList(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            int streak = player.getPersistentDataContainer()
                .getOrDefault(new NamespacedKey("soulstreak", "player_streak"), 
                             org.bukkit.persistence.PersistentDataType.INTEGER, 0);
            
            String tier = tierManager.getTierName(streak);
            String displayName = tier + " " + player.getName();
            
            // Limit length to prevent issues
            if (displayName.length() > 40) {
                displayName = displayName.substring(0, 40);
            }
            
            player.setPlayerListName(displayName);
        } catch (Exception e) {
            // Handle any exceptions gracefully
            // Could log this error if needed
        }
    }
    
    /**
     * Updates tab list for all online players (use sparingly)
     */
    public void updateAllTabLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            markDirty(player);
        }
    }
    
    /**
     * Cleans up player data when they leave
     */
    public void onPlayerQuit(Player player) {
        dirtyPlayers.remove(player);
        // Clean up tab list when player leaves
        if (player != null) {
            player.setPlayerListName(player.getName());
        }
    }
    
    /**
     * Starts the tab update task
     */
    public static Object startTabUpdateTask(TabListManager manager, boolean isFolia, org.bukkit.plugin.Plugin plugin) {
        if (isFolia) {
            // Use Folia scheduler
            try {
                Class<?> globalSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalScheduler");
                java.lang.reflect.Method getGlobalScheduler = globalSchedulerClass.getMethod("getGlobalScheduler", org.bukkit.plugin.Plugin.class);
                Object globalScheduler = getGlobalScheduler.invoke(null, plugin);
                
                Class<?> taskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
                java.lang.reflect.Method runAtFixedRate = globalSchedulerClass.getMethod("runAtFixedRate", 
                    org.bukkit.plugin.Plugin.class, 
                    Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask").getDeclaredClasses()[0], 
                    long.class, long.class);
                
                return runAtFixedRate.invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) task -> {
                    manager.updateDirtyTabLists();
                }, Constants.TAB_UPDATE_INTERVAL, Constants.TAB_UPDATE_INTERVAL);
            } catch (Exception e) {
                // Fallback to Bukkit scheduler
                return startBukkitTabUpdateTask(manager, plugin);
            }
        } else {
            // Use Bukkit scheduler
            return startBukkitTabUpdateTask(manager, plugin);
        }
    }
    
    private static Object startBukkitTabUpdateTask(TabListManager manager, org.bukkit.plugin.Plugin plugin) {
        return new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                manager.updateDirtyTabLists();
            }
        }.runTaskTimer(plugin, Constants.TAB_UPDATE_INTERVAL, Constants.TAB_UPDATE_INTERVAL);
    }
}

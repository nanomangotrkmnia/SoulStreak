package dev.nanomango.soulstreak;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import dev.nanomango.soulstreak.commands.CommandHandler;
import dev.nanomango.soulstreak.commands.HelpCommand;
import dev.nanomango.soulstreak.commands.WithdrawCommand;
import dev.nanomango.soulstreak.config.ConfigManager;
import dev.nanomango.soulstreak.managers.TabListManager;
import dev.nanomango.soulstreak.tiers.TierManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main plugin class for SoulStreak
 * Optimized version with improved performance, maintainability, and robustness
 */
public class SoulStreak extends JavaPlugin implements Listener, TabCompleter {

    // Core components
    private NamespacedKey streakKey;
    private NamespacedKey heartsKey;
    private ConfigManager configManager;
    private TierManager tierManager;
    private TabListManager tabListManager;
    private Object tabUpdateTask;
    
    // Command handlers
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    @Override
    public void onEnable() {
        // Initialize keys
        this.streakKey = new NamespacedKey(this, "player_streak");
        this.heartsKey = new NamespacedKey(this, "player_hearts");
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.tierManager = new TierManager();
        
        // Check if running on Folia
        boolean isFolia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalScheduler");
            getLogger().info("Folia detected - Using Folia scheduler!");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            getLogger().info("Bukkit detected - Using Bukkit scheduler!");
        }
        
        // Save default configuration
        saveDefaultConfig();
        
        // Register events and commands
        Bukkit.getPluginManager().registerEvents(this, this);
        registerCommands();
        this.getCommand(Constants.MAIN_COMMAND).setExecutor(this);
        this.getCommand(Constants.MAIN_COMMAND).setTabCompleter(this);
        
        // Start tab update task
        this.tabListManager = new TabListManager(tierManager, null);
        this.tabUpdateTask = TabListManager.startTabUpdateTask(tabListManager, isFolia, this);
        
        // Add heart crafting recipe
        addHeartRecipe();
        
        getLogger().info("SoulStreak v1.0.0 - Optimized Version with Persistent Hearts - Enabled!");
    }

    /**
     * Registers all command handlers
     */
    private void registerCommands() {
        commandHandlers.put(Constants.HELP_SUBCOMMAND, new HelpCommand());
        commandHandlers.put(Constants.WITHDRAW_SUBCOMMAND, new WithdrawCommand(this));
        
        // Admin commands could be added here
        // commandHandlers.put(Constants.SETSTREAK_SUBCOMMAND, new SetStreakCommand(this));
        // commandHandlers.put(Constants.ADDHEARTS_SUBCOMMAND, new AddHeartsCommand(this));
        // etc.
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadHeartData(player);
        
        int streak = getStreak(player);
        tierManager.applyTierEffects(player, streak);
        tabListManager.markDirty(player);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        tierManager.clearPlayerData(player);
        tabListManager.onPlayerQuit(player);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (victim == null) return;

        int victimStreak = getStreak(victim);
        boolean wasStreakEnded = false;
        
        // Handle death mechanics
        if (killer != null && !killer.equals(victim)) {
            // PvP death - lose streak
            int newVictimStreak = Math.max(victimStreak - configManager.getStreakLossOnDeath(), 0);
            setStreak(victim, newVictimStreak);
            
            // Heart loss mechanics
            handleHeartLoss(victim);
            
            if (victimStreak > 0 && newVictimStreak == 0) {
                wasStreakEnded = true;
                String deathMessage = formatStreakEndMessage(victim, killer);
                event.setDeathMessage(deathMessage);
            } else if (newVictimStreak < victimStreak) {
                victim.sendMessage(Constants.ERROR_PREFIX + "You lost " + configManager.getStreakLossOnDeath() + " streak! Current streak: " + newVictimStreak);
            }
            
            // Killer gains streak
            int currentStreak = getStreak(killer);
            if (currentStreak < Constants.MAX_STREAK) {
                setStreak(killer, currentStreak + 1);
                tierManager.applyTierEffects(killer, currentStreak + 1);
                killer.sendMessage(Constants.SUCCESS_PREFIX + "Kill streak: " + (currentStreak + 1) + "!");
            }
            
            // Mark both players for tab update
            tabListManager.markDirty(victim);
            tabListManager.markDirty(killer);
            
        } else {
            // Natural death
            if (configManager.isNaturalDeathsAffectNothing()) {
                // Completely silent natural deaths
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("RIGHT_CLICK") || !event.hasItem()) {
            return;
        }
        
        ItemStack item = event.getItem();
        
        if (isHeartItem(item)) {
            event.setCancelled(true);
            handleHeartConsumption(event.getPlayer(), item);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        int streak = getStreak(player);
        String tier = tierManager.getTierName(streak);
        
        String format = tier + " " + player.getName() + ": " + event.getMessage();
        event.setFormat(format);
    }

    /**
     * Handles heart loss on death
     */
    private void handleHeartLoss(Player victim) {
        AttributeInstance maxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) return;
        
        double currentHealth = maxHealth.getBaseValue();
        
        // Only lose hearts if above minimum
        if (currentHealth <= Constants.MIN_HEALTH) return;
        
        double heartsLost = (Math.random() < Constants.ONE_HEART_LOSS_CHANCE) ? 
                           Constants.ONE_HEART : Constants.ONE_HEART * 2;
        
        double newHealth = Math.max(currentHealth - heartsLost, Constants.MIN_HEALTH);
        maxHealth.setBaseValue(newHealth);
        saveHeartData(victim, newHealth);
        
        victim.sendMessage(Constants.ERROR_PREFIX + "You died and lost " + (int)(heartsLost / Constants.ONE_HEART) + " heart(s)!");
        victim.sendMessage(Constants.INFO_PREFIX + "Current hearts: " + (int)(newHealth / Constants.ONE_HEART));
    }

    /**
     * Handles heart consumption
     */
    private void handleHeartConsumption(Player player, ItemStack item) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) return;
        
        if (maxHealth.getBaseValue() >= configManager.getMaxHearts() * Constants.ONE_HEART) {
            player.sendMessage(Constants.ERROR_PREFIX + "You already have maximum hearts!");
            return;
        }
        
        // Remove the heart item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }
        
        double newHealth = Math.min(maxHealth.getBaseValue() + Constants.ONE_HEART, 
                                  configManager.getMaxHearts() * Constants.ONE_HEART);
        maxHealth.setBaseValue(newHealth);
        saveHeartData(player, newHealth);
        
        player.sendMessage(Constants.SUCCESS_PREFIX + "Heart consumed! +1 heart restored.");
        player.sendMessage(Constants.INFO_PREFIX + "Current hearts: " + (int)(newHealth / Constants.ONE_HEART) + "/" + configManager.getMaxHearts());
    }

    /**
     * Formats streak end message using StringBuilder for better performance
     */
    private String formatStreakEndMessage(Player victim, Player killer) {
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        String weaponName = getWeaponName(weapon);
        
        StringBuilder message = new StringBuilder();
        message.append(Constants.ERROR_PREFIX)
               .append(victim.getName())
               .append("'s kill streak was ended by ")
               .append(ChatColor.YELLOW)
               .append(killer.getName())
               .append(Constants.ERROR_PREFIX)
               .append(" using ")
               .append(ChatColor.AQUA)
               .append(weaponName)
               .append(Constants.ERROR_PREFIX)
               .append("!");
        
        return message.toString();
    }

    /**
     * Gets formatted weapon name
     */
    private String getWeaponName(ItemStack weapon) {
        if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
            return weapon.getItemMeta().getDisplayName();
        }
        
        String weaponName = weapon.getType().toString().toLowerCase().replace("_", " ");
        String[] words = weaponName.split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        
        return formatted.toString().trim();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase(Constants.MAIN_COMMAND)) {
            return false;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constants.ERROR_PREFIX + Constants.PLAYERS_ONLY);
            return true;
        }
        
        // Show current streak if no arguments
        if (args.length == 0) {
            Player player = (Player) sender;
            int streak = getStreak(player);
            String tier = tierManager.getTierName(streak);
            player.sendMessage(ChatColor.GOLD + "Your current streak: " + ChatColor.WHITE + streak + " " + tier);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        CommandHandler handler = commandHandlers.get(subCommand);
        
        if (handler != null) {
            return handler.handle(sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage(Constants.ERROR_PREFIX + Constants.UNKNOWN_SUBCOMMAND);
            return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList(Constants.HELP_SUBCOMMAND, Constants.WITHDRAW_SUBCOMMAND));
            
            if (sender.hasPermission("soulstreak.admin")) {
                subCommands.addAll(Arrays.asList(Constants.GIVE_SUBCOMMAND, Constants.SETSTREAK_SUBCOMMAND, 
                                           Constants.ADDHEARTS_SUBCOMMAND, Constants.RESET_SUBCOMMAND, 
                                           Constants.TOGGLE_SUBCOMMAND, Constants.CONFIG_SUBCOMMAND, 
                                           Constants.RELOAD_SUBCOMMAND));
            }
            
            String current = args[0].toLowerCase();
            completions = subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(current))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (Arrays.asList(Constants.RESET_SUBCOMMAND, Constants.GIVE_SUBCOMMAND, 
                           Constants.SETSTREAK_SUBCOMMAND, Constants.ADDHEARTS_SUBCOMMAND).contains(subCommand) 
                && sender.hasPermission("soulstreak.admin")) {
                
                // Tab complete player names
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(onlinePlayer.getName());
                    }
                }
            } else if (Constants.TOGGLE_SUBCOMMAND.equals(subCommand) && sender.hasPermission("soulstreak.admin")) {
                List<String> toggleOptions = Arrays.asList(Constants.TOGGLE_NATURAL_DEATHS, Constants.TOGGLE_HEART_LOSS);
                String current = args[1].toLowerCase();
                completions = toggleOptions.stream()
                    .filter(option -> option.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
            } else if (Constants.CONFIG_SUBCOMMAND.equals(subCommand) && sender.hasPermission("soulstreak.admin")) {
                List<String> configKeys = Arrays.asList(Constants.CONFIG_STREAK_LOSS, Constants.CONFIG_LOSE_HEART, 
                                                     Constants.CONFIG_MAX_HEARTS, Constants.CONFIG_NATURAL_DEATHS);
                String current = args[1].toLowerCase();
                completions = configKeys.stream()
                    .filter(key -> key.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            if (Constants.RESET_SUBCOMMAND.equals(subCommand) && sender.hasPermission("soulstreak.admin")) {
                List<String> resetTypes = Arrays.asList(Constants.RESET_EFFECTS, Constants.RESET_STREAK, 
                                                     Constants.RESET_HEARTS, Constants.RESET_ALL);
                String current = args[2].toLowerCase();
                completions = resetTypes.stream()
                    .filter(type -> type.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
            } else if (Constants.GIVE_SUBCOMMAND.equals(subCommand) && sender.hasPermission("soulstreak.admin")) {
                List<String> itemTypes = Arrays.asList("heart");
                String current = args[2].toLowerCase();
                completions = itemTypes.stream()
                    .filter(type -> type.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }

    // Utility methods
    private int getStreak(Player p) {
        if (p == null) return 0;
        PersistentDataContainer container = p.getPersistentDataContainer();
        return container.getOrDefault(streakKey, PersistentDataType.INTEGER, 0);
    }

    private void setStreak(Player p, int amount) {
        if (p == null) return;
        PersistentDataContainer container = p.getPersistentDataContainer();
        container.set(streakKey, PersistentDataType.INTEGER, amount);
    }

    public void saveHeartData(Player player, double health) {
        if (player == null) return;
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(heartsKey, PersistentDataType.DOUBLE, health);
    }

    private void loadHeartData(Player player) {
        if (player == null) return;
        
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) return;
        
        PersistentDataContainer container = player.getPersistentDataContainer();
        double savedHealth = container.getOrDefault(heartsKey, PersistentDataType.DOUBLE, Constants.DEFAULT_HEALTH);
        
        // Ensure saved health is within valid bounds
        savedHealth = Math.max(Constants.MIN_HEALTH, 
                             Math.min(savedHealth, configManager.getMaxHearts() * Constants.ONE_HEART));
        maxHealth.setBaseValue(savedHealth);
    }

    private boolean isHeartItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && 
               meta.getDisplayName().equals(Constants.HEART_NAME);
    }

    private void addHeartRecipe() {
        NamespacedKey heartKey = new NamespacedKey(this, "heart_recipe");
        ItemStack result = createHeartItem();
        
        ShapedRecipe recipe = new ShapedRecipe(heartKey, result);
        recipe.shape(Constants.HEART_RECIPE_PATTERN.split(" "));
        recipe.setIngredient('d', Constants.CRAFTING_DIAMOND);
        recipe.setIngredient('r', Constants.CRAFTING_REDSTONE);
        
        Bukkit.addRecipe(recipe);
        getLogger().info("Heart crafting recipe added!");
    }

    private ItemStack createHeartItem() {
        ItemStack heart = new ItemStack(Constants.HEART_MATERIAL);
        ItemMeta meta = heart.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(Constants.HEART_NAME);
            List<String> lore = Arrays.asList(Constants.HEART_LORE_1, Constants.HEART_LORE_2, Constants.HEART_LORE_3);
            meta.setLore(lore);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            heart.setItemMeta(meta);
        }
        
        return heart;
    }

    // Getters for command handlers
    public ConfigManager getConfigManager() { return configManager; }
    public TierManager getTierManager() { return tierManager; }
    public TabListManager getTabListManager() { return tabListManager; }
    public NamespacedKey getStreakKey() { return streakKey; }
    public NamespacedKey getHeartsKey() { return heartsKey; }

    @Override
    public void onDisable() {
        // Clean up tasks
        if (tabUpdateTask != null) {
            try {
                if (tabUpdateTask instanceof org.bukkit.scheduler.BukkitTask) {
                    ((org.bukkit.scheduler.BukkitTask) tabUpdateTask).cancel();
                } else {
                    Class<?> taskClass = tabUpdateTask.getClass();
                    java.lang.reflect.Method cancel = taskClass.getMethod("cancel");
                    cancel.invoke(tabUpdateTask);
                }
            } catch (Exception e) {
                getLogger().warning("Failed to cancel tab update task: " + e.getMessage());
            }
        }
        
        getLogger().info("SoulStreak v1.0.0 - Disabled!");
    }
}

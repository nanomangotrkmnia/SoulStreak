package dev.nanomango.soulstreak.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import dev.nanomango.soulstreak.SoulStreak;
import dev.nanomango.soulstreak.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the withdraw command for SoulStreak
 */
public class WithdrawCommand implements CommandHandler {
    
    private final SoulStreak plugin;
    
    public WithdrawCommand(SoulStreak plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constants.ERROR_PREFIX + Constants.PLAYERS_ONLY);
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("soulstreak.withdraw")) {
            player.sendMessage(Constants.ERROR_PREFIX + Constants.NO_PERMISSION);
            return true;
        }
        
        withdrawHearts(player);
        return true;
    }
    
    /**
     * Withdraws bonus hearts from a player as items
     */
    private void withdrawHearts(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            player.sendMessage(Constants.ERROR_PREFIX + "Unable to access health attributes.");
            return;
        }
        
        double currentHealth = maxHealth.getBaseValue();
        if (currentHealth <= Constants.DEFAULT_HEALTH) {
            player.sendMessage(Constants.ERROR_PREFIX + "You don't have any bonus hearts to withdraw.");
            return;
        }
        
        double heartsToWithdraw = Math.min(currentHealth - Constants.DEFAULT_HEALTH, 10.0); // Max 10 hearts
        int heartsAmount = (int) (heartsToWithdraw / Constants.ONE_HEART);
        
        // Create heart items
        ItemStack heartItem = createBonusHeartItem(heartsAmount);
        
        // Give item and update health
        player.getInventory().addItem(heartItem);
        double newHealth = currentHealth - heartsToWithdraw;
        maxHealth.setBaseValue(newHealth);
        
        // Save heart data
        plugin.saveHeartData(player, newHealth);
        
        player.sendMessage(Constants.SUCCESS_PREFIX + "Withdrew " + heartsAmount + " bonus hearts!");
        player.sendMessage(Constants.INFO_PREFIX + "Right-click the heart items to restore health.");
    }
    
    /**
     * Creates a bonus heart item stack
     */
    private ItemStack createBonusHeartItem(int amount) {
        ItemStack heartItem = new ItemStack(Constants.HEART_MATERIAL, amount);
        ItemMeta meta = heartItem.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(Constants.BONUS_HEART_NAME);
            List<String> lore = new ArrayList<>();
            lore.add(Constants.BONUS_HEART_LORE_1);
            lore.add(Constants.BONUS_HEART_LORE_2);
            meta.setLore(lore);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            heartItem.setItemMeta(meta);
        }
        
        return heartItem;
    }
}

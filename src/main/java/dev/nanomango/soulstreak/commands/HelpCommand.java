package dev.nanomango.soulstreak.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.nanomango.soulstreak.Constants;

/**
 * Handles the help command for SoulStreak
 */
public class HelpCommand implements CommandHandler {
    
    @Override
    public boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constants.ERROR_PREFIX + Constants.PLAYERS_ONLY);
            return true;
        }
        
        Player player = (Player) sender;
        sendHelpMessage(player);
        return true;
    }
    
    /**
     * Sends the help message to a player
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== SoulStreak Base Version with Hearts ===");
        player.sendMessage(ChatColor.YELLOW + "/streak" + ChatColor.WHITE + " - Show your current streak");
        player.sendMessage(ChatColor.YELLOW + "/streak help" + ChatColor.WHITE + " - Show this help message");
        
        if (player.hasPermission("soulstreak.withdraw")) {
            player.sendMessage(ChatColor.YELLOW + "/streak withdraw" + ChatColor.WHITE + " - Withdraw bonus hearts as items");
        }
        
        if (player.hasPermission("soulstreak.admin")) {
            player.sendMessage(ChatColor.RED + "=== Admin Commands ===");
            player.sendMessage(ChatColor.YELLOW + "/streak setstreak <player> <amount>" + ChatColor.WHITE + " - Set player streak");
            player.sendMessage(ChatColor.YELLOW + "/streak addhearts <player> <amount>" + ChatColor.WHITE + " - Add hearts to player");
            player.sendMessage(ChatColor.YELLOW + "/streak reset <player> [type]" + ChatColor.WHITE + " - Reset player data (effects/streak/hearts/all)");
            player.sendMessage(ChatColor.YELLOW + "/streak toggle <setting>" + ChatColor.WHITE + " - Toggle settings (naturaldeaths/heartloss)");
            player.sendMessage(ChatColor.YELLOW + "/streak config <key> <value>" + ChatColor.WHITE + " - Configure settings");
            player.sendMessage(ChatColor.YELLOW + "/streak reload" + ChatColor.WHITE + " - Reload configuration");
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Streak Tiers ===");
        player.sendMessage(ChatColor.GRAY + "0-2 kills: No bonus");
        player.sendMessage(ChatColor.GREEN + "3 kills: Tier 1 - Entry milestone");
        player.sendMessage(ChatColor.AQUA + "5-9 kills: Tier 2 - Fire Resistance & +2 Hearts");
        player.sendMessage(ChatColor.DARK_RED + "10-14 kills: Tier 3 - Fire Resistance, Strength & +3 Hearts");
        player.sendMessage(ChatColor.GOLD + "15 kills: Tier 4 - Fire Resistance, Strength 1, Speed 1 & +5 Hearts");
        player.sendMessage(ChatColor.RED + "Death Penalty: Lose 1 heart (35%) or 2 hearts (65%)");
        player.sendMessage(ChatColor.YELLOW + "Hearts: Craftable (Diamond+Redstone) and withdrawable!");
    }
}

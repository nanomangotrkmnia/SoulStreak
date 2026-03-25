package dev.nanomango.soulstreak.commands;

import org.bukkit.command.CommandSender;

/**
 * Interface for handling subcommands in the SoulStreak plugin
 */
@FunctionalInterface
public interface CommandHandler {
    
    /**
     * Handles a subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments (excluding the subcommand)
     * @return true if the command was handled successfully, false otherwise
     */
    boolean handle(CommandSender sender, String[] args);
}

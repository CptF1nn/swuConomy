package com.swucraft.swuConomy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {
    DataHandler dHandler;

    public BalanceCommand(DataHandler dHandler) {
        this.dHandler = dHandler;
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        if (!player.hasPermission("swuConomy.getBalance")) {
            String message = "You are not allowed to use this command.";
            player.sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
            return false;
        }

        player.sendMessage("You have " + dHandler.Balance(player) + " " + SwUtility.currencyName);

        return true;
    }
}

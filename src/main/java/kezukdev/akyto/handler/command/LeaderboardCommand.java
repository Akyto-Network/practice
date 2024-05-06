package kezukdev.akyto.handler.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;

public class LeaderboardCommand implements CommandExecutor {
	
	private final Practice main;
	
	public LeaderboardCommand(final Practice main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(org.bukkit.ChatColor.RED + "You must be a player to do that");
			return false;
		}

		final Player playerSender = (Player) sender;
		playerSender.openInventory(this.main.getManagerHandler().getInventoryManager().getLeaderboardInventory());

		return false;
	}
}

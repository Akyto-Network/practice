package kezukdev.akyto.handler.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import akyto.core.profile.ProfileState;
import kezukdev.akyto.Practice;
import kezukdev.akyto.utils.Utils;

public class LeaderboardCommand implements CommandExecutor {
	
	private final Practice main;
	
	public LeaderboardCommand(final Practice main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to do that");
			return false;
		}
		final Player playerSender = (Player) sender;
		if (Utils.getProfiles(playerSender.getUniqueId()).isInState(ProfileState.FIGHT)) {
			sender.sendMessage(ChatColor.RED + "You cannot do that right now!");
			return false;
		}
		playerSender.openInventory(this.main.getManagerHandler().getInventoryManager().getLeaderboardInventory());

		return false;
	}
}

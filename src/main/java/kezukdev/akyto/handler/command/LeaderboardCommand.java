package kezukdev.akyto.handler.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import net.md_5.bungee.api.ChatColor;

public class LeaderboardCommand implements CommandExecutor {
	
	private final Practice main;
	
	public LeaderboardCommand(final Practice main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		final Profile profile = this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId());
		if (profile.getProfileState().equals(ProfileState.FREE) || profile.getProfileState().equals(ProfileState.QUEUE) || profile.getProfileState().equals(ProfileState.SPECTATE)) {
			Bukkit.getPlayer(sender.getName()).openInventory(this.main.getManagerHandler().getInventoryManager().getLeaderboardInventory());
		}
		else {
			sender.sendMessage(ChatColor.RED + "You cannot do that right now!");
		}
		return false;
	}

}

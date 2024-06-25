package kezukdev.akyto.handler.command;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.utils.Utils;

public class StatisticsCommand implements CommandExecutor {

	private final Practice main;
	
	public StatisticsCommand(final Practice main) { this.main = main; }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(org.bukkit.ChatColor.RED + "You must be a player to do that");
			return false;
		}

		final Player playerSender = (Player) sender;
		final Profile profile = Utils.getProfiles(playerSender.getUniqueId());
		boolean is_busy = profile.isInState(ProfileState.FIGHT, ProfileState.EDITOR);

		if (args.length == 0) {
			if (is_busy) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now");
				return false;
			}

			playerSender.openInventory(Core.API.getManagerHandler().getInventoryManager().getProfileInventory().get(playerSender.getUniqueId()));
			return false;
		}

		if (args.length == 1) {
            if (is_busy) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now");
				return false;
			}

			final Player target = Bukkit.getPlayer(args[0]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
				return false;
			}

			playerSender.openInventory(Core.API.getManagerHandler().getInventoryManager().getProfileInventory().get(target.getUniqueId()));
			return false;
		}

		sender.sendMessage(ChatColor.RED + "/" + cmd.getName());
		return false;
	}
}

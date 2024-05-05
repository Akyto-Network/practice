package kezukdev.akyto.handler.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.profile.ProfileState;

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
		final ProfileState state = this.main.getUtils().getProfiles(playerSender.getUniqueId()).getProfileState();
		boolean is_busy = state.equals(ProfileState.FIGHT) || state.equals(ProfileState.EDITOR);

		if (args.length == 0) {
			if (is_busy) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now");
				return false;
			}

			playerSender.openInventory(this.main.getManagerHandler().getInventoryManager().getProfileInventory().get(playerSender.getUniqueId()));
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

			playerSender.openInventory(this.main.getManagerHandler().getInventoryManager().getProfileInventory().get(target.getUniqueId()));
			return false;
		}

		sender.sendMessage(ChatColor.RED + "/" + cmd.getName());
		return false;
	}
}

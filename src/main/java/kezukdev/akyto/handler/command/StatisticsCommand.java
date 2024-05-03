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
		if (!(sender instanceof Player)) return false;
		final ProfileState state = this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState();
		boolean is_busy = state.equals(ProfileState.FIGHT) || state.equals(ProfileState.EDITOR);
		if (args.length == 0) {
			if (is_busy) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now");
				return false;
			}
			Bukkit.getPlayer(sender.getName()).openInventory(this.main.getManagerHandler().getInventoryManager().getProfileInventory().get(Bukkit.getPlayer(sender.getName()).getUniqueId()));
			return false;
		}
		if (args.length == 1) {
            if (is_busy) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now");
				return false;
			}
			if (Bukkit.getPlayer(args[0]) == null) {
				sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
				return false;
			}
			Bukkit.getPlayer(sender.getName()).openInventory(this.main.getManagerHandler().getInventoryManager().getProfileInventory().get(Bukkit.getPlayer(args[0]).getUniqueId()));
			return false;
		}
		sender.sendMessage(ChatColor.RED + "/" + cmd.getName());
		return false;
	}

}

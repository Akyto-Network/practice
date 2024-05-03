package kezukdev.akyto.handler.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;

public class DuelCommand implements CommandExecutor {
	
	private final Practice main;
	
	public DuelCommand(final Practice main) { this.main = main; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "/" + cmd.getName() + " <player>");
			return false;
		}
		if (args.length > 2) {
			sender.sendMessage(ChatColor.RED + "/" + cmd.getName() + " accept <player>");
			return false;
		}
		if (args.length == 1) {
			final Profile profile = this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId());
			if (args[0].equals(sender.getName())) {
				sender.sendMessage(ChatColor.RED + "You cannot duel yourself.");
				return false;
			}
			if (!profile.getSettings().get(1)) {
				sender.sendMessage(ChatColor.RED + "You have disable your duel request in your settings!");
				return false;
			}
			if (!profile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now.");
				return false;
			}
			if (Bukkit.getPlayer(args[0]) == null) {
				sender.sendMessage(ChatColor.RED + args[0] + " not found on akyto.");
				return false;
			}
			final Profile targetProfile = this.main.getUtils().getProfiles(Bukkit.getPlayer(args[0]).getUniqueId());
			if (!targetProfile.getSettings().get(1)) {
				sender.sendMessage(ChatColor.RED + "This player doesn't accept any duel request!");
				return false;
			}
			if (!targetProfile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + args[0] + " is not free now.");
				return false;
			}
			this.main.getManagerHandler().getRequestManager().createPullRequest(Bukkit.getPlayer(sender.getName()).getUniqueId(), Bukkit.getPlayer(args[0]).getUniqueId());	
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
			final Profile profile = this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId());
			if (!profile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now.");
				return false;
			}
			if (Bukkit.getPlayer(args[1]) == null) {
				sender.sendMessage(ChatColor.RED + args[0] + " not found on akyto.");
				return false;
			}
			if (!this.main.getManagerHandler().getRequestManager().getRequest().containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "This player don't have send any request!");
				return false;
			}
			if (!this.main.getManagerHandler().getRequestManager().getRequest().get(Bukkit.getPlayer(args[1]).getUniqueId()).getRequested().equals(Bukkit.getPlayer(sender.getName()).getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "You don't have any request from this player!");
				return false;
			}
			final Profile targetProfile = this.main.getUtils().getProfiles(Bukkit.getPlayer(args[1]).getUniqueId());
			if (!targetProfile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + args[0] + " is not free now.");
				return false;
			}
			new Duel(this.main, Sets.newHashSet(Bukkit.getPlayer(args[1]).getUniqueId()), Sets.newHashSet(Bukkit.getPlayer(sender.getName()).getUniqueId()), false, this.main.getManagerHandler().getRequestManager().getRequest().get(Bukkit.getPlayer(args[1]).getUniqueId()).getKit());
			this.main.getManagerHandler().getRequestManager().removeRequest(Bukkit.getPlayer(args[1]).getUniqueId());
		}
		return false;
	}

}

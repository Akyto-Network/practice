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
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import kezukdev.akyto.request.Request;
import kezukdev.akyto.utils.Utils;

public class DuelCommand implements CommandExecutor {
	
	private final Practice main;
	
	public DuelCommand(final Practice main) { this.main = main; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to do that");
			return false;
		}

		final Player playerSender = (Player) sender;
		final Profile senderProfile = Utils.getProfiles(playerSender.getUniqueId());

		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "/" + cmd.getName() + " <player>");
			return false;
		}

		if (args.length > 2) {
			sender.sendMessage(ChatColor.RED + "/" + cmd.getName() + " accept <player>");
			return false;
		}

		if (args.length == 1) {
			Player target = Bukkit.getPlayer(args[0]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + args[0] + " not found on akyto.");
				return false;
			}

			if (target.getUniqueId().equals(playerSender.getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "You cannot duel yourself.");
				return false;
			}

			if (!senderProfile.getSettings().get(1)) {
				sender.sendMessage(ChatColor.RED + "You have disabled your duel request in your settings!");
				return false;
			}

			if (!senderProfile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now.");
				return false;
			}

			final Profile targetProfile = Utils.getProfiles(target.getUniqueId());

			if (!targetProfile.getSettings().get(1)) {
				sender.sendMessage(ChatColor.RED + "This player doesn't accept any duel request!");
				return false;
			}

			if (!targetProfile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + target.getDisplayName() + " is not free now.");
				return false;
			}

			this.main.getManagerHandler().getRequestManager().createPullRequest(playerSender.getUniqueId(), target.getUniqueId());
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
			if (!senderProfile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now.");
				return false;
			}

			final Player target = Bukkit.getPlayer(args[1]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + args[1] + " not found on akyto.");
				return false;
			}
			
			final Request request = Utils.getRequestByUUID(target.getUniqueId());
			if (request == null || !request.getReceiver().equals(Utils.getUUID(sender.getName()))) {
				sender.sendMessage(ChatColor.RED + "You have no request for a duel from " + target.getName());
				return false;
			}
			final Profile targetProfile = Utils.getProfiles(target.getUniqueId());
			if (!targetProfile.getProfileState().equals(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + target.getDisplayName() + " is not free now.");
				return false;
			}

			new Duel(this.main, Sets.newHashSet(target.getUniqueId()), Sets.newHashSet(playerSender.getUniqueId()), false, request.getKit(), DuelType.SINGLE, request.getArena());
			this.main.getManagerHandler().getRequestManager().removeRequest(request);
		}
		return false;
	}
}

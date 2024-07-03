package kezukdev.akyto.handler.command;

import akyto.core.Core;
import kezukdev.akyto.handler.manager.RequestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
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
			if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsValue(args[0])) {
				sender.sendMessage(ChatColor.RED + args[0] + " not found on akyto.");
				return false;
			}
			Player target = Bukkit.getPlayer(args[0]);

			if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsKey(args[0])) {
				target = Bukkit.getPlayer(Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().get(args[0]));
			}
			if (target == null) {
				sender.sendMessage(ChatColor.RED + args[0] + " not found on akyto.");
				return false;
			}

			if (target.getUniqueId().equals(playerSender.getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "You cannot duel yourself.");
				return false;
			}

			if (senderProfile.getSettings()[1] != 0) {
				sender.sendMessage(ChatColor.RED + "You have disabled your duel request in your settings!");
				return false;
			}

			if (!senderProfile.isInState(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now.");
				return false;
			}
			final RequestManager requestManager = Practice.getAPI().getManagerHandler().getRequestManager();
			if (requestManager.getRequest().containsKey(CoreUtils.getUUID(sender.getName()))) {
				sender.sendMessage(ChatColor.RED + "There's a pending request in all of this :3");
				return false;
			}

			final Profile targetProfile = Utils.getProfiles(target.getUniqueId());

			if (!targetProfile.isInState(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + target.getName() + " is not available at the moment");
				return false;
			}
			if (targetProfile.getSettings()[1] != 0) {
				sender.sendMessage(ChatColor.RED + "This player doesn't accept any duel request!");
				return false;
			}
			requestManager.createPullRequest(playerSender.getUniqueId(), target.getUniqueId());
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
			if (!senderProfile.isInState(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now.");
				return false;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsKey(args[1])) {
				target = Bukkit.getPlayer(Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().get(args[1]));
			}

			if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsValue(args[1])) {
				sender.sendMessage(ChatColor.RED + args[1] + " not found on akyto.");
				return false;
			}

			if (target == null) {
				sender.sendMessage(ChatColor.RED + args[1] + " not found on akyto.");
				return false;
			}
			
			Request request = Utils.getRequestByUUID(target.getUniqueId());
			if (request == null || !request.getReceiver().equals(CoreUtils.getUUID(sender.getName())) || request.getArena() == null || request.getKit() == null) {
				sender.sendMessage(ChatColor.RED + "You have no request for a duel from " + target.getName());
				return false;
			}
			final Profile targetProfile = Utils.getProfiles(target.getUniqueId());
			if (!targetProfile.isInState(ProfileState.FREE)) {
				sender.sendMessage(ChatColor.RED + args[1] + " is not free now.");
				return false;
			}
			new Duel(this.main, Sets.newHashSet(target.getUniqueId()), Sets.newHashSet(playerSender.getUniqueId()), false, request.getKit(), DuelType.SINGLE, request.getArena());
			this.main.getManagerHandler().getRequestManager().getRequest().remove(target.getUniqueId());
		}
		return false;
	}
}

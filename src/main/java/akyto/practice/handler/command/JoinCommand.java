package akyto.practice.handler.command;

import akyto.core.Core;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.handler.manager.TournamentManager;
import akyto.practice.tournament.TournamentState;
import akyto.practice.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

import java.util.UUID;

public class JoinCommand implements CommandExecutor {
	
	private final Practice main;
	
	public JoinCommand(final Practice potted) { this.main = potted; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		final Player player = Bukkit.getPlayer(sender.getName());
		if (!Core.API.getManagerHandler().getProfileManager().getProfiles().get(player.getUniqueId()).getProfileState().equals(ProfileState.FREE)) {
			sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
			return false;
		}
		if (Utils.getTournamentByPlayerUUID(player.getUniqueId()) != null) {
			sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
			return false;
		}
		if (args.length == 0) {
			this.main.getManagerHandler().getEventManager().joinEvent(player.getUniqueId());
			return false;
		}
		if (args.length == 1) {
			TournamentManager tournamentManager = this.main.getManagerHandler().getTournamentManager();
			final UUID id = UUID.fromString(args[0]);
			if (tournamentManager.getTournamentEntry(id) == null) {
				sender.sendMessage(ChatColor.RED + "No tournament available with this ID.");
				return false;
			}
			if (tournamentManager.getTournamentEntry(id).getTournamentState().equals(TournamentState.STARTED)) {
				sender.sendMessage(ChatColor.RED + "Tournament already started.");
				return false;
			}
			tournamentManager.joinTournament(Bukkit.getPlayer(sender.getName()), UUID.fromString(args[0]));
		}
		else {
			sender.sendMessage(ChatColor.RED + "/" + cmd.getName() + ChatColor.GRAY + " or " + ChatColor.RED + "/" + cmd.getName() + " <tournamentId>");
			return false;
		}
		return false;
	}

}

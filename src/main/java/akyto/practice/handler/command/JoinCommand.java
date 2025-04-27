package akyto.practice.handler.command;

import akyto.core.Core;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class JoinCommand implements CommandExecutor {
	
	private final Practice main;
	
	public JoinCommand(final Practice potted) { this.main = potted; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		if (args.length != 0) {
			sender.sendMessage(ChatColor.RED + "/" + cmd.getName());
			return false;
		}
		if (!Core.API.getManagerHandler().getProfileManager().getProfiles().get(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.FREE)) {
			sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
			return false;
		}
		this.main.getManagerHandler().getEventManager().joinEvent(Bukkit.getPlayer(sender.getName()).getUniqueId());
		return false;
	}

}

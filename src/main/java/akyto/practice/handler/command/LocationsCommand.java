package akyto.practice.handler.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import akyto.practice.utils.location.LocationUtil;
import net.md_5.bungee.api.ChatColor;

public class LocationsCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(org.bukkit.ChatColor.RED + "You must be a player to do that");
			return false;
		}

		if (!sender.hasPermission("akyto.arena")) {
			sender.sendMessage(ChatColor.RED + "The permission akyto.arena is needed for this.");
			return false;
		}

		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "/location set <spawn/editor/showcase/sumoEvent/sumoFirst/sumoSecond>");
			return false;
		}

		if (args[1].contains("spawn") ||
				args[1].contains("editor") ||
				args[1].contains("showcase") ||
				args[1].contains("sumoEvent") ||
				args[1].contains("sumoFirst") ||
				args[1].contains("sumoSecond")) {
			LocationUtil.getLocationHelper(args[1]).setLocation(Bukkit.getPlayer(sender.getName()).getLocation());
			sender.sendMessage(ChatColor.GREEN + "The location " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " has been set!");
		}
		else {
			sender.sendMessage(ChatColor.RED + "/location set <spawn/editor/showcase/sumoEvent/sumoFirst/sumoSecond>");
			return false;
		}
		return false;
	}
}

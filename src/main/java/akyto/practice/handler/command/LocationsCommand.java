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

		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "/location set<spawn/editor>");
			return false;
		}

		if (args[0].contains("spawn") || args[0].contains("editor")) {
			LocationUtil.getLocationHelper(args[0].contains("spawn") ? "spawn" : "editor").setLocation(Bukkit.getPlayer(sender.getName()).getLocation());
			sender.sendMessage(ChatColor.GREEN + "The location " + ChatColor.WHITE + (args[0].contains("spawn") ? "spawn" : "editor") + ChatColor.GREEN + " has been set!");
		}

		return false;
	}
}

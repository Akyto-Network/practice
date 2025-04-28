package akyto.practice.handler.command;

import akyto.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import akyto.practice.utils.location.LocationUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationsCommand implements CommandExecutor {

	private final Pattern oitcPattern = Pattern.compile("^oitc(\\d+)$");
	private final Practice main;

	public LocationsCommand(final Practice main) {
		this.main = main;
	}

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
			sender.sendMessage(ChatColor.RED + "/location set <spawn/editor/showcase/sumoEvent/sumoFirst/sumoSecond/oitcEvent/oitc1...>");
			return false;
		}

		String locationType = args[1];

		if (locationType.equals("spawn") ||
				locationType.equals("editor") ||
				locationType.equals("showcase") ||
				locationType.equals("sumoEvent") ||
				locationType.equals("sumoFirst") ||
				locationType.equals("sumoSecond") ||
				locationType.equals("oitcEvent")) {
			setLocation(sender, locationType);
			return true;
		}
		if (locationType.startsWith("oitc")) {
			Matcher matcher = oitcPattern.matcher(locationType);
			if (matcher.matches()) {
				int locationNumber = Integer.parseInt(matcher.group(1));
				setOitcLocation(sender, locationNumber);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Argument need to be 'oitc<number>'.");
				return false;
			}
		}
		sender.sendMessage(ChatColor.RED + "/location set <spawn/editor/showcase/sumoEvent/sumoFirst/sumoSecond>");
		return false;
	}

	private void setLocation(CommandSender sender, String locationType) {
		Player player = Bukkit.getPlayer(sender.getName());
		if (player != null) {
			LocationUtil.getLocationHelper(locationType).setLocation(player.getLocation());
			sender.sendMessage(ChatColor.GREEN + "The location " + ChatColor.WHITE + locationType + ChatColor.GREEN + " has been set!");
		} else {
			sender.sendMessage(ChatColor.RED + "You must be player to execute this.");
		}
	}

	private void setOitcLocation(CommandSender sender, int locationNumber) {
		Player player = Bukkit.getPlayer(sender.getName());
		LocationUtil locationUtil = LocationUtil.getLocationHelper("oitc" + locationNumber);
		if (player != null) {
			locationUtil.setLocation(player.getLocation());
			this.main.getOitcLoc().set(locationNumber, locationUtil);
			sender.sendMessage(ChatColor.GREEN + "OITC location " + locationNumber + " has been defined.");
		} else {
			sender.sendMessage(ChatColor.RED + "You must be player to execute this.");
		}
	}
}

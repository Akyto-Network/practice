package kezukdev.akyto.handler.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.arena.ArenaType;
import kezukdev.akyto.utils.location.LocationSerializer;

public class ArenaCommand implements CommandExecutor {
	
	private final Practice main;
	
	public ArenaCommand(final Practice practice) { this.main = practice; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || !sender.isOp()) return false;
		
		if (args.length == 0) {
			sender.sendMessage(new String[] {
					ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
					"",
					ChatColor.DARK_GRAY + "Arena Command Help" + ChatColor.GRAY + ":",
					ChatColor.WHITE + "/arena create <name> <NORMAL/SUMO>",
					ChatColor.WHITE + "/arena setpos<1/2> <name>",
					ChatColor.WHITE + "/arena delete <name>",
					"",
					ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------"
			});
			return false;
		}
		if (args[0].equalsIgnoreCase("create")) {
			if (args.length > 3 || args.length < 3 || ArenaType.valueOf(args[2].toUpperCase()) == null) {
				sender.sendMessage(new String[] {
						ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
						"",
						ChatColor.DARK_GRAY + "Arena Command Help" + ChatColor.GRAY + ":",
						ChatColor.WHITE + "/arena create <name> <NORMAL/SUMO>",
						ChatColor.WHITE + "/arena setpos<1/2> <name>",
						ChatColor.WHITE + "/arena delete <name>",
						"",
						ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------"
				});
				return false;
			}
			if (this.main.getManagerHandler().getArenaManager().getArena(args[1]) != null) {
				sender.sendMessage(ChatColor.RED + "Sorry but this arena already exist.");
				return false;
			}
			if (ArenaType.valueOf(args[2].toUpperCase()) == null) {
				sender.sendMessage(ChatColor.RED + "ArenaType available: NORMAL, SUMO.");
				return false;
			}
			this.main.getArenasMap().putIfAbsent(args[1], new Arena(main, args[1], LocationSerializer.fromBukkitLocation(Bukkit.getPlayer(sender.getName()).getLocation()), LocationSerializer.fromBukkitLocation(Bukkit.getPlayer(sender.getName()).getLocation()), ArenaType.valueOf(args[2].toUpperCase())));
			sender.sendMessage(ChatColor.GREEN + "You have succesfully create the " + args[1] + " arena into the " + args[2] + " type!");
			return false;
		}
		if (args[0].equalsIgnoreCase("setpos1") || args[0].equalsIgnoreCase("setpos2")) {
			if (args.length > 2 || args.length < 2) {
				sender.sendMessage(new String[] {
						ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
						"",
						ChatColor.DARK_GRAY + "Arena Command Help" + ChatColor.GRAY + ":",
						ChatColor.WHITE + "/arena create <name> <NORMAL/SUMO>",
						ChatColor.WHITE + "/arena setpos<1/2> <name>",
						ChatColor.WHITE + "/arena delete <name>",
						"",
						ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------"
				});
				return false;
			}
			if (this.main.getManagerHandler().getArenaManager().getArena(args[1]) == null) {
				sender.sendMessage(ChatColor.RED + "Sorry but this arena doesn't exist!");
				return false;
			}
			this.main.getManagerHandler().getArenaManager().getArena(args[1]).getPosition().set(args[0].contains("1") ? 0 : 1, LocationSerializer.fromBukkitLocation(Bukkit.getPlayer(sender.getName()).getLocation()));
			sender.sendMessage(ChatColor.GREEN + "The " + (args[0].contains("1") ? "first" : "second") + " location is now setup for the " + args[1] + " arena!");
		}
		return false;
	}

}

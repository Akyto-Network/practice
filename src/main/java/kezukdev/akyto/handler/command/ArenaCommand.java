package kezukdev.akyto.handler.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.arena.ArenaType;
import kezukdev.akyto.utils.location.LocationSerializer;

import java.util.jar.JarEntry;

public class ArenaCommand implements CommandExecutor {
	
	private final Practice main;
	
	public ArenaCommand(final Practice practice) { this.main = practice; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || !sender.isOp()) {
			sender.sendMessage(org.bukkit.ChatColor.RED + "You must be an op player to do that");
			return false;
		}

		final Player playerSender = (Player) sender;
		
		if (args.length == 0) {
			sender.sendMessage(new String[] {
					ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
					"",
					ChatColor.DARK_GRAY + "Arena Command Help" + ChatColor.GRAY + ":",
					ChatColor.WHITE + "/arena create <name> <NORMAL/SUMO>",
					ChatColor.WHITE + "/arena setpos<1/2> <name>",
					ChatColor.WHITE + "/arena delete <name>",
					ChatColor.WHITE + "/arena setcorners <name> <corner> <x> <y> <z>",
					"",
					ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------"
			});
			return false;
		}
		if (args[0].equalsIgnoreCase("create")) {
			if (args.length != 3) {
				sender.sendMessage(new String[] {
						ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
						"",
						ChatColor.DARK_GRAY + "Arena Command Help" + ChatColor.GRAY + ":",
						ChatColor.WHITE + "/arena create <name> <NORMAL/SUMO>",
						ChatColor.WHITE + "/arena setpos<1/2> <name>",
						ChatColor.WHITE + "/arena delete <name>",
						ChatColor.WHITE + "/arena setcorners <name> <corner> <x> <y> <z>",
						"",
						ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------"
				});
				return false;
			} else {
                ArenaType.valueOf(args[2].toUpperCase());
            }
            if (this.main.getManagerHandler().getArenaManager().getArena(args[1]) != null) {
				sender.sendMessage(ChatColor.RED + "Sorry but this arena already exist.");
				return false;
			}
            ArenaType.valueOf(args[2].toUpperCase());
            this.main.getArenasMap().putIfAbsent(args[1], new Arena(main, args[1], LocationSerializer.fromBukkitLocation(playerSender.getLocation()), LocationSerializer.fromBukkitLocation(playerSender.getLocation()), ArenaType.valueOf(args[2].toUpperCase()), Material.PAPER));
			sender.sendMessage(ChatColor.GREEN + "You have succesfully create the " + args[1] + " arena into the " + args[2] + " type!");
			return false;
		}

		if (args[0].equalsIgnoreCase("setcorners")) {
			if (args.length != 6) {
				sender.sendMessage(ChatColor.RED + "Usage: /arena setcorners <name> <corner> <x> <y> <z>");
				return false;
			}

			Arena target = this.main.getManagerHandler().getArenaManager().getArena(args[1]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Arena not found");
				return false;
			}

			try {
				double x = Double.parseDouble(args[3]);
				double y = Double.parseDouble(args[4]);
				double z = Double.parseDouble(args[5]);
				if (args[2].equals("1")) {
					target.setCorner1(new LocationSerializer(x, y, z));
				} else if (args[2].equals("2")) {
					target.setCorner2(new LocationSerializer(x, y, z));
				} else {
					sender.sendMessage(ChatColor.RED + "Invalid corner, must be 1 or 2");
					return false;
				}
				sender.sendMessage(ChatColor.GREEN + "Successfully set corner" + args[2] + " for arena " + target.getName());
			} catch (Exception ex) {
				sender.sendMessage(ChatColor.RED + "Failed to parse x y z");
			}

			return false;
		}

		if (args[0].equalsIgnoreCase("setpos1") || args[0].equalsIgnoreCase("setpos2")) {
			if (args.length != 2) {
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
			this.main.getManagerHandler().getArenaManager().getArena(args[1]).getPosition().set(args[0].contains("1") ? 0 : 1, LocationSerializer.fromBukkitLocation(playerSender.getLocation()));
			sender.sendMessage(ChatColor.GREEN + "The " + (args[0].contains("1") ? "first" : "second") + " location is now setup for the " + args[1] + " arena!");
		}
		if (args[0].equalsIgnoreCase("seticon")) {
			if (args.length != 2) {
				sender.sendMessage(new String[] {
						ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
						"",
						ChatColor.DARK_GRAY + "Arena Command Help" + ChatColor.GRAY + ":",
						ChatColor.WHITE + "/arena create <name> <NORMAL/SUMO>",
						ChatColor.WHITE + "/arena setpos<1/2> <name>",
						ChatColor.WHITE + "/arena seticon <arena>",
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
			this.main.getManagerHandler().getArenaManager().getArena(args[1]).setIcon(playerSender.getItemInHand().getType());
			sender.sendMessage(ChatColor.GREEN + "The " + playerSender.getItemInHand().getType().toString() +" has been setted at icon of " + args[1] + " arena!");
		}
		return false;
	}

}

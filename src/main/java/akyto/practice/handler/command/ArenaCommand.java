package akyto.practice.handler.command;

import com.google.common.collect.ImmutableList;

import akyto.core.utils.location.LocationSerializer;
import it.unimi.dsi.fastutil.Pair;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import akyto.practice.Practice;
import akyto.practice.arena.Arena;
import akyto.practice.arena.ArenaType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArenaCommand implements CommandExecutor {

	private final Practice main;
	private final Map<UUID, Pair<Arena, Integer>> corners = new HashMap<>();
	private static final String[] helpMessage = new String[] {
			ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
			"",
			ChatColor.DARK_GRAY + "Arena Command Help" + ChatColor.GRAY + ":",
			ChatColor.WHITE + "/arena create <name> <NORMAL/SUMO>",
			ChatColor.WHITE + "/arena setpos<1/2> <name>",
			ChatColor.WHITE + "/arena delete <name>",
			ChatColor.WHITE + "/arena tp <name>",
			ChatColor.WHITE + "/arena save <name>",
			"",
			ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------"
	};

	public ArenaCommand(final Practice practice) {
		this.main = practice;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || !sender.isOp()) {
			sender.sendMessage(org.bukkit.ChatColor.RED + "You must be an op player to do that");
			return false;
		}

		final Player playerSender = (Player) sender;
		
		if (args.length == 0) {
			sender.sendMessage(helpMessage);
			return false;
		}

		if (args[0].equalsIgnoreCase("create")) {
			if (args.length != 3) {
				sender.sendMessage(helpMessage);
				return false;
			} else {
                ArenaType.valueOf(args[2].toUpperCase());
            }
			String name = args[1].toLowerCase();
            if (this.main.getManagerHandler().getArenaManager().getArena(name) != null) {
				sender.sendMessage(ChatColor.RED + "Sorry but this arena already exist.");
				return false;
			}
            ArenaType.valueOf(args[2].toUpperCase());
            this.main.getArenasMap().putIfAbsent(name, new Arena(main, name, LocationSerializer.fromBukkitLocation(playerSender.getLocation()), LocationSerializer.fromBukkitLocation(playerSender.getLocation()), ArenaType.valueOf(args[2].toUpperCase()), Material.PAPER));
			sender.sendMessage(ChatColor.GREEN + "You have succesfully create the " + args[1] + " arena into the " + args[2] + " type!");
			return false;
		}

		if (args[0].equalsIgnoreCase("tp")) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /arena tp <name>");
				return false;
			}

			Arena target = this.main.getManagerHandler().getArenaManager().getArena(args[1]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Arena \"" + args[1] + "\" not found");
				return false;
			}
			for (LocationSerializer loc : target.getPosition()) {
				if (loc != null) {
					playerSender.teleport(loc.toBukkitLocation());
					sender.sendMessage(ChatColor.GREEN + "Successfully teleported to " + target.getName());
					return false;
				}
			}
			sender.sendMessage(ChatColor.RED + "Not any location to teleport to :(");
			return false;
		}

		if (args[0].equalsIgnoreCase("setcorners")) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /arena setcorners <name>");
				return false;
			}

			Arena target = this.main.getManagerHandler().getArenaManager().getArena(args[1]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Arena \"" + args[1] + "\" not found");
				return false;
			}

			corners.put(playerSender.getUniqueId(), Pair.of(target, 1));
			sender.sendMessage(ChatColor.GREEN + "Left click on the first corner of " + target.getName());

			return false;
		}

		if (args[0].equalsIgnoreCase("delete")) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /arena delete <name>");
				return false;
			}

			Arena target = this.main.getManagerHandler().getArenaManager().getArena(args[1]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Arena \"" + args[1] + "\" not found");
				return false;
			}

			this.main.getArenasMap().remove(args[1].toLowerCase());

			sender.sendMessage(ChatColor.GREEN + "Successfully deleted " + args[1]);

			return false;
		}

		if (args[0].equalsIgnoreCase("setpos1") || args[0].equalsIgnoreCase("setpos2")) {
			if (args.length != 2) {
				sender.sendMessage(helpMessage);
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
				sender.sendMessage(helpMessage);
				return false;
			}
			if (this.main.getManagerHandler().getArenaManager().getArena(args[1]) == null) {
				sender.sendMessage(ChatColor.RED + "Sorry but this arena doesn't exist!");
				return false;
			}
			this.main.getManagerHandler().getArenaManager().getArena(args[1]).setIcon(playerSender.getItemInHand().getType());
			sender.sendMessage(ChatColor.GREEN + "The " + playerSender.getItemInHand().getType().toString() +" has been setted at icon of " + args[1] + " arena!");
			return false;
		}

		if (args[0].equalsIgnoreCase("save")) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /arena save <name>");
				return false;
			}

			Arena target = this.main.getManagerHandler().getArenaManager().getArena(args[1]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Arena \"" + args[1] + "\" not found");
				return false;
			}

			target.setSave(true);
			sender.sendMessage(ChatColor.GREEN + args[1] + " will be saved at next restart");

			return false;
		}
		return false;
	}

	public static class Completer implements TabCompleter {

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
			Validate.notNull(sender, "Sender cannot be null");
			Validate.notNull(args, "Arguments cannot be null");
			Validate.notNull(alias, "Alias cannot be null");

			if (args.length < 2) {
				return Stream.of("create", "setpos1", "setpos2", "delete", "tp")
						.filter(sub -> args.length == 0 || sub.startsWith(args[0].toLowerCase()))
						.collect(Collectors.toUnmodifiableList());
			}

			if (args.length < 3) {
				return Practice.getAPI().getArenasMap().keySet().stream()
						.filter(sub -> sub.startsWith(args[1].toLowerCase()))
						.collect(Collectors.toUnmodifiableList());
			}

			return ImmutableList.of();
		}
	}

}

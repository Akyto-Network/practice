package kezukdev.akyto.handler.command;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Pair;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.arena.ArenaType;
import kezukdev.akyto.utils.location.LocationSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.jar.JarEntry;
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
			ChatColor.WHITE + "/arena setcorners <name>",
			ChatColor.WHITE + "/arena tp <name>",
			"",
			ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------"
	};
	
	public ArenaCommand(final Practice practice) {
		this.main = practice;

		// Event handler for setting arena corners
		practice.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onCorner(PlayerInteractEvent event) {
				Player player = event.getPlayer();
				Block clicked = event.getClickedBlock();

				if (clicked == null || !corners.containsKey(player.getUniqueId()))
					return;

				event.setCancelled(true);

				Pair<Arena, Integer> editing = corners.remove(player.getUniqueId());
				Arena target = editing.first();
				int corner = editing.second();

				LocationSerializer clickedLoc = LocationSerializer.fromBukkitLocation(clicked.getLocation());

				if (corner == 1)
					target.setCorner1(clickedLoc);
				else
					target.setCorner2(clickedLoc);

				player.sendMessage(ChatColor.GREEN + String.format("Successfully set corner %d for arena %s to x=%f y=%f z=%f", corner, target.getName(), clickedLoc.getX(), clickedLoc.getY(), clickedLoc.getZ()));

				if (corner == 2)
					player.sendMessage(ChatColor.GREEN + "All corners have been set for arena " + target.getName() + "!");
				else {
					player.sendMessage(ChatColor.GREEN + "Left click on the second corner of " + target.getName());
					corners.put(player.getUniqueId(), Pair.of(target, 2));
				}
			}

			@EventHandler
			public void onQuit(PlayerQuitEvent event) {
				corners.remove(event.getPlayer().getUniqueId());
			}

			@EventHandler
			public void onKick(PlayerKickEvent event) {
				corners.remove(event.getPlayer().getUniqueId());
			}
		}, practice);
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
				return Stream.of("create", "setpos1", "setpos2", "setcorners", "delete", "tp")
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

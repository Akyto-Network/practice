package akyto.practice.utils.chat;

import java.util.*;
import java.util.stream.Collectors;

import akyto.practice.handler.manager.TournamentManager;
import akyto.practice.tournament.TournamentEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import akyto.core.utils.CoreUtils;
import akyto.core.utils.components.ComponentJoiner;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.kit.Kit;
import akyto.practice.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageUtils {
	
	public static TextComponent endMessage(final UUID winner, final UUID looser) {
		TextComponent winnerComponent = new TextComponent("Winner: ");
		winnerComponent.setColor(ChatColor.GREEN);
		TextComponent winnerNameComponent = new TextComponent(CoreUtils.getName(winner));
		winnerNameComponent.setColor(ChatColor.GRAY);
		winnerNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + CoreUtils.getName(winner)));
		TextComponent loserComponent = new TextComponent("Loser: ");
		loserComponent.setColor(ChatColor.RED);
		TextComponent loserNameComponent = new TextComponent(CoreUtils.getName(looser));
		loserNameComponent.setColor(ChatColor.GRAY);
		loserNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + CoreUtils.getName(looser)));
		TextComponent separatorComponent = new TextComponent(ChatColor.GRAY + " - ");
		winnerComponent.addExtra(winnerNameComponent);
		loserComponent.addExtra(loserNameComponent);
		winnerComponent.addExtra(separatorComponent);
		winnerComponent.addExtra(loserComponent);
		return winnerComponent;
	}
	
	public static void sendSplitMessage(final List<UUID> first, final List<UUID> second, final Kit ladder) {
	    final ComponentJoiner joinerOne = new ComponentJoiner(ChatColor.GRAY + ", ");
	    final ComponentJoiner joinerTwo = new ComponentJoiner(ChatColor.GRAY + ", ");
        final TextComponent firsttxt = new TextComponent(ChatColor.DARK_GRAY + CoreUtils.getName(first.getFirst()) + "'s teams" + ChatColor.GRAY + ": " + ChatColor.RED);
        final TextComponent secondtxt = new TextComponent(ChatColor.DARK_GRAY + CoreUtils.getName(second.getFirst()) + "'s teams" + ChatColor.GRAY + ": " + ChatColor.RED);
	    first.forEach(uuid -> {
	    	final TextComponent itxt = new TextComponent(CoreUtils.getName(uuid));
	        itxt.setColor(ChatColor.RED);
	        itxt.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD.toString() + Utils.getProfiles(uuid).getStats().get(2)[ladder.id()] + " elos").create()));
	        joinerOne.add(itxt);
	    });
	    second.forEach(uuid -> {
	    	final TextComponent itxt = new TextComponent(CoreUtils.getName(uuid));
	        itxt.setColor(ChatColor.RED);
	        itxt.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD.toString() + Utils.getProfiles(uuid).getStats().get(2)[ladder.id()] + " elos").create()));
	        joinerTwo.add(itxt);
	    });
	    firsttxt.addExtra(joinerOne.toTextComponent());
	    secondtxt.addExtra(joinerTwo.toTextComponent());
	    final List<List<UUID>> list = Arrays.asList(first, second);
	    list.forEach(uuids -> {
	    	for (UUID uuid : uuids) {
	    		Bukkit.getPlayer(uuid).spigot().sendMessage(firsttxt);
	    		Bukkit.getPlayer(uuid).spigot().sendMessage(secondtxt);
	    	}
	    });
	}
	
	public static void sendPartyComponent(final List<Set<UUID>> players) {
		final Duel duel = Utils.getDuelByUUID(players.getFirst().stream().toList().getFirst());
		if (duel.getDuelType().equals(DuelType.FFA)) {
			TextComponent invComponent = new TextComponent(ChatColor.YELLOW + "Inventorie(s)" + ChatColor.GRAY + ": ");
            final ComponentJoiner joiner = new ComponentJoiner(ChatColor.GRAY + ", ");
            players.forEach(uuid -> {
                for (UUID uuids : uuid) {
                    final TextComponent itxt = new TextComponent(CoreUtils.getName(uuids));
                    itxt.setColor(ChatColor.WHITE);
                    itxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + CoreUtils.getName(uuids) + "'s inventory").create()));
                    itxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + CoreUtils.getName(uuids)));
                    joiner.add(itxt);

                }
            });
            invComponent.addExtra(joiner.toTextComponent());
            players.forEach(uuid -> {
                for (UUID uuids : uuid) {
                    if (Bukkit.getPlayer(uuids) != null) {
                    	Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.YELLOW + "Match Information");
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY + "Winner: " + ChatColor.GOLD + CoreUtils.getName(Utils.getDuelByUUID(uuids).getWinner().getFirst()));
        				Bukkit.getPlayer(uuids).sendMessage(" ");
        				Bukkit.getPlayer(uuids).spigot().sendMessage(invComponent);
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
                    }
                }
            });
            return;
		}
		if (duel.getDuelType().equals(DuelType.SPLIT)) {
            TextComponent winnerComponent = new TextComponent(ChatColor.GRAY + "Winner(s)" + ChatColor.GRAY + ": ");
            TextComponent loserComponent = new TextComponent(ChatColor.GRAY + "Looser(s)" + ChatColor.GRAY + ": ");
            final ComponentJoiner joinerWin = new ComponentJoiner(ChatColor.GRAY + ", ");
            final ComponentJoiner joinerLose = new ComponentJoiner(ChatColor.GRAY + ", ");
            duel.getWinner().forEach(uuid -> {
                final TextComponent wtxt = new TextComponent(CoreUtils.getName(uuid));
                wtxt.setColor(ChatColor.GREEN);
                wtxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + CoreUtils.getName(uuid) + "'s inventory").create()));
                wtxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + CoreUtils.getName(uuid)));
                joinerWin.add(wtxt);
            });

            List<UUID> losers = duel.getFirst().containsAll(duel.getWinner()) ? new ArrayList<>(duel.getSecond()) : new ArrayList<>(duel.getFirst());

            losers.forEach(uuid -> {
                final TextComponent ltxt = new TextComponent(CoreUtils.getName(uuid));
                ltxt.setColor(ChatColor.RED);
                ltxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + CoreUtils.getName(uuid) + "'s inventory").create()));
                ltxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + CoreUtils.getName(uuid)));
				joinerLose.add(ltxt);
            });

            winnerComponent.addExtra(joinerWin.toTextComponent());
            loserComponent.addExtra(joinerLose.toTextComponent());

            players.forEach(uuidList -> uuidList.forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
                    player.spigot().sendMessage(winnerComponent);
                    player.spigot().sendMessage(loserComponent);
                    player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
                }
            }));
		}
	}

	public static void sendSingleEndMessage(final Duel duel, final List<UUID> players, final UUID winner, final UUID looser) {
		players.forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
				player.sendMessage(ChatColor.YELLOW + "Match Information");
				player.spigot().sendMessage(endMessage(winner, looser));
				if (duel.isRanked()) {
					player.sendMessage(ChatColor.GRAY + "Elo Changes: " + (winner.equals(uuid) ? ChatColor.GREEN + "+" : ChatColor.RED + "-") + (int) Math.min(Math.max(1.0D / (1.0D + Math.pow(10.0D, (Utils.getProfiles(winner).getStats().get(2)[duel.getKit().id()] - Utils.getProfiles(looser).getStats().get(2)[duel.getKit().id()]) / 400.0D)) * 32.0D, 4), 40));
				}
				if (!duel.getSpectators().isEmpty()) {
					player.sendMessage("");
					String specMsg = ChatColor.GRAY + "Spectators (" + duel.getSpectators().size() + ChatColor.GRAY + "): " +
							duel.getSpectators().stream()
									.map(spec -> ChatColor.WHITE + Bukkit.getPlayer(spec).getName())
									.collect(Collectors.joining(", "));
					player.sendMessage(specMsg);
				}
				player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
			}
		});
	}

	public static void sendTournamentRequestMessage(Player player, String tournamentId, String kitName, int teamSize) {
		TextComponent header = new TextComponent(org.bukkit.ChatColor.GRAY + " (*) " + org.bukkit.ChatColor.DARK_RED + org.bukkit.ChatColor.BOLD + "Tournament" + org.bukkit.ChatColor.GRAY + ": ");
		TextComponent kitLine = new TextComponent(org.bukkit.ChatColor.RED + "Kit" + org.bukkit.ChatColor.GRAY + ": " + org.bukkit.ChatColor.WHITE + ChatColor.stripColor(kitName));
		TextComponent teamSizeLine = new TextComponent(org.bukkit.ChatColor.RED + "Team Size" + org.bukkit.ChatColor.GRAY + ": " + org.bukkit.ChatColor.WHITE + teamSize + "v" + teamSize);

		header.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + tournamentId));
		header.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(org.bukkit.ChatColor.GRAY + "Join the " + ChatColor.stripColor(kitName) + " tournament!").create()));

		kitLine.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + tournamentId));
		kitLine.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(org.bukkit.ChatColor.GRAY + "Join the " + ChatColor.stripColor(kitName) + " tournament!").create()));

		teamSizeLine.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + tournamentId));
		teamSizeLine.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(org.bukkit.ChatColor.GRAY + "Join the " + ChatColor.stripColor(kitName) + " tournament!").create()));

		player.sendMessage(org.bukkit.ChatColor.GRAY.toString() + org.bukkit.ChatColor.STRIKETHROUGH + "----------------------------");
		player.spigot().sendMessage(header);
		player.sendMessage(" ");
		player.spigot().sendMessage(kitLine);
		player.spigot().sendMessage(teamSizeLine);
		player.sendMessage(org.bukkit.ChatColor.GRAY.toString() + org.bukkit.ChatColor.STRIKETHROUGH + "----------------------------");
	}

	public static void determineAndDisplayTournamentStage(TournamentEntry entry) {
		int participantsCount = entry.getParticipants().size();
		String stage = "";

		switch (participantsCount) {
			case 2:
				stage = "Final";
				break;
			case 4:
				stage = "Semi";
				break;
			case 8:
				stage = "Quarter";
				break;
			default:
				stage = "Qualification";
				break;
		}
		MessageHelper.notifyUUID(entry.getParticipants(), TournamentManager.prefix + " &fCurrent stage&7: &c" + stage);

	}
}

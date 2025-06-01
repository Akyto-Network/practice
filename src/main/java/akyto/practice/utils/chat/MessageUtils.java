package akyto.practice.utils.chat;

import akyto.core.Core;
import akyto.core.utils.CoreUtils;
import akyto.core.utils.components.ComponentJoiner;
import akyto.core.utils.format.FormatUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.handler.manager.QueueManager;
import akyto.practice.handler.manager.TournamentManager;
import akyto.practice.kit.Kit;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageUtils {
	
	public static TextComponent endMessage(final UUID winner, final UUID looser) {
		TextComponent winnerComponent = new TextComponent("Winner: ");
		winnerComponent.setColor(ChatColor.GREEN);
		TextComponent winnerNameComponent = new TextComponent(CoreUtils.getName(winner));
		winnerNameComponent.setColor(ChatColor.GRAY);
		winnerNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + CoreUtils.getName(winner)));
		winnerNameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to see inventory").create()));
		TextComponent loserComponent = new TextComponent("Loser: ");
		loserComponent.setColor(ChatColor.RED);
		TextComponent loserNameComponent = new TextComponent(CoreUtils.getName(looser));
		loserNameComponent.setColor(ChatColor.GRAY);
		loserNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + CoreUtils.getName(looser)));
		loserNameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to see inventory").create()));
		TextComponent separatorComponent = new TextComponent(ChatColor.GRAY + " ৷ ");
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

            List<UUID> losers = players.get(1).stream().toList();

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
				player.sendMessage(" ");
				player.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Match Results" + ChatColor.GRAY + ":");
				player.spigot().sendMessage(endMessage(winner, looser));
				if (duel.isRanked()) {
					player.sendMessage(ChatColor.WHITE + " Elo Changes: " + (winner.equals(uuid) ? ChatColor.GREEN + "+" : ChatColor.RED + "-") + (int) Math.min(Math.max(1.0D / (1.0D + Math.pow(10.0D, (Utils.getProfiles(winner).getStats().get(2)[duel.getKit().id()] - Utils.getProfiles(looser).getStats().get(2)[duel.getKit().id()]) / 400.0D)) * 32.0D, 4), 40));
				}
				if (!duel.getSpectators().isEmpty()) {
					player.sendMessage("");
					String specMsg = ChatColor.WHITE + "Spectators " + ChatColor.GRAY + "(" + ChatColor.RED + duel.getSpectators().size() + ChatColor.GRAY + "): " +
							duel.getSpectators().stream()
									.map(spec -> ChatColor.RED + Bukkit.getPlayer(spec).getName())
									.collect(Collectors.joining(ChatColor.GRAY + ", "));
					player.sendMessage(specMsg);
				}
				player.sendMessage(" ");
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

	public static void displayTournamentStage(TournamentEntry entry) {
		MessageHelper.notifyUUID(entry.getParticipants(), TournamentManager.prefix + " &fCurrent stage&7: &c" + determineTournamentStage(entry));
	}

	public static String determineTournamentStage(TournamentEntry entry) {
		int participantsCount = entry.getParticipants().size();
		String stage;

		if (participantsCount <= entry.getTeamSize() * 2) {
			stage = "Final";
		}
		else if (participantsCount <= entry.getTeamSize() * 4) {
			stage = "Semi";
		}
		else if (participantsCount <= entry.getTeamSize() * 8) {
			stage = "Quarter";
		}
		else {
			stage = "Qualification";
		}
		return stage;
	}

	public static String getColorTeam(final Duel duel, final UUID player) {
		return duel.getFirst().contains(player) ? org.bukkit.ChatColor.BLUE + "Blue" : org.bukkit.ChatColor.RED + "Red";
	}

	public static void sendQueueMessage(final Player player, QueueManager.QueueEntry entry) {
		final String[] message = new String[] {
			" ",
			ChatColor.DARK_RED.toString() + ChatColor.BOLD + (entry.isRanked() ? "Ranked" : "Unranked") + " Queue" + ChatColor.GRAY + ":",
			" ",
			ChatColor.WHITE + " Kit: " + ChatColor.RED + ChatColor.stripColor(entry.getKit().displayName()),
			ChatColor.GRAY.toString() + ChatColor.ITALIC + " Searching for a match...",
			" "
		};
		player.sendMessage(message);
	}

	public static void sendMatchFound(final Duel duel, final UUID player) {
		final UUID oppId = duel.getFirst().contains(player) ? duel.getSecond().stream().toList().getFirst() : duel.getFirst().stream().toList().getFirst();
		final String oppName = CoreUtils.getName(oppId);
		final int eloOpps = Utils.getProfiles(oppId).getStats().get(2)[duel.getKit().id()];
		String[] message = null;
		if (!duel.isRanked()) {
			message = new String[] {
					" ",
					ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Match Founded" + ChatColor.GRAY + ":",
					" ",
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Arena: " + ChatColor.RED + duel.getArena().getName(),
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Opponent: " + ChatColor.RED + oppName + ChatColor.GRAY + " (" + ChatColor.YELLOW + Bukkit.getPlayer(oppId).getPing() + "ms" + ChatColor.GRAY + ")",
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Your Ping: " + ChatColor.RED + Bukkit.getPlayer(player).getPing() + "ms",
					" "
			};
		}
		else {
			message = new String[] {
					" ",
					ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Match Informations" + ChatColor.GRAY + ":",
					" ",
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Arena: " + ChatColor.RED + duel.getArena().getName(),
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Opponent: " + ChatColor.RED + oppName + ChatColor.GRAY + " (" + ChatColor.YELLOW + Bukkit.getPlayer(oppId).getPing() + "ms" + ChatColor.GRAY + ")",
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Your Ping: " + ChatColor.RED + Bukkit.getPlayer(player).getPing() + "ms",
					" ",
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Their Elo: " + ChatColor.RED + eloOpps,
					ChatColor.GRAY + " • " + ChatColor.WHITE + " Your Elo: " + ChatColor.RED + Utils.getProfiles(player).getStats().get(2)[duel.getKit().id()],
					" "
			};
		}
		Bukkit.getPlayer(player).sendMessage(message);
	}

	public static void matchHistory(final String nameOne, final String nameTwo, final String kit, final String arena, final String duration, final boolean ranked, final int eloA, final int eloB) {
		String channelId = "1369877567770857502";

		TextChannel channel = Core.API.getJda().getTextChannelById(channelId);
		if (channel == null) {
			System.err.println("The channel with '" + channelId + "' id not found.");
			return;
		}

		EmbedBuilder embedBuilder = new EmbedBuilder();
		String title = nameOne + (ranked ? " [" + eloA + "]" : "") + " against " + nameTwo + (ranked ? " [" + eloB + "]" : "");
		embedBuilder.setTitle(title);
		embedBuilder.setColor(Color.RED);
		embedBuilder.addField("Type: ", ranked ? "Ranked" : "Unranked", true);
		embedBuilder.addField("Kit: ", kit, true);
		embedBuilder.addField("Arena: ", arena, true);
		embedBuilder.addBlankField(false);
		embedBuilder.addField("Statistics of " + nameOne, "", false);
		final DuelStatistics duelStatisticsA = Practice.API.getManagerHandler().getProfileManager().getDuelStatistics().get(CoreUtils.getUUID(nameOne));
		embedBuilder.addField("Hits: ", String.valueOf(duelStatisticsA.getHits()), true);
		embedBuilder.addField("Longest Hits: ", String.valueOf(duelStatisticsA.getLongestHit()), true);
		if (!kit.equalsIgnoreCase("Sumo")) {
			embedBuilder.addField("Block-Hit: ", String.valueOf(duelStatisticsA.getBlockHit()), true);
		}
		final List<String> kitContainsPotion = Arrays.asList("NoDebuff", "Debuff", "NoEnchant", "Axe");
		if (kitContainsPotion.contains(kit)) {
			embedBuilder.addField("Pot Throw: ", String.valueOf(duelStatisticsA.getPotThrown()), true);
			embedBuilder.addField("Pot Missed: ", String.valueOf(duelStatisticsA.getPotMissed()), true);
			embedBuilder.addField("Pot Accuracy: ", String.format("%.2f", FormatUtils.potionAccuracy(duelStatisticsA.getPotThrown(), duelStatisticsA.getPotHeal())) + "%", true);
		}
		embedBuilder.addBlankField(true);
		embedBuilder.addField("Statistics of " + nameTwo, "", false);
		final DuelStatistics duelStatisticsB = Practice.API.getManagerHandler().getProfileManager().getDuelStatistics().get(CoreUtils.getUUID(nameTwo));
		embedBuilder.addField("Hits: ", String.valueOf(duelStatisticsB.getHits()), true);
		embedBuilder.addField("Longest Hits: ", String.valueOf(duelStatisticsB.getLongestHit()), true);
		if (!kit.equalsIgnoreCase("Sumo")) {
			embedBuilder.addField("Block-Hit: ", String.valueOf(duelStatisticsB.getBlockHit()), true);
		}
		if (kitContainsPotion.contains(kit)) {
			embedBuilder.addField("Pot Throw: ", String.valueOf(duelStatisticsB.getPotThrown()), true);
			embedBuilder.addField("Pot Missed: ", String.valueOf(duelStatisticsB.getPotMissed()), true);
			embedBuilder.addField("Pot Accuracy: ", String.format("%.2f", FormatUtils.potionAccuracy(duelStatisticsB.getPotThrown(), duelStatisticsB.getPotHeal())) + "%", true);
		}
		embedBuilder.addField("Winner:", nameOne, true);
		embedBuilder.addBlankField(false);
		embedBuilder.setFooter("Match ended in " + duration);

		MessageCreateData messageData = MessageCreateData.fromEmbeds(embedBuilder.build());
		channel.sendMessage(messageData).queue();
	}
}

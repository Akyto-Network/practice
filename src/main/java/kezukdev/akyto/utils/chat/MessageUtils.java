package kezukdev.akyto.utils.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageUtils {
	
	public static TextComponent endMessage(final UUID winner, final UUID looser) {
		TextComponent winnerComponent = new TextComponent("Winner: ");
		winnerComponent.setColor(ChatColor.GREEN);
		TextComponent winnerNameComponent = new TextComponent(Utils.getName(winner));
		winnerNameComponent.setColor(ChatColor.GRAY);
		winnerNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + Utils.getName(winner)));
		TextComponent loserComponent = new TextComponent("Loser: ");
		loserComponent.setColor(ChatColor.RED);
		TextComponent loserNameComponent = new TextComponent(Utils.getName(looser));
		loserNameComponent.setColor(ChatColor.GRAY);
		loserNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + Utils.getName(looser)));
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
        final TextComponent firsttxt = new TextComponent(ChatColor.DARK_GRAY + Utils.getName(first.get(0)) + "'s teams" + ChatColor.GRAY + ": " + ChatColor.RED);
        final TextComponent secondtxt = new TextComponent(ChatColor.DARK_GRAY + Utils.getName(second.get(0)) + "'s teams" + ChatColor.GRAY + ": " + ChatColor.RED);
	    first.forEach(uuid -> {
	    	final TextComponent itxt = new TextComponent(Utils.getName(uuid));
	        itxt.setColor(ChatColor.RED);
	        itxt.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD.toString() + Practice.getAPI().getManagerHandler().getProfileManager().getProfiles().get(uuid).getStats().get(2)[ladder.id()] + " elos").create()));
	        joinerOne.add(itxt);
	    });
	    second.forEach(uuid -> {
	    	final TextComponent itxt = new TextComponent(Utils.getName(uuid));
	        itxt.setColor(ChatColor.RED);
	        itxt.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD.toString() + Practice.getAPI().getManagerHandler().getProfileManager().getProfiles().get(uuid).getStats().get(2)[ladder.id()] + " elos").create()));
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
	
	public static void sendPartyComponent(final List<List<UUID>> players) {
		final Duel duel = Utils.getDuelByUUID(players.get(0).get(0));
		if (duel.getDuelType().equals(DuelType.FFA)) {
			TextComponent invComponent = new TextComponent(ChatColor.YELLOW + "Inventorie(s)" + ChatColor.GRAY + ": ");
            final ComponentJoiner joiner = new ComponentJoiner(ChatColor.GRAY + ", ");
            players.forEach(uuid -> {
                for (UUID uuids : uuid) {
                    final TextComponent itxt = new TextComponent(Utils.getName(uuids));
                    itxt.setColor(ChatColor.WHITE);
                    itxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + Utils.getName(uuids) + "'s inventory").create()));
                    itxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + Utils.getName(uuids)));
                    joiner.add(itxt);

                }
            });
            invComponent.addExtra(joiner.toTextComponent());
            players.forEach(uuid -> {
                for (UUID uuids : uuid) {
                    if (Bukkit.getPlayer(uuids) != null) {
                    	Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.YELLOW + "Match Information");
        				Bukkit.getPlayer(uuids).sendMessage(ChatColor.GRAY + "Winner: " + ChatColor.GOLD + Utils.getName(Utils.getDuelByUUID(uuids).getWinner().get(0)));
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
                final TextComponent wtxt = new TextComponent(Utils.getName(uuid));
                wtxt.setColor(ChatColor.GREEN);
                wtxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + Utils.getName(uuid) + "'s inventory").create()));
                wtxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + Utils.getName(uuid)));
                joinerWin.add(wtxt);
            });

            List<UUID> losers = duel.getFirst().containsAll(duel.getWinner()) ? new ArrayList<>(duel.getSecond()) : new ArrayList<>(duel.getFirst());

            losers.forEach(uuid -> {
                final TextComponent ltxt = new TextComponent(Utils.getName(uuid));
                ltxt.setColor(ChatColor.RED);
                ltxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to view " + Utils.getName(uuid) + "'s inventory").create()));
                ltxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + Utils.getName(uuid)));
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

}
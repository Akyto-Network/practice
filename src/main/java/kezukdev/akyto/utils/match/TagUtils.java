package kezukdev.akyto.utils.match;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import akyto.core.utils.CoreUtils;

public class TagUtils {
	
	public static void setupTeams(final List<UUID> first, List<UUID> second) {
		first.forEach(uuid -> {
			Scoreboard sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
			if (!Bukkit.getPlayer(uuid).getScoreboard().equals(sb)) {
				sb = Bukkit.getPlayer(uuid).getScoreboard();
			}
			final Team team1 = sb.registerNewTeam("green");
			team1.setPrefix(ChatColor.GREEN.toString());
			team1.addEntry(CoreUtils.getName(uuid));
			team1.setAllowFriendlyFire(false);
			final Team team2 = sb.registerNewTeam("red");
			team2.setPrefix(ChatColor.RED.toString());
			second.forEach(secondUUID -> team2.addEntry(CoreUtils.getName(secondUUID)));
			Bukkit.getPlayer(uuid).setScoreboard(sb);
		});
		second.forEach(uuid -> {
			Scoreboard sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
			if (!Bukkit.getPlayer(uuid).getScoreboard().equals(sb)) {
				sb = Bukkit.getPlayer(uuid).getScoreboard();
			}
			final Team team1 = sb.registerNewTeam("green");
			team1.setPrefix(ChatColor.GREEN.toString());
			team1.addEntry(CoreUtils.getName(uuid));
			team1.setAllowFriendlyFire(false);
			final Team team2 = sb.registerNewTeam("red");
			team2.setPrefix(ChatColor.RED.toString());
			first.forEach(firstUUID -> team2.addEntry(CoreUtils.getName(firstUUID)));
			Bukkit.getPlayer(uuid).setScoreboard(sb);
		});
	}
	
	public static void clearEntries(final List<List<UUID>> players) {
		players.get(0).forEach(first -> clearNameTags(Bukkit.getPlayer(first)));
		players.get(1).forEach(first -> clearNameTags(Bukkit.getPlayer(first)));
	}
	
    public static void clearNameTags(Player player) {
    	if (player == null) return;
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) return;
        clearEntries(scoreboard.getTeam("green"));
        clearEntries(scoreboard.getTeam("red"));
        scoreboard.getTeam("green").unregister();
        scoreboard.getTeam("red").unregister();
    }

    private static void clearEntries(Team team) {
        if (team == null) return;
        team.getEntries().forEach(team::removeEntry);
    }
	
	public static void setupRank(final Team rank, final Player player) {
		
	}
}

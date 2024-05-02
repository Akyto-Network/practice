package kezukdev.akyto.utils;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TagUtils {
	
	public static void setupTeams(final List<List<UUID>> players) {
		players.get(0).forEach(uuid -> {
			Scoreboard sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
			if (!Bukkit.getPlayer(uuid).getScoreboard().equals(sb)) {
				sb = Bukkit.getPlayer(uuid).getScoreboard();
			}
			final Team team1 = sb.registerNewTeam("green");
			team1.setPrefix(ChatColor.GREEN.toString());
			team1.addEntry(Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName());
			team1.setAllowFriendlyFire(false);
			final Team team2 = sb.registerNewTeam("red");
			team2.setPrefix(ChatColor.RED.toString());
			players.get(1).forEach(second -> team2.addEntry(Bukkit.getPlayer(second) != null ? Bukkit.getPlayer(second).getName() : Bukkit.getOfflinePlayer(second).getName()));
			Bukkit.getPlayer(uuid).setScoreboard(sb);
		});
		players.get(1).forEach(uuid -> {
			Scoreboard sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
			if (!Bukkit.getPlayer(uuid).getScoreboard().equals(sb)) {
				sb = Bukkit.getPlayer(uuid).getScoreboard();
			}
			final Team team1 = sb.registerNewTeam("green");
			team1.setPrefix(ChatColor.GREEN.toString());
			team1.addEntry(Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName());
			team1.setAllowFriendlyFire(false);
			final Team team2 = sb.registerNewTeam("red");
			team2.setPrefix(ChatColor.RED.toString());
			players.get(0).forEach(second -> team2.addEntry(Bukkit.getPlayer(second) != null ? Bukkit.getPlayer(second).getName() : Bukkit.getOfflinePlayer(second).getName()));
			Bukkit.getPlayer(uuid).setScoreboard(sb);
		});
	}
	
	public static void clearEntries(final List<List<UUID>> players) {
		players.get(0).forEach(first -> clearNameTags(Bukkit.getPlayer(first)));
		players.get(1).forEach(first -> clearNameTags(Bukkit.getPlayer(first)));
	}
	
    public static void clearNameTags(Player player) {
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

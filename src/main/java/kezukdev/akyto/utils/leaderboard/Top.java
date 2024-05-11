package kezukdev.akyto.utils.leaderboard;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
public class Top {

    private int elo_id;
    private final Map<String, Integer> topBoard = new HashMap<>();
    private final ArrayList<String> loreRanked = new ArrayList<>();
    private final ArrayList<String> lore = new ArrayList<>();

    public Top(int elo_id, Map<String, int[]> map) {
    	this.elo_id = elo_id;
        extirpate(map);
        organise();
    }

    public Top(Map<String, int[]> map) {
        map.forEach((key, value) -> topBoard.put(key, (int) Arrays.stream(value).average().orElse(0)));
        organise();
    }

    private void extirpate(Map<String, int[]> map) {
        map.forEach((key, value) -> topBoard.put(key, value[elo_id]));
    }

    private void organise() {
        List<Map.Entry<String, Integer>> entries = topBoard.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int x = 1;
        for (Map.Entry<String, Integer> entry : entries) {
            if (x <= 3) {
            	loreRanked.add(ChatColor.DARK_GRAY + "#" + x + " " + ChatColor.RED + entry.getKey() + ChatColor.GRAY + " (" + ChatColor.WHITE + entry.getValue() + ChatColor.GRAY + ")");
            }
            if (x <= 10) {
            	lore.add(ChatColor.DARK_GRAY + "#" + x + " " + ChatColor.RED + entry.getKey() + ChatColor.GRAY + " (" + ChatColor.WHITE + entry.getValue() + ChatColor.GRAY + ")");
            }
        	x++;
        }
    }
}
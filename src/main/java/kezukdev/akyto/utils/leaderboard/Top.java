package kezukdev.akyto.utils.leaderboard;

import java.util.*;
import java.util.stream.Collectors;

import kezukdev.akyto.Practice;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
public class Top {

    private int elo_id;
    private final Map<String, Integer> topboard = new HashMap<>();
    private final ArrayList<String> loreRanked = new ArrayList<>();
    private final ArrayList<String> lore = new ArrayList<>();

    public Top(int elo_id, Map<String, int[]> map) {
    	this.elo_id = elo_id;
        extirpate(map);
        organise();
    }

    public Top(Map<String, int[]> map, final Practice main) {
    	for(Map.Entry<String, int[]> entry : map.entrySet()) {
            int global_elo=0;
            for(int elo : entry.getValue()) {
                global_elo+=elo;
            }
            global_elo = global_elo/entry.getValue().length;
            topboard.put(entry.getKey(), global_elo);
        }
        organise();
    }

    private void extirpate(Map<String, int[]> map) {
        map.forEach((key, value) -> topboard.put(key, value[elo_id]));
    }

    private void organise() {
        List<Map.Entry<String, Integer>> entries = topboard.entrySet().stream().sorted(Map.Entry.comparingByValue()).limit(topboard.size()).collect(Collectors.toList());
        Collections.reverse(entries);
        int x=1;
        for(Map.Entry<String, Integer> entry : entries) {
            if(x <= 3) {
            	loreRanked.add(ChatColor.DARK_GRAY + "#" + x + " " + ChatColor.RED + entry.getKey() + ChatColor.GRAY + " (" + ChatColor.WHITE + entry.getValue() + ChatColor.GRAY + ")");
            }
            if(x <= 10) {
            	lore.add(ChatColor.DARK_GRAY + "#" + x + " " + ChatColor.RED + entry.getKey() + ChatColor.GRAY + " (" + ChatColor.WHITE + entry.getValue() + ChatColor.GRAY + ")");
            }
        	x++;
        }
    }
}
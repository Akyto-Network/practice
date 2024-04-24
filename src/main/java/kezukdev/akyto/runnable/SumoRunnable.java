package kezukdev.akyto.runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;

public class SumoRunnable extends BukkitRunnable {
	
	private Practice main;
    private final Duel match;
    
    public SumoRunnable(final Practice main, final Duel match) {
    	this.main = main;
        this.match = match;
    }
    
    public void run() {
        if (match == null) {
            this.cancel();
            return;
        }
        List<UUID> uuids = new ArrayList<UUID>();
        uuids.addAll(match.getFirst());
        uuids.addAll(match.getSecond());
        for (final UUID uuid : uuids) {
        	final Player player = Bukkit.getPlayer(uuid);
            if (player.getLocation().getBlock().isLiquid()) {
                this.cancel();
                this.main.getManagerHandler().getDuelManager().endSingle(match.getFirst().contains(player.getUniqueId()) ? match.getSecond().stream().collect(Collectors.toList()).get(0) : match.getFirst().stream().collect(Collectors.toList()).get(0));
                break;
            }
        }
    }
}
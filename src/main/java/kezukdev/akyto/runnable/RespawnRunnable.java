package kezukdev.akyto.runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.utils.Utils;
import kezukdev.akyto.utils.match.MatchUtils;

public class RespawnRunnable extends BukkitRunnable {
    
    private final Practice main;
    private final List<Set<UUID>> players;
    private final Duel duel;
    
    public RespawnRunnable(final List<Set<UUID>> players, final Practice main) {
        this.main = main;
        this.players = players;
        this.duel = Utils.getDuelByUUID(players.getFirst().stream().toList().getFirst()) != null ? Utils.getDuelByUUID(players.getFirst().stream().toList().getFirst()) : Utils.getDuelByUUID(players.getLast().stream().toList().getFirst());
    }

    @Override
    public void run() {
        players.forEach(uuids -> uuids.forEach(uuid -> {
            Utils.sendToSpawn(uuid, true);
            if (Bukkit.getPlayer(uuid) != null) {
            	MatchUtils.multiArena(uuid, true, false);
            }
        }));
        final boolean ranked = duel.isRanked();
        final Kit kit = duel.getKit();
		MatchUtils.clearDrops(new ArrayList<>(duel.getFirst()).getFirst() != null ? new ArrayList<>(duel.getFirst()).getFirst() : new ArrayList<>(duel.getSecond()).getFirst());
        main.getDuels().remove(duel);
        main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);	
        main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
        cancel();
    }
}

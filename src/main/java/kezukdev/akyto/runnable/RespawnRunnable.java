package kezukdev.akyto.runnable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.utils.Utils;
import kezukdev.akyto.utils.match.MatchUtils;

public class RespawnRunnable extends BukkitRunnable {
    
    private final Practice main;
    private final List<List<UUID>> players;
    private final Duel duel;
    
    public RespawnRunnable(final List<List<UUID>> players, final Practice main) {
        this.main = main;
        this.players = players;
        this.duel = Utils.getDuelByUUID(players.get(0).get(0));
    }

    @Override
    public void run() {
        players.forEach(uuids -> uuids.forEach(uuid -> {
            Utils.sendToSpawn(uuid, true);
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (Bukkit.getPlayer(uuid) != null) {
                    player.showPlayer(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).showPlayer(player);
                }
            });
        }));
        final boolean ranked = duel.isRanked();
        final Kit kit = duel.getKit();
		MatchUtils.clearDrops(duel.getFirst().stream().collect(Collectors.toList()).get(0) != null ? duel.getFirst().stream().collect(Collectors.toList()).get(0) : duel.getSecond().stream().collect(Collectors.toList()).get(0));
        main.getDuels().remove(duel);
        main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);	
        main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
        cancel();
    }
}

package kezukdev.akyto.runnable;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.kit.Kit;

public class RespawnRunnable extends BukkitRunnable {
    
    private final Practice main;
    private final List<List<UUID>> players;
    private final Duel duel;
    
    public RespawnRunnable(final List<List<UUID>> players, final Practice main) {
        this.main = main;
        this.players = players;
        this.duel = this.main.getUtils().getDuelByUUID(players.get(0).get(0));
    }

    @Override
    public void run() {
        players.forEach(uuids -> uuids.forEach(uuid -> {
            main.getUtils().sendToSpawn(uuid, true);
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (Bukkit.getPlayer(uuid) != null) {
                    player.showPlayer(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).showPlayer(player);
                }
            });
        }));
        final boolean ranked = duel.isRanked();
        final Kit kit = duel.getKit();
        main.getDuels().remove(duel);
        main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);	
        main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
        cancel();
    }
}

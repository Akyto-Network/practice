package akyto.practice.runnable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.kit.Kit;
import akyto.practice.utils.Utils;
import akyto.practice.utils.match.MatchUtils;

public class RespawnRunnable extends BukkitRunnable {

    private final Practice main;
    private final List<Set<UUID>> players;
    private final List<UUID> spectators;
    private Duel duel;

    public RespawnRunnable(final List<Set<UUID>> players, final List<UUID> spectators, final Practice main) {
        this.main = main;
        this.players = players;
        this.spectators = spectators;
        if (!players.isEmpty() && !players.getFirst().isEmpty()) {
            UUID firstPlayerUUID = players.getFirst().iterator().next();
            duel = Utils.getDuelByUUID(firstPlayerUUID);
        }
    }

    @Override
    public void run() {
        this.players.forEach(uuids -> uuids.forEach(playersInMatch -> {
            if (!duel.getDisconnected().contains(playersInMatch)) {
                Utils.sendToSpawn(playersInMatch, true);
                if (Bukkit.getPlayer(playersInMatch) != null) {
                    MatchUtils.multiArena(playersInMatch, true, false);
                }
            }
        }));
        if (!this.spectators.isEmpty()) {
            this.spectators.forEach(uuid -> {
                Utils.sendToSpawn(uuid, true);
                if (Bukkit.getPlayer(uuid) != null) {
                    MatchUtils.multiArena(uuid, true, false);
                }
            });
        }
        if (duel != null) {
            final boolean ranked = duel.isRanked();
            final Kit kit = duel.getKit();
            UUID firstPlayerUUID = duel.getFirst().iterator().next();
            MatchUtils.clearDrops(firstPlayerUUID);
            main.getDuels().remove(duel);
            main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);
            main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
        }
        cancel();
    }
}
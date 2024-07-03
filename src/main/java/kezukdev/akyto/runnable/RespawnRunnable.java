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
    private Duel duel;

    public RespawnRunnable(final List<Set<UUID>> players, final Practice main) {
        this.main = main;
        this.players = players;

        if (!players.isEmpty() && !players.get(0).isEmpty()) {
            UUID firstPlayerUUID = players.get(0).iterator().next();
            duel = Utils.getDuelByUUID(firstPlayerUUID);
        }
    }

    @Override
    public void run() {
        for (Set<UUID> uuids : players) {
            for (UUID uuid : uuids) {
                Duel duelGetter = duel != null ? duel : Utils.getDuelBySpectator(uuid);
                if (duelGetter != null && !duelGetter.getDisconnected().contains(uuid)) {
                    Utils.sendToSpawn(uuid, true);
                    if (Bukkit.getPlayer(uuid) != null) {
                        MatchUtils.multiArena(uuid, true, false);
                    }
                }
            }
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
package kezukdev.akyto.runnable;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.DuelParty;
import kezukdev.akyto.kit.Kit;

public class RespawnRunnable extends BukkitRunnable {
    
    private final Practice main;
    private List<List<UUID>> players;
    private boolean party;
    private Duel duel;
    private DuelParty duelParty;
    
    public RespawnRunnable(final List<List<UUID>> players, final boolean party, final Practice main) {
        this.main = main;
        this.players = players;
        this.party = party;
        if (party) this.duelParty = this.main.getUtils().getDuelPartyByUUID(players.get(0).get(0));
        if (!party) this.duel = this.main.getUtils().getDuelByUUID(players.get(0).get(0));
    }

    @Override
    public void run() {
        players.forEach(uuids -> {
        	uuids.forEach(uuid -> {
            	main.getUtils().sendToSpawn(uuid, true);
            	Bukkit.getOnlinePlayers().forEach(player -> {
            		if (Bukkit.getPlayer(uuid) != null) {
                		player.showPlayer(Bukkit.getPlayer(uuid));
                		Bukkit.getPlayer(uuid).showPlayer(player);	
            		}
            	});
            });
        });
        if(!party) {
            final boolean ranked = duel.isRanked();
            final Kit kit = duel.getKit();
            main.getDuels().remove(duel);
            main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);	
        }
        if (party) { main.getDuelsParty().remove(duelParty); }
        main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
        cancel();
    }
}

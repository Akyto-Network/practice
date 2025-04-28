package akyto.practice.runnable;

import akyto.practice.event.EventState;
import akyto.practice.Practice;
import akyto.practice.handler.manager.EventManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SumoEventRunnable extends BukkitRunnable {

	private final Practice main;

    public SumoEventRunnable(final Practice main) {
    	this.main = main;
    }
    
    public void run() {
        final EventManager eventManager = main.getManagerHandler().getEventManager();
        if (eventManager == null || eventManager.getEventState().equals(EventState.ENDING)) {
            this.cancel();
            return;
        }
        for (final UUID uuid : eventManager.getPlayersSelected()) {
        	final Player player = main.getServer().getPlayer(uuid);
            if (player != null && player.getLocation().getBlock().isLiquid()) {
                this.cancel();
                eventManager.endMatch(eventManager.getPlayersSelected().get(0) == player.getUniqueId() ? eventManager.getPlayersSelected().get(1) : eventManager.getPlayersSelected().get(0));
                break;
            }
        }
    }
}
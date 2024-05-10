package kezukdev.akyto.runnable;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.utils.Utils;

public class PearlExpireRunnable extends BukkitRunnable {
	
	private Player player;
	private Duel duel;
	
	public PearlExpireRunnable(final Player player, final Duel duel) {
		this.player = player;
		this.duel = duel;
	}

	@Override
	public void run() {
		if (player == null || duel == null || duel != Utils.getDuelByUUID(player.getUniqueId())) {
			this.cancel();
			return;
		}
		if (duel.getState().equals(DuelState.FINISHING)) {
			this.cancel();
			return;
		}
		player.sendMessage(ChatColor.GREEN + "Your pearl cooldown has just expired, you can now launch another one!");
	}
}

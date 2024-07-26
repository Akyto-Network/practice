package akyto.practice.runnable;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import akyto.practice.duel.Duel;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.utils.Utils;

public class PearlExpireRunnable extends BukkitRunnable {
	
	private final Player player;
	private final Duel duel;
	
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

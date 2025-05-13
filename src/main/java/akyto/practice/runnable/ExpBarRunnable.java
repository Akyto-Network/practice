package akyto.practice.runnable;

import akyto.practice.Practice;
import akyto.practice.utils.timer.impl.EnderpearlTimer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ExpBarRunnable implements Runnable {
	private final Practice plugin = Practice.API;

	@Override
	public void run() {
		EnderpearlTimer timer = Practice.API.getManagerHandler().getTimerManager().getTimer(EnderpearlTimer.class);

		for (UUID uuid : timer.getCooldowns().keySet()) {
			Player player = this.plugin.getServer().getPlayer(uuid);
			if (player != null) {
				long time = timer.getRemaining(player);
				int seconds = (int)Math.round((double)time / 1000.0);
				player.setLevel(seconds);
				player.setExp((float)time / 15000.0F);
			}
		}
	}
}

package kezukdev.akyto.runnable;

import java.util.List;
import java.util.Timer;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import gym.core.utils.TimeUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.kit.KitInterface;

public class CountdownRunnable extends BukkitRunnable {
	
	private final Duel duel;
	private final Practice main;
	private final List<List<UUID>> players;
	private Integer counter;
	
	public CountdownRunnable(final List<List<UUID>> players, final Duel duel, final Practice main) {
		this.duel = duel;
		this.main = main;
		this.players = players;
		this.counter = 6;
		this.run();
	}

	@Override
	public void run() {
		if (duel == null || duel.getState().equals(DuelState.FINISHING) || duel.getState().equals(DuelState.PLAYING)) {
			this.cancel();
			return;
		}

		counter -= 1;
		if (counter > 0) {
            players.forEach(uuids -> uuids.forEach(uuid -> {
				final Player player = main.getServer().getPlayer(uuid);
                if (player != null) {
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.5f, 1.5f);
                    player.sendMessage(ChatColor.RED.toString() + counter + "s" + ChatColor.GRAY + "...");
                }
            }));
        } else {
            final KitInterface kit = (KitInterface) duel.getKit();
			players.forEach(uuids -> uuids.forEach(uuid -> {
				final Player player = main.getServer().getPlayer(uuid);
                if (player != null) {
                    if (player.getInventory().contains(Material.BOOK)) {
                        player.getInventory().setArmorContents(kit.armor());
                        player.getInventory().setContents(kit.content());
                        player.updateInventory();
                    }
                    player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1f, 1f);
                    player.sendMessage(ChatColor.DARK_GRAY + "The match has begun" + ChatColor.GRAY + "," + ChatColor.WHITE + " good luck.");
                }
            }));
			this.duel.timer = new Timer();
			this.duel.startTime = System.currentTimeMillis();
			TimeUtils.startDuration(duel);
			duel.setState(DuelState.PLAYING);
			this.cancel();
		}
	}

}

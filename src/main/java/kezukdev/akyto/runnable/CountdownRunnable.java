package kezukdev.akyto.runnable;

import java.util.List;
import java.util.Timer;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.kit.KitInterface;

public class CountdownRunnable extends BukkitRunnable {
	
	private Practice main;
	private List<List<UUID>> players;
	private Integer counter;
	
	public CountdownRunnable(final List<List<UUID>> players, final Practice main) {
		this.main = main;
		this.players = players;
		this.counter = 6;
		this.run();
	}

	@Override
	public void run() {
		if ((main.getUtils().getDuelByUUID(players.get(0).get(0)) != null && (main.getUtils().getDuelByUUID(players.get(0).get(0)).getState().equals(DuelState.FINISHING) || main.getUtils().getDuelByUUID(players.get(0).get(0)).getState().equals(DuelState.PLAYING))) || (main.getUtils().getDuelPartyByUUID(players.get(0).get(0)) != null && (main.getUtils().getDuelPartyByUUID(players.get(0).get(0)).getState().equals(DuelState.FINISHING) || main.getUtils().getDuelPartyByUUID(players.get(0).get(0)).getState().equals(DuelState.PLAYING))) ) {
			this.cancel();
			return;
		}
		counter -= 1;
		if (counter <= 0) {
			players.forEach(uuids -> {
				uuids.forEach(uuid -> {
					if (main.getUtils().getDuelByUUID(players.get(0).get(0)) != null) {
						main.getUtils().getDuelByUUID(players.get(0).get(0)).setState(DuelState.STARTING);
					}
					if (main.getUtils().getDuelPartyByUUID(players.get(0).get(0)) != null) {
						main.getUtils().getDuelPartyByUUID(players.get(0).get(0)).setState(DuelState.STARTING);
					}
					if (Bukkit.getPlayer(uuid) != null) {
						if (Bukkit.getPlayer(uuid).getInventory().contains(Material.BOOK)) {
							final KitInterface kit = this.main.getUtils().getDuelByUUID(uuid) != null ? (KitInterface) this.main.getUtils().getDuelByUUID(uuid).getKit() : (KitInterface) this.main.getUtils().getDuelPartyByUUID(uuid).getKit();
							Bukkit.getPlayer(uuid).getInventory().setArmorContents(kit.armor());
							Bukkit.getPlayer(uuid).getInventory().setContents(kit.content());
							Bukkit.getPlayer(uuid).updateInventory();
						}
						Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.FIREWORK_LARGE_BLAST, 1f, 1f);
						Bukkit.getPlayer(uuid).sendMessage(ChatColor.DARK_GRAY + "The match has begun" + ChatColor.GRAY + "," + ChatColor.WHITE + " good luck.");
						if (main.getUtils().getDuelByUUID(players.get(0).get(0)) != null) {
							main.getUtils().getDuelByUUID(players.get(0).get(0)).timer = new Timer();
							main.getUtils().getDuelByUUID(players.get(0).get(0)).startTime = System.currentTimeMillis();
							this.main.getUtils().startSingleDuration(main.getUtils().getDuelByUUID(players.get(0).get(0)));
						}
						if (main.getUtils().getDuelPartyByUUID(players.get(0).get(0)) != null) {
							main.getUtils().getDuelPartyByUUID(players.get(0).get(0)).timer = new Timer();
							main.getUtils().getDuelPartyByUUID(players.get(0).get(0)).startTime = System.currentTimeMillis();
							this.main.getUtils().startMultipleDuration(main.getUtils().getDuelPartyByUUID(players.get(0).get(0)));
						}
					}
				});
			});
			if (main.getUtils().getDuelByUUID(players.get(0).get(0)) != null) {
				main.getUtils().getDuelByUUID(players.get(0).get(0)).setState(DuelState.PLAYING);
			}
			if (main.getUtils().getDuelPartyByUUID(players.get(0).get(0)) != null) {
				main.getUtils().getDuelPartyByUUID(players.get(0).get(0)).setState(DuelState.PLAYING);
			}
			this.cancel();
		}
		if (counter > 0) {
			if (players != null) {
				players.forEach(uuids -> {
					uuids.forEach(uuid -> {
						if (Bukkit.getPlayer(uuid) != null) {
							Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.NOTE_PIANO, 1.5f, 1.5f);
							Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED.toString() + counter + "s" + ChatColor.GRAY + "...");		
						}
					});
				});	
			}
		}
	}

}

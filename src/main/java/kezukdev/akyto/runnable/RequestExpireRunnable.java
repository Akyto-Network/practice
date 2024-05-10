package kezukdev.akyto.runnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.request.Request;

public class RequestExpireRunnable extends BukkitRunnable {
	
	private Request request;
	
	public RequestExpireRunnable(final Request request) {
		this.request = request;
	}

	@Override
	public void run() {
		if (request == null || !Practice.getAPI().getManagerHandler().getRequestManager().getRequest().contains(request)) {
			this.cancel();
			return;
		}
		Bukkit.getPlayer(request.getRequester()).sendMessage(ChatColor.RED + "Your duel request for " + (Bukkit.getPlayer(request.getReceiver()) != null ? Bukkit.getPlayer(request.getReceiver()).getName() : Bukkit.getOfflinePlayer(request.getReceiver()).getName()) + " expires");
		if (Bukkit.getPlayer(request.getReceiver()) != null) {
			Bukkit.getPlayer(request.getReceiver()).sendMessage(ChatColor.RED + "The duel request of " + (Bukkit.getPlayer(request.getRequester()) != null ? Bukkit.getPlayer(request.getRequester()).getName() : Bukkit.getOfflinePlayer(request.getRequester()).getName()) + " have expired!");
		}
	}

}

package kezukdev.akyto.runnable;

import akyto.core.Core;
import akyto.core.utils.CoreUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.request.Request;

public class RequestExpireRunnable extends BukkitRunnable {
	
	private final Request request;
	
	public RequestExpireRunnable(final Request request) {
		this.request = request;
	}

	@Override
	public void run() {
		if (request == null || !Practice.getAPI().getManagerHandler().getRequestManager().getRequest().containsKey(request.getRequester())) {
			this.cancel();
			return;
		}
		Bukkit.getPlayer(request.getRequester()).sendMessage(ChatColor.RED + "Your duel request to " + CoreUtils.getName(request.getReceiver()) + " expires");
		if (Bukkit.getPlayer(request.getReceiver()) != null) {
			Bukkit.getPlayer(request.getReceiver()).sendMessage(ChatColor.RED + "The duel request of " + CoreUtils.getName(request.getRequester()) + " have expired!");
		}
		Practice.getAPI().getManagerHandler().getRequestManager().removeRequest(request.getRequester());
	}

}

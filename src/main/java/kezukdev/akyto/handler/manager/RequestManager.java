package kezukdev.akyto.handler.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import kezukdev.akyto.Practice;
import kezukdev.akyto.request.Request;
import kezukdev.akyto.request.Request.RequestType;
import kezukdev.akyto.runnable.RequestExpireRunnable;
import kezukdev.akyto.utils.Utils;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
public class RequestManager {
	
	private final Practice main;
	private final List<Request> request;
	
	public RequestManager(final Practice main) {
		this.main = main;
		this.request = new ArrayList<>();
	}
	
	public void createPullRequest(final UUID sender, final UUID target) {
		this.request.add(new Request(sender, target, null, null, RequestType.DUEL));
		Bukkit.getPlayer(sender).openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[2]);
	}
	
	public void createPartyRequest(final UUID sender, final UUID target) {
		this.request.add(new Request(sender, target, null, null, RequestType.PARTY));
	}
	
	public void sendNotification(final UUID sender, final RequestType type) {
		final Request request = Utils.getRequestByUUID(sender);
		Bukkit.getPlayer(sender).sendMessage(ChatColor.GREEN + "Your " + (type.equals(RequestType.DUEL) ? "duel request into " + ChatColor.stripColor(request.getKit().displayName()) : "party invitation") + " as been sent to " + Bukkit.getPlayer(request.getReceiver()).getName());
		final TextComponent comp = new TextComponent(ChatColor.GREEN + "You have just received a " + (type.equals(RequestType.DUEL) ? "duel request from " : "party invitation from " ) + Bukkit.getPlayer(sender).getName() + (type.equals(RequestType.DUEL) ? " into " + ChatColor.stripColor(request.getKit().displayName()) + ChatColor.GREEN + " on " + ChatColor.YELLOW + request.getArena().getName() : ""));
		comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to accept the " + (type.equals(RequestType.DUEL) ? "duel request." : "party invitation.")).create()));
		comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, (type.equals(RequestType.DUEL) ? "/duel accept " : "/party join ") + Bukkit.getPlayer(sender).getName()));
		Bukkit.getPlayer(request.getReceiver()).spigot().sendMessage(comp);
		if (type.equals(RequestType.DUEL)) {
			new RequestExpireRunnable(request).runTaskLaterAsynchronously(main, 200L);
		}
	}
	
	public void removeRequest(final Request request) {
		this.request.remove(request);
	}
}

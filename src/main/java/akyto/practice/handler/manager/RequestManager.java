package akyto.practice.handler.manager;

import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.request.Request;
import akyto.practice.request.Request.RequestType;
import akyto.practice.runnable.RequestExpireRunnable;
import akyto.practice.utils.Utils;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class RequestManager {
	
	private final Practice main;
	private final HashMap<UUID, Request> request;
	private final HashMap<UUID, RequestExpireRunnable> expireRunnable;
	
	public RequestManager(final Practice main) {
		this.main = main;
		this.request = new HashMap<>();
		this.expireRunnable = new HashMap<>();
	}
	
	public void createPullRequest(final UUID sender, final UUID target) {
		this.request.put(sender, new Request(sender, target, null, null, RequestType.DUEL));
		Bukkit.getPlayer(sender).openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[2]);
	}
	
	public void createPartyRequest(final UUID sender, final UUID target) {
		this.request.put(sender, new Request(sender, target, null, null, RequestType.PARTY));
	}
	
	public void sendNotification(final UUID sender, final RequestType type) {
		final Request request = Utils.getRequestByUUID(sender);
		Bukkit.getPlayer(sender).sendMessage(ChatColor.GREEN + "Your " + (type.equals(RequestType.DUEL) ? "duel request into " + ChatColor.stripColor(request.getKit().displayName()) : "party invitation") + " as been sent to " + Bukkit.getPlayer(request.getReceiver()).getName());

		String[] message = null;
		if (type.equals(RequestType.DUEL)) {
			message = new String[] {
					"",
					ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Duel Request" + ChatColor.GRAY + ":",
					" ",
					ChatColor.GRAY + " • " + ChatColor.WHITE + "Sender: " + ChatColor.RED + CoreUtils.getName(sender) + ChatColor.GRAY + " (" + ChatColor.YELLOW + Bukkit.getPlayer(sender).getPing() + "ms" + ChatColor.GRAY + ")",
					ChatColor.GRAY + " • " + ChatColor.WHITE + "Kit: " + ChatColor.RED + ChatColor.stripColor(request.getKit().displayName()),
					ChatColor.GRAY + " • " + ChatColor.WHITE + "Arena: " + ChatColor.RED + ChatColor.stripColor(request.getArena().getName()),
			};
		}
		else if (type.equals(RequestType.PARTY)) {
			message = new String[] {
					"",
					ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Party Invitation" + ChatColor.GRAY + ":",
					" ",
					ChatColor.GRAY + " • " + ChatColor.WHITE + "Leader: " + ChatColor.RED + CoreUtils.getName(sender) + ChatColor.GRAY + " (" + ChatColor.YELLOW + Bukkit.getPlayer(sender).getPing() + "ms" + ChatColor.GRAY + ")",
					ChatColor.GRAY + " • " + ChatColor.WHITE + "Members: " + ChatColor.RED + Utils.getPartyByUUID(sender).getMembers().size()
			};
		}

		final TextComponent comp = new TextComponent(ChatColor.GRAY + "[" + ChatColor.GREEN + (type.equals(RequestType.DUEL) ? "ACCEPT" : "JOIN") + ChatColor.GRAY + "]");
		comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to accept the " + (type.equals(RequestType.DUEL) ? "duel request." : "party invitation.")).create()));
		comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, (type.equals(RequestType.DUEL) ? "/duel accept " : "/party join ") + CoreUtils.getName(sender)));
		Bukkit.getPlayer(request.getReceiver()).sendMessage(message);
		Bukkit.getPlayer(request.getReceiver()).spigot().sendMessage(comp);
		Bukkit.getPlayer(request.getReceiver()).sendMessage(" ");
		if (type.equals(RequestType.DUEL)) {
			final RequestExpireRunnable runnable = new RequestExpireRunnable(request);
			this.expireRunnable.put(sender, runnable);
			runnable.runTaskLaterAsynchronously(main, 20 * 20L);
		}
	}
	
	public void removeRequest(final UUID sender) {
		this.request.remove(sender);
	}
}

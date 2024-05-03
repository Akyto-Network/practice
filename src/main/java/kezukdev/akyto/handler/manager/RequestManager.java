package kezukdev.akyto.handler.manager;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import kezukdev.akyto.Practice;
import kezukdev.akyto.kit.Kit;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
public class RequestManager {
	
	private Practice main;
	private HashMap<UUID, UUID> startRequest;
	private HashMap<UUID, UUID> partyRequest;
	private HashMap<UUID, RequestEntry> request;
	
	public RequestManager(final Practice main) {
		this.main = main;
		this.startRequest = new HashMap<>();
		this.partyRequest = new HashMap<>();
		this.request = new HashMap<>();
	}
	
	public void createPullRequest(final UUID sender, final UUID target) { 
		this.startRequest.put(sender, target);
		Bukkit.getPlayer(sender).openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[2]);
	}
	
	public void createDuelRequest(final UUID sender, final UUID target, final Kit kit) {
		this.request.put(sender, new RequestEntry(target, kit));
		Bukkit.getPlayer(sender).sendMessage(ChatColor.GREEN + "Your duel request into " + ChatColor.stripColor(kit.displayName()) + " as been sent to " + Bukkit.getPlayer(target).getName());
		final TextComponent comp = new TextComponent(ChatColor.GREEN + "You have just received a duel request from " + Bukkit.getPlayer(sender).getName() + " into " + ChatColor.stripColor(kit.displayName()));
		comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to accept the duel request.").create()));
		comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/duel accept " + Bukkit.getPlayer(sender).getName()));
		Bukkit.getPlayer(target).spigot().sendMessage(comp);
		this.startRequest.remove(sender);
	}
	
	public void createPartyRequest(final UUID sender, final UUID target) {
		this.partyRequest.put(sender, target);
		Bukkit.getPlayer(sender).sendMessage(ChatColor.GREEN + "Your party invitation as been sent to " + Bukkit.getPlayer(target).getName());
		final TextComponent comp = new TextComponent(ChatColor.GREEN + "You have just received a party invitation from " + Bukkit.getPlayer(sender).getName());
		comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to accept the party invitation.").create()));
		comp.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/party join " + Bukkit.getPlayer(sender).getName()));
		Bukkit.getPlayer(target).spigot().sendMessage(comp);
	}
	
	public void removeRequest(final UUID sender) {
        this.partyRequest.remove(sender);
        this.startRequest.remove(sender);
        this.request.remove(sender);
	}
	
	@Getter
	public static class RequestEntry {
		private final UUID requested;
		private final Kit kit;
		
		public RequestEntry(final UUID requested, final Kit kit) {
			this.requested = requested;
			this.kit = kit;
		}
	}

}

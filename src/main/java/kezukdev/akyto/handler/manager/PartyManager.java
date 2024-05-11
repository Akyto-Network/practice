package kezukdev.akyto.handler.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import kezukdev.akyto.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import kezukdev.akyto.utils.chat.ComponentJoiner;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
public class PartyManager {
	
	private final Practice main;
	private final List<PartyEntry> parties;
	
	public PartyManager(final Practice main) {
		this.main = main;
		this.parties = new ArrayList<>();
	}

	public void createParty(final UUID creator) {
		if (!Utils.getProfiles(creator).isInState(ProfileState.FREE)) {
			Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "You cannot do this right now!");
			return;
		}

		if (Utils.getPartyByUUID(creator) != null) {
			Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "You're already at a party!");
			return;
		}

		this.parties.add(new PartyEntry(creator));
		this.main.getServer().getPlayer(creator).sendMessage(ChatColor.GRAY + "You've just created your own party!");
		this.main.getManagerHandler().getItemManager().giveItems(creator, false);
		this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
	}
	
	public void sendPartyInformation(final UUID sender) {
		if (Utils.getPartyByUUID(sender) == null && Bukkit.getPlayer(sender) != null) {
			Bukkit.getPlayer(sender).sendMessage(ChatColor.RED + "You're not in a party!");
			return;
		}

		final PartyEntry party = Utils.getPartyByUUID(sender);
		TextComponent partyComponent = new TextComponent(ChatColor.YELLOW + "Member(s)" + ChatColor.GRAY + ": ");
		final ComponentJoiner joiner = new ComponentJoiner(ChatColor.GRAY + ", ");
		party.getMembers().forEach(member -> {
			if (!member.equals(party.getCreator())) {
				final TextComponent itxt = new TextComponent(Utils.getName(member));
				joiner.add(itxt);	
			}
		});
		partyComponent.addExtra(joiner.toTextComponent());
		Bukkit.getPlayer(sender).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
		Bukkit.getPlayer(sender).sendMessage(ChatColor.DARK_GRAY + "Leader" + ChatColor.GRAY + ": " + ChatColor.WHITE + Utils.getName(party.getCreator()));
		Bukkit.getPlayer(sender).spigot().sendMessage(partyComponent);
		Bukkit.getPlayer(sender).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
	}
	
	public void kickParty(final UUID kicker, final UUID kicked) {
		if(Utils.getPartyByUUID(kicker) == null) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You're not in any party!");
			return;
		}
		if (kicker == kicked) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You can't kick yourself.");
			return;
		}
		if (!Utils.getPartyByUUID(kicker).getMembers().contains(kicked)) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "This player is not in your party!");
			return;
		}
		if (Utils.getPartyByUUID(kicker).getCreator().equals(kicked)) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You can't kick the party creator!");
			return;
		}
		if (!Utils.getPartyByUUID(kicker).getCreator().equals(kicker)) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You're not the party leader!");
			return;
		}
		Utils.getPartyByUUID(kicker).getMembers().remove(kicked);
		Utils.getPartyByUUID(kicker).getMembers().forEach(uuids -> Bukkit.getPlayer(uuids).sendMessage(ChatColor.RED + Bukkit.getPlayer(kicked).getName() + " was kicked from the party"));
		if (Utils.getDuelByUUID(kicked) == null) {
			Utils.sendToSpawn(kicked, true);
		}
		Bukkit.getPlayer(kicked).sendMessage(ChatColor.RED + "You were excluded from the party.");
	}
	
	public void joinParty(final UUID inviter, final UUID invited) {
	    if (Utils.getPartyByUUID(inviter) == null || Bukkit.getPlayer(inviter) == null) {
	        Bukkit.getPlayer(invited).sendMessage(ChatColor.RED + "Target is not connected or him isn't in any party!");
	        return;
	    }
	    final PartyEntry party = Utils.getPartyByUUID(inviter);
	    party.getMembers().add(invited);
	    if (Utils.getDuelByUUID(inviter) != null) {
	    	final Duel duel = Utils.getDuelByUUID(inviter);
	        Utils.getProfiles(invited).setProfileState(ProfileState.SPECTATE);
	        duel.getSpectator().add(invited);
	        Bukkit.getPlayer(invited).teleport(duel.getArena().getPosition().get(0).toBukkitLocation());
	        Bukkit.getOnlinePlayers().forEach(player -> {
	            if (Utils.getProfiles(player.getUniqueId()).isInState(ProfileState.FIGHT, ProfileState.SPECTATE)) {
	                player.hidePlayer(Bukkit.getPlayer(invited));
	                Bukkit.getPlayer(invited).hidePlayer(player);
	            }
	            if (duel.getFirst().contains(player.getUniqueId()) || duel.getSecond().contains(player.getUniqueId())) {
	                Bukkit.getPlayer(invited).showPlayer(player);
	            }
	        });
	    }
	    party.getMembers().forEach(uuid -> {
	        if (Bukkit.getPlayer(uuid) != null) {
	        	Bukkit.getPlayer(uuid).sendMessage(new String[] {
	        			ChatColor.GREEN + Utils.getName(invited) + " joined the party!",
	        			ChatColor.GRAY.toString() + ChatColor.ITALIC +  "We advise you to play " + 
	    	        			(party.getMembers().size() % 2 == 0 ? ChatColor.YELLOW.toString() + ChatColor.ITALIC + "split" + ChatColor.GRAY + " (" + ChatColor.GOLD + (party.getMembers().size() / 2) + ChatColor.RED + "v" + ChatColor.GOLD + (party.getMembers().size() / 2) + ChatColor.GRAY + ")" : ChatColor.YELLOW.toString() + ChatColor.ITALIC + "FFA")});
	        }
	    });
	    this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
	    this.main.getManagerHandler().getItemManager().giveItems(invited, false);
	}
	
	public void leaveParty(final UUID sender) {
		if (Utils.getPartyByUUID(sender) == null && Bukkit.getPlayer(sender) != null) {
			Bukkit.getPlayer(sender).sendMessage(ChatColor.RED + "You're not in a party!");
			return;
		}
		final Profile profile = Utils.getProfiles(sender);
		final PartyEntry party = Utils.getPartyByUUID(sender);
		if (party.getCreator().equals(sender)) {
			party.getMembers().forEach(uuid -> {
				if (Utils.getProfiles(uuid) != null) {
					if (profile.isInState(ProfileState.FREE)) {
						this.main.getManagerHandler().getItemManager().giveItems(uuid, true);
					}
				}
				if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + Utils.getName(sender) + " to dissolve the party!");
			});
			this.parties.remove(party);
			this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
        } else {
			party.getMembers().remove(sender);
			if (profile.isInState(ProfileState.FREE)) {
				this.main.getManagerHandler().getItemManager().giveItems(sender, true);
			}
			if (profile.isInState(ProfileState.SPECTATE)) {
				Utils.sendToSpawn(sender, true);
			}
			party.getMembers().forEach(uuid -> {
				if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + Utils.getName(sender) + " left the party!");
			});
			this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
		}
	}
	
	@Getter @Setter
	public static class PartyEntry {
		
		private UUID creator;
		private Set<UUID> members;
		private boolean open;
		
		public PartyEntry(final UUID creator) {
			this.creator = creator;
			this.open = false;
			this.members = new HashSet<>();
			this.members.add(creator);
		}
	}
}

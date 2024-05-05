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
		if (!this.main.getUtils().getProfiles(creator).getProfileState().equals(ProfileState.FREE)) {
			Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "You cannot do this right now!");
			return;
		}

		if (this.getPartyByUUID(creator) != null) {
			Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "You're already at a party!");
			return;
		}

		this.parties.add(new PartyEntry(creator));
		this.main.getServer().getPlayer(creator).sendMessage(ChatColor.GRAY + "You've just created your own party!");
		this.main.getManagerHandler().getItemManager().giveItems(creator, false);
		this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
	}
	
	public void sendPartyInformation(final UUID sender) {
		if (this.getPartyByUUID(sender) == null && Bukkit.getPlayer(sender) != null) {
			Bukkit.getPlayer(sender).sendMessage(ChatColor.RED + "You're not in a party!");
			return;
		}

		final PartyEntry party = this.getPartyByUUID(sender);
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
		if(this.getPartyByUUID(kicker) == null) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You're not in any party!");
			return;
		}
		if (kicker == kicked) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You can't kick yourself.");
			return;
		}
		if (!this.getPartyByUUID(kicker).getMembers().contains(kicked)) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "This player is not in your party!");
			return;
		}
		if (this.getPartyByUUID(kicker).getCreator().equals(kicked)) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You can't kick the party creator!");
			return;
		}
		if (!this.getPartyByUUID(kicker).getCreator().equals(kicker)) {
			Bukkit.getPlayer(kicker).sendMessage(ChatColor.RED + "You're not the party leader!");
			return;
		}
		this.getPartyByUUID(kicker).getMembers().remove(kicked);
		this.getPartyByUUID(kicker).getMembers().forEach(uuids -> Bukkit.getPlayer(uuids).sendMessage(ChatColor.RED + Bukkit.getPlayer(kicked).getName() + " was kicked from the party"));
		if (this.main.getUtils().getDuelByUUID(kicked) == null) {
			this.main.getUtils().sendToSpawn(kicked, false);
		}
		Bukkit.getPlayer(kicked).sendMessage(ChatColor.RED + "You were excluded from the party.");
	}
	
	public void joinParty(final UUID inviter, final UUID invited) {
	    if (this.getPartyByUUID(inviter) == null || Bukkit.getPlayer(inviter) == null) {
	        Bukkit.getPlayer(invited).sendMessage(ChatColor.RED + "Target is not connected or him isn't in any party!");
	        return;
	    }
	    final PartyEntry party = this.getPartyByUUID(inviter);
	    party.getMembers().add(invited);
	    if (this.main.getUtils().getDuelByUUID(inviter) != null) {
	        this.main.getUtils().getProfiles(invited).setProfileState(ProfileState.SPECTATE);
	        this.main.getUtils().getDuelByUUID(inviter).getSpectator().add(invited);
	        Bukkit.getPlayer(invited).teleport(this.main.getUtils().getDuelByUUID(inviter).getArena().getPosition().get(0).toBukkitLocation());
	        Bukkit.getOnlinePlayers().forEach(player -> {
	            if (this.main.getUtils().getProfiles(player.getUniqueId()).getProfileState().equals(ProfileState.FIGHT) || this.main.getUtils().getProfiles(player.getUniqueId()).getProfileState().equals(ProfileState.SPECTATE)) {
	                player.hidePlayer(Bukkit.getPlayer(invited));
	                Bukkit.getPlayer(invited).hidePlayer(player);
	            }
	            if (this.main.getUtils().getDuelBySpectator(invited).getFirst().contains(player.getUniqueId()) || this.main.getUtils().getDuelBySpectator(invited).getSecond().contains(player.getUniqueId())) {
	                Bukkit.getPlayer(invited).showPlayer(player);
	            }
	        });
	    }
	    party.getMembers().forEach(uuid -> {
	        if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + Utils.getName(invited) + " join the party!");
	    });
	    this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
	    this.main.getManagerHandler().getItemManager().giveItems(invited, false);
	}
	
	public void leaveParty(final UUID sender) {
		if (this.getPartyByUUID(sender) == null && Bukkit.getPlayer(sender) != null) {
			Bukkit.getPlayer(sender).sendMessage(ChatColor.RED + "You're not in a party!");
			return;
		}
		final PartyEntry party = this.getPartyByUUID(sender);
		if (party.getCreator().equals(sender)) {
			party.getMembers().forEach(uuid -> {
				if (this.main.getUtils().getProfiles(uuid) != null) {
					if (this.main.getUtils().getProfiles(sender).getProfileState().equals(ProfileState.FREE)) {
						this.main.getManagerHandler().getItemManager().giveItems(uuid, true);
					}
				}
				if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + Utils.getName(sender) + " to dissolve the party!");
			});
			this.parties.remove(party);
			this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
        } else {
			party.getMembers().remove(sender);
			if (this.main.getUtils().getProfiles(sender) != null) {
				if (this.main.getUtils().getProfiles(sender).getProfileState().equals(ProfileState.FREE)) {
					this.main.getManagerHandler().getItemManager().giveItems(sender, true);
				}
				if (this.main.getUtils().getProfiles(sender).getProfileState().equals(ProfileState.SPECTATE)) {
					this.main.getUtils().sendToSpawn(sender, true);
				}
			}
			party.getMembers().forEach(uuid -> {
				if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + Utils.getName(sender) + " left the party!");
			});
			this.main.getManagerHandler().getInventoryManager().refreshPartyInventory();
		}
	}

	public PartyEntry getPartyByUUID(UUID uuid) {
		return this.parties.stream().filter(party -> party.getMembers().contains(uuid)).findFirst().orElse(null);
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

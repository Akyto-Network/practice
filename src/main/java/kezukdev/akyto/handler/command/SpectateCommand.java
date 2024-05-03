package kezukdev.akyto.handler.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import net.md_5.bungee.api.ChatColor;

public class SpectateCommand implements CommandExecutor {

	private final Practice main;
	
	public SpectateCommand(final Practice main) { this.main = main; }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		if (args.length == 0) {
			if (this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.QUEUE) || this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.EDITOR) || this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.FIGHT)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
				return false;
			}
			this.main.getManagerHandler().getInventoryManager().getSpectateMultipage().open(Bukkit.getPlayer(sender.getName()), 1);
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.GRAY + " * " + ChatColor.RED + "/" + cmd.getName() + " <player>");
			return false;
		}
        if (this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.QUEUE) || this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.EDITOR) || this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.FIGHT)) {
            sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
            return false;
        }
        final UUID targetUUID = Bukkit.getPlayer(args[0]) == null ? Bukkit.getOfflinePlayer(args[0]).getUniqueId() : Bukkit.getPlayer(args[0]).getUniqueId();
        if (this.main.getUtils().getDuelByUUID(targetUUID) == null) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " isn't in fight.");
            return false;
        }
        if (this.main.getUtils().getDuelByUUID(targetUUID).getState().equals(DuelState.FINISHING)) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " duel is finished.");
            return false;
        }
        if (this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.SPECTATE)) {
            final Duel duel = this.main.getUtils().getDuelBySpectator(Bukkit.getPlayer(sender.getName()).getUniqueId());
            duel.getSpectator().remove(Bukkit.getPlayer(sender.getName()).getUniqueId());
            Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + sender.getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.");
                Bukkit.getPlayer(sender.getName()).hidePlayer(Bukkit.getPlayer(uuid));
            }));
        }
        if (!this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.SPECTATE)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.hidePlayer(Bukkit.getPlayer(sender.getName()));
                Bukkit.getPlayer(sender.getName()).hidePlayer(player);
            });
            this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId()).setProfileState(ProfileState.SPECTATE);
            this.main.getManagerHandler().getItemManager().giveItems(Bukkit.getPlayer(sender.getName()).getUniqueId(), false);
        }
        final Duel duel = this.main.getUtils().getDuelByUUID(targetUUID);
        List<List<UUID>> players = Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond()));
        players.forEach(uuids -> uuids.forEach(uuid -> {
            Bukkit.getPlayer(sender.getName()).showPlayer(Bukkit.getPlayer(uuid));
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + sender.getName() + ChatColor.DARK_GRAY + " is now spectating.");
        }));
        duel.getSpectator().add(Bukkit.getPlayer(sender.getName()).getUniqueId());
        final Profile profile = this.main.getUtils().getProfiles(Bukkit.getPlayer(sender.getName()).getUniqueId());
        if (!duel.getSpectator().isEmpty()) {
            duel.getSpectator().forEach(spectator -> {
                if (profile.getSpectateSettings().get(0)) Bukkit.getPlayer(sender.getName()).showPlayer(Bukkit.getPlayer(spectator));
                if (!profile.getSpectateSettings().get(0)) Bukkit.getPlayer(sender.getName()).hidePlayer(Bukkit.getPlayer(spectator));
            });
        }
        if (profile.getSpectateSettings().get(1)) Bukkit.getPlayer(sender.getName()).setFlySpeed(0.1f);
        if (!profile.getSpectateSettings().get(1)) Bukkit.getPlayer(sender.getName()).setFlySpeed(0.25f);
        Bukkit.getPlayer(sender.getName()).teleport(Bukkit.getPlayer(targetUUID).getLocation());
        this.main.getManagerHandler().getInventoryManager().generateChangeSpectateInventory(Bukkit.getPlayer(sender.getName()).getUniqueId());
        return false;
	}

}

package kezukdev.akyto.handler.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import kezukdev.akyto.utils.Utils;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "You must be a player to do that");
            return false;
        }

        final Player playerSender = (Player) sender;
        final Profile profileSender = Utils.getProfiles(playerSender.getUniqueId());
        
		if (args.length == 0) {
			if (profileSender.getProfileState().equals(ProfileState.QUEUE) || profileSender.getProfileState().equals(ProfileState.EDITOR) || profileSender.getProfileState().equals(ProfileState.FIGHT)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
				return false;
			}

			this.main.getManagerHandler().getInventoryManager().getSpectateMultipage().open(playerSender, 1);
			return false;
		}

		if (args.length != 1) {
			sender.sendMessage(ChatColor.GRAY + " * " + ChatColor.RED + "/" + cmd.getName() + " <player>");
			return false;
		}

        if (profileSender.getProfileState().equals(ProfileState.QUEUE) || profileSender.getProfileState().equals(ProfileState.EDITOR) || profileSender.getProfileState().equals(ProfileState.FIGHT)) {
            sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
            return false;
        }

        final UUID targetUUID = Utils.getUUID(args[0]);
        final Duel targetDuel = Utils.getDuelByUUID(targetUUID);
        
        if (targetDuel == null) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " isn't in fight.");
            return false;
        }

        if (targetDuel.getState().equals(DuelState.FINISHING)) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " duel is finished.");
            return false;
        }

        if (profileSender.getProfileState().equals(ProfileState.SPECTATE)) {
            final Duel duel = Utils.getDuelBySpectator(playerSender.getUniqueId());
            duel.getSpectator().remove(playerSender.getUniqueId());
            Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + sender.getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.");
                playerSender.hidePlayer(Bukkit.getPlayer(uuid));
            }));
        }
        targetDuel.getSpectator().add(playerSender.getUniqueId());
        if (!profileSender.getProfileState().equals(ProfileState.SPECTATE)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.hidePlayer(playerSender);
                playerSender.hidePlayer(player);
            });
            profileSender.setProfileState(ProfileState.SPECTATE);
            this.main.getManagerHandler().getItemManager().giveItems(playerSender.getUniqueId(), false);
        }

        List<List<UUID>> players = Arrays.asList(new ArrayList<>(targetDuel.getFirst()), new ArrayList<>(targetDuel.getSecond()));
        players.forEach(uuids -> uuids.forEach(uuid -> {
            playerSender.showPlayer(Bukkit.getPlayer(uuid));
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + sender.getName() + ChatColor.DARK_GRAY + " is now spectating.");
        }));

        if (!targetDuel.getSpectator().isEmpty()) {
            targetDuel.getSpectator().forEach(spectator -> {
                if (profileSender.getSpectateSettings().get(0)) playerSender.showPlayer(Bukkit.getPlayer(spectator));
                if (!profileSender.getSpectateSettings().get(0)) playerSender.hidePlayer(Bukkit.getPlayer(spectator));
            });
        }

        if (profileSender.getSpectateSettings().get(1)) playerSender.setFlySpeed(0.1f);
        if (!profileSender.getSpectateSettings().get(1)) playerSender.setFlySpeed(0.25f);
        playerSender.teleport(Bukkit.getPlayer(targetUUID).getLocation());
        this.main.getManagerHandler().getInventoryManager().generateChangeSpectateInventory(playerSender.getUniqueId());
        return false;
	}
}

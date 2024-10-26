package akyto.practice.handler.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import akyto.core.Core;
import akyto.practice.utils.Utils;
import akyto.practice.utils.match.MatchUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.cache.DuelState;
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
			if (profileSender.isInState(ProfileState.QUEUE, ProfileState.EDITOR, ProfileState.FIGHT)) {
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

        if (profileSender.isInState(ProfileState.QUEUE, ProfileState.EDITOR, ProfileState.FIGHT)) {
            sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
            return false;
        }

        String name = args[0];

        final UUID targetUUID = CoreUtils.getUUID(name);
        final Duel targetDuel = Utils.getDuelByUUID(targetUUID);
        
        if (targetDuel == null) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " isn't in fight.");
            return false;
        }

        if (targetDuel.getState().equals(DuelState.FINISHING)) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " duel is finished.");
            return false;
        }

        Arrays.asList(targetDuel.getFirst(), targetDuel.getSecond()).forEach(players -> players.forEach(uuids -> {
            final Profile profile = Utils.getProfiles(uuids);
            if (profile.getSettings()[4] != 0) {
                if (((Player) sender).getOpenInventory() != null) ((Player) sender).closeInventory();
                sender.sendMessage(ChatColor.RED + "This fight does not accept spectators!");
                return;
            }
        }));

        if (profileSender.isInState(ProfileState.SPECTATE)) {
            final Duel duel = Utils.getDuelBySpectator(playerSender.getUniqueId());
            if (duel.equals(Utils.getDuelByUUID(targetUUID))) {
            	sender.sendMessage(ChatColor.RED + "You are currently spectate this match.");
            	return false;
            }
            duel.getSpectators().remove(playerSender.getUniqueId());
            Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + sender.getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.");
            }));
        }
        targetDuel.getSpectators().add(playerSender.getUniqueId());
        if (!profileSender.isInState(ProfileState.SPECTATE)) {
            profileSender.setProfileState(ProfileState.SPECTATE);
            this.main.getManagerHandler().getItemManager().giveItems(playerSender.getUniqueId(), false);
        }
        MatchUtils.multiArena(playerSender.getUniqueId(), false, true);
        List<List<UUID>> players = Arrays.asList(new ArrayList<>(targetDuel.getFirst()), new ArrayList<>(targetDuel.getSecond()));
        players.forEach(uuids -> uuids.forEach(uuid -> {
            playerSender.showPlayer(Bukkit.getPlayer(uuid));
            Bukkit.getPlayer(uuid).hidePlayer(playerSender);
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + sender.getName() + ChatColor.DARK_GRAY + " is now spectating.");
        }));

        if (!targetDuel.getSpectators().isEmpty()) {
            targetDuel.getSpectators().forEach(spectator -> {
                if (profileSender.getSettings()[8] != 1) playerSender.showPlayer(Bukkit.getPlayer(spectator));
                if (profileSender.getSettings()[8] != 0) playerSender.hidePlayer(Bukkit.getPlayer(spectator));
            });
        }

        if (profileSender.getSettings()[7] != 1) playerSender.setFlySpeed(0.2f);
        if (profileSender.getSettings()[7] != 0) playerSender.setFlySpeed(0.45f);
        playerSender.teleport(Bukkit.getPlayer(targetUUID).getLocation());
        this.main.getManagerHandler().getInventoryManager().generateChangeSpectateInventory(playerSender.getUniqueId());
        return false;
	}
}

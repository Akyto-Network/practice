package kezukdev.akyto.board;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.bizarrealex.aether.scoreboard.Board;
import com.bizarrealex.aether.scoreboard.BoardAdapter;
import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.handler.manager.PartyManager;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;

public class SideBoard implements BoardAdapter {
    private final Practice plugin;
    private final String spacer = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------";

    public SideBoard(final Practice plugin) {  this.plugin = plugin; }

    @Override
    public String getTitle(final Player player) {
        return ChatColor.WHITE.toString() + ChatColor.BOLD + "AKYTO";
    }

    @Override
    public List<String> getScoreboard(final Player player, final Board board, final Set<BoardCooldown> cooldowns) {
        final Profile pm = this.plugin.getManagerHandler().getProfileManager().getProfiles().get(player.getUniqueId());
        if (pm == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            return null;
        }
        if (pm.getSettings().get(0)) {
            if (pm.getProfileState().equals(ProfileState.FREE) || pm.getProfileState().equals(ProfileState.QUEUE) || pm.getProfileState().equals(ProfileState.MOD)) {
                return this.getLobbyBoard(player);
            }
            if (pm.getProfileState().equals(ProfileState.FIGHT)) {
            	return this.getGameBoard(player);
            }
            if (pm.getProfileState().equals(ProfileState.SPECTATE)) {
            	return this.getSpecBoard(player);
            }	
        }
        return null;
    }

    private List<String> getLobbyBoard(final Player player) {
        final List<String> board = new LinkedList<>();
        board.add(spacer);
        board.add(ChatColor.DARK_GRAY + "Online" + ChatColor.GRAY + ": " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size());
        board.add(ChatColor.DARK_GRAY + "In Fight" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.plugin.getDuels().size());
        board.add(ChatColor.DARK_GRAY + "In Queue" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.plugin.getQueue().size());
        if (this.plugin.getUtils().getPartyByUUID(player.getUniqueId()) != null) {
        	board.add(" ");
        	final PartyManager.PartyEntry party = this.plugin.getUtils().getPartyByUUID(player.getUniqueId());
        	board.add(ChatColor.DARK_GRAY + "Leader" + ChatColor.GRAY + ": " + ChatColor.WHITE + Bukkit.getPlayer(party.getCreator()).getName());
        	board.add(ChatColor.DARK_GRAY + "Members" + ChatColor.GRAY + ": " + ChatColor.WHITE + (party.getMembers().size()-1));
        }
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.club");
        board.add(spacer);
        return board;
    }

    private List<String> getGameBoard(final Player player) {
        final List<String> board = new LinkedList<>();
        final Duel duel = this.plugin.getUtils().getDuelByUUID(player.getUniqueId());
        board.add(spacer);
        final UUID opps = this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getFirst().contains(player.getUniqueId()) ? new ArrayList<>(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getSecond()).get(0) : new ArrayList<>(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getFirst()).get(0);
        if (duel.getState().equals(DuelState.PLAYING) || duel.getState().equals(DuelState.STARTING)) {
        	if (duel.getDuelType().equals(DuelType.SINGLE)) {
                board.add(ChatColor.DARK_GRAY + "Opponent" + ChatColor.GRAY + ": " + ChatColor.RESET + (Bukkit.getPlayer(opps) != null ? Bukkit.getPlayer(opps).getName() : Bukkit.getOfflinePlayer(opps).getName()));
	
        	}
        	if (duel.getDuelType().equals(DuelType.FFA)) {
        		board.add(ChatColor.DARK_GRAY + "Alives" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getAlives().size());
        		board.add(" ");
        	}
        	if (duel.getDuelType().equals(DuelType.SPLIT)) {
        		final int green = duel.getFirst().contains(player.getUniqueId()) ? duel.getFirst().size() : duel.getSecond().size();
        		final int red = duel.getSecond().contains(player.getUniqueId()) ? duel.getFirst().size() : duel.getSecond().size();
        		final int greenAlive = duel.getFirst().contains(player.getUniqueId()) ? duel.getFirstAlives().size() : duel.getSecondAlives().size();
        		final int redAlive = duel.getSecond().contains(player.getUniqueId()) ? duel.getFirstAlives().size() : duel.getSecondAlives().size();
        		board.add(ChatColor.GREEN + "▊ " + ChatColor.DARK_GRAY + "alives" + ChatColor.GRAY + ": " + ChatColor.RESET + greenAlive + "/" + green);
        		board.add(ChatColor.RED + "▊ " + ChatColor.DARK_GRAY + "alives" + ChatColor.GRAY + ": " + ChatColor.RESET + redAlive + "/" + red);
        		board.add(" ");
        	}
            board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + this.getFormattedDuration(this.plugin.getUtils().getDuelByUUID(player.getUniqueId())));
            if (this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() != 0L) {
                final double time = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 1000.0D;
                final DecimalFormat df = new DecimalFormat("#.#");
                player.setLevel((int)time);
                final float timeInf = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 16000.0f;
                player.setExp(timeInf);
                board.add(ChatColor.RED + "Enderpearl" + ChatColor.GRAY + ": " + ChatColor.RESET + df.format(time) + "s");
            }
        }
        if (duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
        	board.add(ChatColor.DARK_GRAY + "Winner(s)" + ChatColor.GRAY + ": " + ChatColor.RESET + (Bukkit.getPlayer(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getWinner().get(0)) != null ? Bukkit.getPlayer(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getWinner().get(0)).getName() : Bukkit.getOfflinePlayer(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getWinner().get(0)).getName()));
        }	
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.club");
        board.add(spacer);
        return board;
    }
    
    private List<String> getSpecBoard(final Player player) {
        final List<String> board = new LinkedList<>();
        board.add(spacer);
        Duel duel = this.plugin.getUtils().getDuelBySpectator(player.getUniqueId());
        if (duel == null) duel = this.plugin.getUtils().getDuelByUUID(player.getUniqueId());
        if (duel.getState().equals(DuelState.PLAYING) || duel.getState().equals(DuelState.STARTING)) {
        	if (duel.getDuelType().equals(DuelType.SINGLE)) {
                final String first = Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getFirst()).get(0)).getName();
                final String second = Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getFirst()).get(0)).getName();
                board.add(ChatColor.GREEN + first);
                board.add(ChatColor.DARK_GRAY + "    against");
                board.add(ChatColor.RED + second);	
        	}
        	if (duel.getDuelType().equals(DuelType.FFA)) {
        		board.add(ChatColor.DARK_GRAY + "Alives" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getAlives().size());
        	}
        	if (duel.getDuelType().equals(DuelType.SPLIT)) {
        		board.add(ChatColor.GREEN + "▊ " + ChatColor.DARK_GRAY + "alives" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getFirstAlives().size() + "/" + duel.getFirst().size());
        		board.add(ChatColor.RED + "▊ " + ChatColor.DARK_GRAY + "alives" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getSecondAlives().size() + "/" + duel.getSecond().size());
        	}
            board.add(" ");
            board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + this.getFormattedDuration(duel));
        }
        if (duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
        	board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.RESET + (Bukkit.getPlayer(duel.getWinner().get(0)) != null ? Bukkit.getPlayer(duel.getWinner().get(0)).getName() : Bukkit.getOfflinePlayer(duel.getWinner().get(0)).getName()));
        }
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.club");
        board.add(spacer);
        return board;
    }

    public String getFormattedDuration(final Duel duel) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duel.getDuration());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duel.getDuration()) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
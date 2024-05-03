package kezukdev.akyto.board;

import java.text.DateFormat;
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
import kezukdev.akyto.duel.DuelParty;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.handler.manager.PartyManager;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;

public class SideBoard implements BoardAdapter
{
    private final Practice plugin;
    private final String spacer = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------";
    DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT);

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
        board.add(spacer);
        if (this.plugin.getUtils().getDuelPartyByUUID(player.getUniqueId()) != null) {
        	final DuelParty duel = this.plugin.getUtils().getDuelPartyByUUID(player.getUniqueId());
        	if (duel.getDuelPartyType().equals("ffa")) {
        		board.add(ChatColor.WHITE.toString() + duel.getAlives().size() + ChatColor.GRAY + "/" + ChatColor.RED + (duel.getFirst().size() + duel.getSecond().size()));
        		
        	}
        	if (duel.getDuelPartyType().equals("split") || duel.getDuelPartyType().equals("duel")) {
        		final String firstName = Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getFirst()).get(0)).getName();
        		final String secondName = Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getSecond()).get(0)).getName();
        		board.add(ChatColor.DARK_GRAY + firstName + "'s team" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getFirstAlives().size() + ChatColor.GRAY + "/" + ChatColor.RED + duel.getFirst().size());
        		board.add(ChatColor.DARK_GRAY + secondName + "'s team" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getSecondAlives().size() + ChatColor.GRAY + "/" + ChatColor.RED + duel.getSecond().size());
        	}
    		board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + this.getMultipleFormattedDuration(duel));
            if (this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() != 0L) {
                final double time = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 1000.0D;
                final DecimalFormat df = new DecimalFormat("#.#");
                player.setLevel((int)time);
                final float timeInf = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 16000.0f;
                player.setExp(timeInf);
                board.add(ChatColor.RED + "Enderpearl" + ChatColor.GRAY + ": " + ChatColor.RESET + df.format(time) + "s");
            }	
        }
        if (this.plugin.getUtils().getDuelByUUID(player.getUniqueId()) != null) {
            final UUID opps = this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getFirst().contains(player.getUniqueId()) ? new ArrayList<>(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getSecond()).get(0) : new ArrayList<>(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getFirst()).get(0);
            if (this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getState().equals(DuelState.PLAYING) || this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getState().equals(DuelState.STARTING)) {
                board.add(ChatColor.DARK_GRAY + "Opponent" + ChatColor.GRAY + ": " + ChatColor.RESET + (Bukkit.getPlayer(opps) != null ? Bukkit.getPlayer(opps).getName() : Bukkit.getOfflinePlayer(opps).getName()));
                board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + this.getSingleFormattedDuration(this.plugin.getUtils().getDuelByUUID(player.getUniqueId())));
                if (this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() != 0L) {
                    final double time = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 1000.0D;
                    final DecimalFormat df = new DecimalFormat("#.#");
                    player.setLevel((int)time);
                    final float timeInf = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 16000.0f;
                    player.setExp(timeInf);
                    board.add(ChatColor.RED + "Enderpearl" + ChatColor.GRAY + ": " + ChatColor.RESET + df.format(time) + "s");
                }	
            }
            if (this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getState().equals(DuelState.FINISHING)) {
            	board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.RESET + (Bukkit.getPlayer(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getWinner()) != null ? Bukkit.getPlayer(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getWinner()).getName() : Bukkit.getOfflinePlayer(this.plugin.getUtils().getDuelByUUID(player.getUniqueId()).getWinner()).getName()));
            }	
        }
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.club");
        board.add(spacer);
        return board;
    }
    
    private List<String> getSpecBoard(final Player player) {
        final List<String> board = new LinkedList<>();
        board.add(spacer);
        if (this.plugin.getUtils().getDuelPartyBySpectator(player.getUniqueId()) != null) {
        	final DuelParty duel = this.plugin.getUtils().getDuelPartyBySpectator(player.getUniqueId());
        	if (duel.getDuelPartyType().equals("ffa")) {
        		board.add(ChatColor.WHITE.toString() + duel.getAlives().size() + ChatColor.GRAY + "/" + ChatColor.RED + (duel.getFirst().size() + duel.getSecond().size()));
        		
        	}
        	if (duel.getDuelPartyType().equals("split") || duel.getDuelPartyType().equals("duel")) {
        		final String firstName = Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getFirst()).get(0)).getName();
        		final String secondName = Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getSecond()).get(0)).getName();
        		board.add(ChatColor.DARK_GRAY + firstName + "'s team" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getFirstAlives().size() + ChatColor.GRAY + "/" + ChatColor.RED + duel.getFirst().size());
        		board.add(ChatColor.DARK_GRAY + secondName + "'s team" + ChatColor.GRAY + ": " + ChatColor.RESET + duel.getSecondAlives().size() + ChatColor.GRAY + "/" + ChatColor.RED + duel.getSecond().size());
        	}
    		board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + this.getMultipleFormattedDuration(duel));
        }
        if (this.plugin.getUtils().getDuelBySpectator(player.getUniqueId()) != null) {
            final Duel duel = this.plugin.getUtils().getDuelBySpectator(player.getUniqueId());
            final String first = Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getFirst()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getFirst()).get(0)).getName();
            final String second = Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)) != null ? Bukkit.getPlayer(new ArrayList<>(duel.getSecond()).get(0)).getName() : Bukkit.getOfflinePlayer(new ArrayList<>(duel.getFirst()).get(0)).getName();
            if (duel.getState().equals(DuelState.PLAYING) || duel.getState().equals(DuelState.STARTING)) {
                board.add(ChatColor.GREEN + first);
                board.add(ChatColor.DARK_GRAY + "    against");
                board.add(ChatColor.RED + second);
                board.add(" ");
                board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + this.getSingleFormattedDuration(duel));
            }
            if (duel.getState().equals(DuelState.FINISHING)) {
            	board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.RESET + (Bukkit.getPlayer(duel.getWinner()) != null ? Bukkit.getPlayer(duel.getWinner()).getName() : Bukkit.getOfflinePlayer(duel.getWinner()).getName()));
            }	
        }
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.club");
        board.add(spacer);
        return board;
    }

    public String getSingleFormattedDuration(final Duel duel) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duel.getDuration());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duel.getDuration()) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public String getMultipleFormattedDuration(final DuelParty duel) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duel.getDuration());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duel.getDuration()) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
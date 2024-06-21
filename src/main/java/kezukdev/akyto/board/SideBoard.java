package kezukdev.akyto.board;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.utils.Utils;
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
    private final String title =  ChatColor.WHITE.toString() + ChatColor.BOLD + "AKYTO";

    public SideBoard(final Practice plugin) {  this.plugin = plugin; }

    @Override
    public String getTitle(final Player player) {
        return title;
    }

    @Override
    public List<String> getScoreboard(final Player player, final Board board, final Set<BoardCooldown> cooldowns) {
        final Profile pm = this.plugin.getManagerHandler().getProfileManager().getProfiles().get(player.getUniqueId());

        if (pm == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            return null;
        }

        // If player enabled scoreboard
        if (pm.getSettings().get(0)) {
            if (pm.isInState(ProfileState.FREE, ProfileState.QUEUE)) {
                return this.getLobbyBoard(player);
            }
            if (pm.isInState(ProfileState.FIGHT)) {
            	return this.getGameBoard(player);
            }
            if (pm.isInState(ProfileState.SPECTATE)) {
            	return this.getSpecBoard(player);
            }	
        }
        return null;
    }

    private List<String> getLobbyBoard(final Player player) {
        final List<String> board = new LinkedList<>();

        board.add(spacer);
        board.add(ChatColor.DARK_GRAY + "Online" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.plugin.getServer().getOnlinePlayers().size());
        final int inFight = this.plugin.getDuels().size()*2;
        board.add(ChatColor.DARK_GRAY + "In Fight" + ChatColor.GRAY + ": " + ChatColor.WHITE + inFight);
        board.add(ChatColor.DARK_GRAY + "In Queue" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.plugin.getQueue().size());

        final PartyManager.PartyEntry playerParty = Utils.getPartyByUUID(player.getUniqueId());

        if (playerParty != null) {
        	board.add(" ");
        	board.add(ChatColor.DARK_GRAY + "Leader" + ChatColor.GRAY + ": " + ChatColor.WHITE + Utils.getName(playerParty.getCreator()));
        	board.add(ChatColor.DARK_GRAY + "Members" + ChatColor.GRAY + ": " + ChatColor.WHITE + (playerParty.getMembers().size() - 1));
        }

        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.club");
        board.add(spacer);
        return board;
    }

    private List<String> getGameBoard(final Player player) {
        final List<String> board = new LinkedList<>();
        final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
        final UUID opps = duel.getFirst().contains(player.getUniqueId()) ? new ArrayList<>(duel.getSecond()).get(0) : new ArrayList<>(duel.getFirst()).get(0);

        board.add(spacer);

        if (duel.getState().equals(DuelState.PLAYING) || duel.getState().equals(DuelState.STARTING)) {
        	if (duel.getDuelType().equals(DuelType.SINGLE)) {
                board.add(ChatColor.DARK_GRAY + "Opponent" + ChatColor.GRAY + ": " + ChatColor.RESET + Utils.getName(opps));
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

            board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + this.getFormattedDuration(duel));

            DuelStatistics statistics = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId());
            if (statistics != null && statistics.getEnderPearlCooldown() != 0L) {
                final double time = statistics.getEnderPearlCooldown() / 1000.0D;
                final DecimalFormat df = new DecimalFormat("#.#");
                board.add(ChatColor.RED + "Enderpearl" + ChatColor.GRAY + ": " + ChatColor.RESET + df.format(time) + "s");
        		final float timeInf = Practice.getAPI().getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 16000.0f;
        		player.setExp(timeInf);
        		player.setLevel((int) time);
            }
        }

        if (duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
        	board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.RESET + Utils.getName(duel.getWinner().get(0)));
        }

        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.club");
        board.add(spacer);
        return board;
    }
    
    private List<String> getSpecBoard(final Player player) {
        final List<String> board = new LinkedList<>();
        board.add(spacer);
        Duel duel = Utils.getDuelBySpectator(player.getUniqueId());
        if (duel == null) duel = Utils.getDuelByUUID(player.getUniqueId());
        if (duel.getState().equals(DuelState.PLAYING) || duel.getState().equals(DuelState.STARTING)) {
        	if (duel.getDuelType().equals(DuelType.SINGLE)) {
                final String first = Utils.getName(duel.getFirst().iterator().next());
                final String second = Utils.getName(duel.getSecond().iterator().next());
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
        	board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.RESET + Utils.getName(duel.getWinner().get(0)));
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
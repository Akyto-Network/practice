package akyto.practice.board;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.event.EventState;
import akyto.practice.handler.manager.EventManager;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.utils.chat.MessageUtils;
import akyto.practice.utils.match.DurationUtils;
import akyto.practice.utils.match.MatchUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.bizarrealex.aether.scoreboard.Board;
import com.bizarrealex.aether.scoreboard.BoardAdapter;
import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.handler.manager.PartyManager;
import akyto.practice.utils.Utils;

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
        final Profile pm = Core.API.getManagerHandler().getProfileManager().getProfiles().get(player.getUniqueId());

        if (pm == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            return null;
        }

        // If player enabled scoreboard
        if (pm.getSettings()[0] == 0) {
            if (pm.isInState(ProfileState.FREE, ProfileState.QUEUE, ProfileState.MOD)) {
                return this.getLobbyBoard(player);
            }
            if (pm.isInState(ProfileState.FIGHT)) {
            	return this.getGameBoard(player);
            }
            if (pm.isInState(ProfileState.SPECTATE)) {
            	return this.getSpecBoard(player);
            }
            if (pm.isInState(ProfileState.EVENT)) {
                return this.getEventBoard(player);
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
        	board.add(ChatColor.DARK_GRAY + "Leader" + ChatColor.GRAY + ": " + ChatColor.WHITE + CoreUtils.getName(playerParty.getCreator()));
        	board.add(ChatColor.DARK_GRAY + "Members" + ChatColor.GRAY + ": " + ChatColor.WHITE + (playerParty.getMembers().size() - 1));
        }

        final TournamentEntry tournamentEntry = Utils.getTournamentByPlayerUUID(player.getUniqueId());

        if (tournamentEntry != null) {
            board.add(" ");
            board.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Tournament:");
            board.add(ChatColor.DARK_GRAY + " Kit" + ChatColor.GRAY + ": " + ChatColor.WHITE + ChatColor.stripColor(tournamentEntry.getKit().displayName()));
            board.add(ChatColor.DARK_GRAY + " Creator" + ChatColor.GRAY + ": " + ChatColor.WHITE + CoreUtils.getName(tournamentEntry.getCreator()));
            board.add(ChatColor.DARK_GRAY + " Size" + ChatColor.GRAY + ": " + ChatColor.WHITE + tournamentEntry.getTeamSize() + "v" + tournamentEntry.getTeamSize());
            board.add(ChatColor.DARK_GRAY + " In" + ChatColor.GRAY + ": " + ChatColor.WHITE + tournamentEntry.getParticipants().size());
        }

        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.net");
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
                board.add(ChatColor.DARK_GRAY + "Opponent" + ChatColor.GRAY + ": " + ChatColor.RESET + CoreUtils.getName(opps));
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

            board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + DurationUtils.getFormattedDuration(duel));
            board.add(" ");
            board.add(ChatColor.DARK_GRAY + "Ping" + ChatColor.GRAY + ": " + ChatColor.WHITE + player.getPing() + "ms");
            DuelStatistics statistics = this.plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId());
            if (statistics != null && statistics.getEnderPearlCooldown() != 0L) {
                final double time = statistics.getEnderPearlCooldown() / 1000.0D;
                final DecimalFormat df = new DecimalFormat("#.#");
                board.add(ChatColor.RED + "Enderpearl" + ChatColor.GRAY + ": " + ChatColor.RESET + df.format(time) + "s");
        		final float timeInf = Practice.API.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 16000.0f;
        		player.setExp(timeInf);
        		player.setLevel((int) time);
            }
        }

        if (duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
        	board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.RESET + CoreUtils.getName(duel.getWinner().getFirst()));
        }
        final TournamentEntry tournamentEntry = Utils.getTournamentByPlayerUUID(player.getUniqueId());

        if (tournamentEntry != null) board.add(ChatColor.DARK_GRAY + "Stage" + ChatColor.GRAY + ": " + ChatColor.WHITE + MessageUtils.determineTournamentStage(tournamentEntry));
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.net");
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
                final String first = CoreUtils.getName(duel.getFirst().iterator().next());
                final String second = CoreUtils.getName(duel.getSecond().iterator().next());
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
            board.add(ChatColor.DARK_GRAY + "Duration" + ChatColor.GRAY + ": " + ChatColor.RESET + DurationUtils.getFormattedDuration(duel));
        }
        if (duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
        	board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.RESET + CoreUtils.getName(duel.getWinner().get(0)));
        }
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.net");
        board.add(spacer);
        return board;
    }

    private List<String> getEventBoard(final Player player) {
        final List<String> board = new LinkedList<>();
        final EventManager eventManager = this.plugin.getManagerHandler().getEventManager();
        board.add(spacer);
        board.add(ChatColor.DARK_GRAY + eventManager.getType().toLowerCase().toUpperCase() + " Event");
        board.add(" ");
        if (eventManager.getEventState().equals(EventState.WAITING)) {
            board.add(ChatColor.DARK_GRAY + "Players" + ChatColor.GRAY + " : " + ChatColor.WHITE + eventManager.getMembers().size());
        }
        if (eventManager.getType().equalsIgnoreCase("sumo")) {
            if (eventManager.getEventState().equals(EventState.STARTING) || eventManager.getEventState().equals(EventState.FIGHT)) {
                final Player player1 = Bukkit.getPlayer(eventManager.getPlayersSelected().get(0));
                final Player player2 = Bukkit.getPlayer(eventManager.getPlayersSelected().get(1));
                board.add(ChatColor.GREEN + player1.getName());
                board.add(ChatColor.DARK_GRAY + "    against");
                board.add(ChatColor.RED + player2.getName());
            }
            if (eventManager.getEventState().equals(EventState.ENDING)) {
                AtomicReference<UUID> winner = new AtomicReference<>();
                eventManager.getPlayersSelected().forEach(selected -> {
                    if (eventManager.getAlives().contains(selected)) {
                        winner.set(selected);
                    }
                });
                board.add(ChatColor.DARK_GRAY + "Winner" + ChatColor.GRAY + ": " + ChatColor.WHITE + Bukkit.getPlayer(winner.get()).getName());
            }
        }
        if (eventManager.getType().equalsIgnoreCase("oitc")) {
            if (eventManager.getEventState().equals(EventState.FIGHT)) {
                List<Profile> memberProfiles = eventManager.getMembers().stream()
                        .map(memberUUID -> Utils.getProfiles(memberUUID))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                memberProfiles.sort((p1, p2) -> Integer.compare(p2.getKill(), p1.getKill()));
                int limit = eventManager.getMembers().size();
                if (eventManager.getMembers().size() >= 5) limit = 5;
                for (int i = 0; i < limit; i++) {
                    Profile profile = memberProfiles.get(i);
                    Player member = Bukkit.getPlayer(profile.getUuid());
                    if (member != null) {
                        board.add(ChatColor.WHITE + member.getName() + ": " + ChatColor.GREEN + profile.getKill() + " kills");
                    }
                }
            }
        }
        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.net");
        board.add(spacer);
        return board;
    }
}
package akyto.practice.board;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.event.EventState;
import akyto.practice.handler.manager.EventManager;
import akyto.practice.handler.manager.PartyManager;
import akyto.practice.handler.manager.ProfileManager;
import akyto.practice.handler.manager.QueueManager;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.utils.Utils;
import akyto.practice.utils.chat.MessageUtils;
import akyto.practice.utils.match.DurationUtils;
import com.bizarrealex.aether.scoreboard.Board;
import com.bizarrealex.aether.scoreboard.BoardAdapter;
import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;
import com.bizarrealex.aether.scoreboard.cooldown.BoardFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SideBoard implements BoardAdapter {
    private final Practice plugin;
    private final String spacer = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------";
    private final String title =  ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Practice";

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
        if (pm.getSettings()[0] != 2) {
            if (pm.isInState(ProfileState.FREE, ProfileState.QUEUE, ProfileState.MOD)) {
                return this.getLobbyBoard(player);
            }
            if (pm.isInState(ProfileState.FIGHT)) {
            	return this.getGameBoard(player, cooldowns);
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
        board.add(ChatColor.WHITE + "Online: " + ChatColor.RED + this.plugin.getServer().getOnlinePlayers().size());
        final int inFight = this.plugin.getDuels().size()*2;
        board.add(ChatColor.WHITE + "Fighting: " + ChatColor.RED + inFight);
        board.add(ChatColor.WHITE + "Queueing: " + ChatColor.RED + this.plugin.getQueue().size());

        final Profile profile = Utils.getProfiles(player.getUniqueId());
        if (profile.isInState(ProfileState.QUEUE)) {
            board.add(" ");
            final QueueManager.QueueEntry entry = plugin.getQueue().get(player.getUniqueId());
            board.add(ChatColor.WHITE + " Kit" + ChatColor.GRAY + ": " + ChatColor.RED + ChatColor.stripColor(entry.getKit().displayName()));
            board.add(ChatColor.WHITE + " Elapsed" + ChatColor.GRAY + ": " + ChatColor.RED + DurationUtils.getFormattedDuration(entry.getDuration()));
            if (entry.isRanked()) {
                board.add(ChatColor.WHITE + " Elo" + ChatColor.GRAY + ": " + ChatColor.RED + profile.getStats().get(2)[entry.getKit().id()]);
            }
        }
        final PartyManager.PartyEntry playerParty = Utils.getPartyByUUID(player.getUniqueId());

        if (playerParty != null) {
        	board.add(" ");
        	board.add(ChatColor.WHITE + " Leader: " + ChatColor.RED + CoreUtils.getName(playerParty.getCreator()));
        	board.add(ChatColor.WHITE + " Members: " + ChatColor.RED + (playerParty.getMembers().size() - 1));
        }

        final TournamentEntry tournamentEntry = Utils.getTournamentByPlayerUUID(player.getUniqueId());

        if (tournamentEntry != null) {
            board.add(" ");
            board.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Tournament:");
            board.add(ChatColor.WHITE + " Kit: " + ChatColor.RED + ChatColor.stripColor(tournamentEntry.getKit().displayName()));
            board.add(ChatColor.WHITE + " Creator: " + ChatColor.RED + CoreUtils.getName(tournamentEntry.getCreator()));
            board.add(ChatColor.WHITE + " Size: " + ChatColor.RED + tournamentEntry.getTeamSize() + "v" + tournamentEntry.getTeamSize());
            board.add(ChatColor.WHITE + " In: " + ChatColor.RED + tournamentEntry.getParticipants().size());
        }

        board.add(" ");
        board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "akyto.net");
        board.add(spacer);
        return board;
    }

    private List<String> getGameBoard(final Player player, final Set<BoardCooldown> cooldowns) {
        final List<String> board = new LinkedList<>();
        final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
        final UUID opps = duel.getFirst().contains(player.getUniqueId()) ? new ArrayList<>(duel.getSecond()).getFirst() : new ArrayList<>(duel.getFirst()).getFirst();

        board.add(spacer);

        if (duel.getState().equals(DuelState.PLAYING) || duel.getState().equals(DuelState.STARTING)) {
        	if (duel.getDuelType().equals(DuelType.SINGLE)) {
                board.add(ChatColor.WHITE + " Opponent: " + ChatColor.RED + CoreUtils.getName(opps));
                if (duel.getKit().name().equals("bedfight")) {
                    board.add(" ");
                    final DuelStatistics statisticsPlayer = plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId());
                    board.add(MessageUtils.getColorTeam(duel, player.getUniqueId()) + " " + (statisticsPlayer.isBedBroke() ? ChatColor.RED + "✖" : ChatColor.GREEN + "✔"));
                    final String oppsColor = MessageUtils.getColorTeam(duel, player.getUniqueId()).equals(ChatColor.BLUE + "Blue") ? ChatColor.RED + "Red" : ChatColor.BLUE + "Blue";
                    final DuelStatistics statisticsOpps = plugin.getManagerHandler().getProfileManager().getDuelStatistics().get(opps);
                    board.add(oppsColor + " " + (statisticsOpps.isBedBroke() ? ChatColor.RED + "✖" : ChatColor.GREEN + "✔"));
                    board.add(" ");
                    board.add(ChatColor.WHITE + " Team: " + MessageUtils.getColorTeam(duel, player.getUniqueId()));
                }
                if (duel.getKit().name().equals("boxing")) {
                    final ProfileManager profileManager = plugin.getManagerHandler().getProfileManager();
                    board.add(" ");
                    board.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Hits:");
                    board.add(ChatColor.WHITE + " Their: " + ChatColor.RED + profileManager.getDuelStatistics().get(opps).getHits());
                    board.add(ChatColor.WHITE + " Your: " + ChatColor.RED + profileManager.getDuelStatistics().get(player.getUniqueId()).getHits());
                    board.add(" ");
                }
        	}

        	if (duel.getDuelType().equals(DuelType.FFA)) {
        		board.add(ChatColor.WHITE + " Alives: " + ChatColor.RED + duel.getAlives().size());
        		board.add(" ");
        	}

        	if (duel.getDuelType().equals(DuelType.SPLIT)) {
                board.add(ChatColor.DARK_RED + " Your Team" + ChatColor.GRAY + ":");
                if (duel.getFirst().contains(player.getUniqueId())) {
                    for (UUID playerId : duel.getFirst()) {
                        Player players = Bukkit.getPlayer(playerId);
                        if (players != null) {
                            String playerName = players.getName();
                            if (!duel.getFirstAlives().contains(playerId)) {
                                playerName = ChatColor.STRIKETHROUGH + playerName;
                            }
                            board.add(ChatColor.GREEN + " " + playerName);
                        }
                    }
                    board.add(ChatColor.RED + " Opponents" + ChatColor.GRAY + ":");
                    for (UUID playerId : duel.getSecond()) {
                        Player players = Bukkit.getPlayer(playerId);
                        if (players != null) {
                            String playerName = players.getName();
                            if (!duel.getSecondAlives().contains(playerId)) {
                                playerName = ChatColor.STRIKETHROUGH + playerName;
                            }
                            board.add(ChatColor.RED + " " + playerName);
                        }
                    }
                }
                else {
                    for (UUID playerId : duel.getSecond()) {
                        Player players = Bukkit.getPlayer(playerId);
                        if (players != null) {
                            String playerName = players.getName();
                            if (!duel.getSecondAlives().contains(playerId)) {
                                playerName = ChatColor.STRIKETHROUGH + playerName;
                            }
                            board.add(ChatColor.GREEN + " " + playerName);
                        }
                    }
                    board.add(ChatColor.RED + " Opponents" + ChatColor.GRAY + ":");
                    for (UUID playerId : duel.getFirst()) {
                        Player players = Bukkit.getPlayer(playerId);
                        if (players != null) {
                            String playerName = players.getName();
                            if (!duel.getFirstAlives().contains(playerId)) {
                                playerName = ChatColor.STRIKETHROUGH + playerName;
                            }
                            board.add(ChatColor.RED + " " + playerName);
                        }
                    }
                }

        		board.add(" ");
        	}
            if (!duel.getDuelType().equals(DuelType.SPLIT)) {
                board.add(ChatColor.WHITE + " Duration: " + ChatColor.RED + DurationUtils.getFormattedDuration(duel.getDuration()));
            }
            if (Utils.getProfiles(player.getUniqueId()).getSettings()[0] == 1 && duel.getDuelType().equals(DuelType.SINGLE)) {
                board.add(" ");
                board.add(ChatColor.WHITE + " His Ping: " + ChatColor.RED + Bukkit.getPlayer(opps).getPing() + "ms");
                board.add(ChatColor.WHITE + " Your Ping: " + ChatColor.RED + player.getPing() + "ms");
            }
            for (final BoardCooldown cooldown : cooldowns) {
                if (cooldown.getId().equals("enderpearl")) {
                    board.add(ChatColor.DARK_RED + " Enderpearl" + ChatColor.GRAY + ": " + ChatColor.WHITE + cooldown.getFormattedString(BoardFormat.SECONDS) + "'s");
                }
            }
        }

        if (duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
        	board.add(ChatColor.WHITE + " Winner: " + ChatColor.RED + CoreUtils.getName(duel.getWinner().getFirst()));
        }
        final TournamentEntry tournamentEntry = Utils.getTournamentByPlayerUUID(player.getUniqueId());

        if (tournamentEntry != null) board.add(ChatColor.WHITE + " Stage: " + ChatColor.RED + MessageUtils.determineTournamentStage(tournamentEntry));
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
                board.add(ChatColor.WHITE.toString() + ChatColor.ITALIC + "    against");
                board.add(ChatColor.RED + second);	
        	}
        	if (duel.getDuelType().equals(DuelType.FFA)) {
        		board.add(ChatColor.WHITE + " Alives: " + ChatColor.RED + duel.getAlives().size());
        	}
        	if (duel.getDuelType().equals(DuelType.SPLIT)) {
                board.add(ChatColor.GREEN + " First Team");
                for (UUID playerId : duel.getFirst()) {
                    Player players = Bukkit.getPlayer(playerId);
                    if (players != null) {
                        String playerName = players.getName();
                        if (!duel.getFirstAlives().contains(playerId)) {
                            playerName = ChatColor.STRIKETHROUGH + playerName;
                        }
                        board.add(ChatColor.GREEN + " " + playerName);
                    }
                }
                board.add(ChatColor.RED + " Second Team");
                for (UUID playerId : duel.getSecond()) {
                    Player players = Bukkit.getPlayer(playerId);
                    if (players != null) {
                        String playerName = players.getName();
                        if (!duel.getSecondAlives().contains(playerId)) {
                            playerName = ChatColor.STRIKETHROUGH + playerName;
                        }
                        board.add(ChatColor.RED + " " + playerName);
                    }
                }
        	}
            board.add(" ");
            board.add(ChatColor.WHITE + " Duration: " + ChatColor.RED + DurationUtils.getFormattedDuration(duel.getDuration()));
        }
        if (duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
        	board.add(ChatColor.WHITE + " Winner: " + ChatColor.RED + CoreUtils.getName(duel.getWinner().getFirst()));
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
                        .map(Utils::getProfiles)
                        .filter(Objects::nonNull).toList();
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
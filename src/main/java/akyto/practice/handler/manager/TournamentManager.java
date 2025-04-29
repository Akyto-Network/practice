package akyto.practice.handler.manager;

import akyto.core.profile.Profile;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.kit.Kit;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.tournament.TournamentState;
import akyto.practice.utils.Utils;
import akyto.core.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TournamentManager implements Listener {

    private final Practice main;
    private final Map<UUID, TournamentEntry> tournaments;
    private Map<Duel, CompletableFuture<List<UUID>>> activeDuels;
    private final int countdownDuration = 60;

    public TournamentManager(Practice main) {
        this.main = main;
        this.tournaments = new HashMap<>();
        this.activeDuels = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    public UUID createTournament(Player creator, Kit kit, int teamSize) {
        if (!Utils.getProfiles(creator.getUniqueId()).isInState(ProfileState.FREE)) {
            creator.sendMessage(ChatColor.RED + "You must be in FREE state to create a tournament!");
            return null;
        }
        UUID tournamentId = UUID.randomUUID();
        TournamentEntry entry = new TournamentEntry(creator.getUniqueId(), tournamentId, kit, teamSize);
        tournaments.put(tournamentId, entry);
        final Profile profile = Utils.getProfiles(creator.getUniqueId());
        profile.setProfileState(ProfileState.TOURNAMENT);
        return tournamentId;
    }

    private void startCountdown(TournamentEntry entry) {
        new BukkitRunnable() {
            int timeLeft = countdownDuration;

            @Override
            public void run() {
                if (timeLeft == 0) {
                    startTournament(entry);
                    cancel();
                    return;
                }

                if (timeLeft <= 10 || timeLeft % 10 == 0) {
                    for (UUID participant : entry.getParticipants()) {
                        Player player = Bukkit.getPlayer(participant);
                        if (player != null) {
                            player.sendMessage(ChatColor.GOLD + "Tournament starting in " + timeLeft + " seconds!");
                        }
                    }
                }

                timeLeft--;
            }
        }.runTaskTimer(main, 0L, 20L); // Run every second
    }

    private void startTournament(TournamentEntry entry) {
        List<UUID> participants = entry.getParticipants();
        int teamSize = entry.getTeamSize();

        if (participants.size() < 2 * teamSize) {
            for (UUID participant : participants) {
                Player player = Bukkit.getPlayer(participant);
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "Not enough players to start the tournament!");
                }
            }
            return;
        }
        runTournamentRound(entry);
    }

    private void runTournamentRound(TournamentEntry entry) {
        List<UUID> participants = entry.getParticipants();
        int teamSize = entry.getTeamSize();

        if (participants.size() <= teamSize) {
            for (UUID participant : participants) {
                Player player = Bukkit.getPlayer(participant);
                if (player != null) {
                    player.sendMessage(ChatColor.GOLD + "Tournament ended! Winner: " + Bukkit.getPlayer(participants.get(0)).getName());
                }
            }
            return;
        }

        Collections.shuffle(participants);
        List<CompletableFuture<List<UUID>>> roundFutures = new ArrayList<>();

        for (int i = 0; i < participants.size(); i += 2 * teamSize) {
            List<UUID> team1 = new ArrayList<>(participants.subList(i, Math.min(i + teamSize, participants.size())));
            List<UUID> team2 = new ArrayList<>(participants.subList(Math.min(i + teamSize, participants.size()), Math.min(i + 2 * teamSize, participants.size())));

            if (team1.isEmpty() || team2.isEmpty()) continue;
            entry.setTournamentState(TournamentState.FIGHT);
            Duel duel = new Duel(main, new HashSet<>(team1), new HashSet<>(team2), false, entry.getKit(), Duel.DuelType.SPLIT, null);
            CompletableFuture<List<UUID>> future = new CompletableFuture<>();
            activeDuels.put(duel, future);
            roundFutures.add(future);

            determineWinnerAsync(duel, future);
        }

        CompletableFuture.allOf(roundFutures.toArray(new CompletableFuture[0])).thenRun(() -> processRoundResults(entry));
    }

    private void determineWinnerAsync(Duel duel, CompletableFuture<List<UUID>> future) {
        Bukkit.getScheduler().runTaskLater(main, () -> {
            List<UUID> winner = determineWinner(duel);
            future.complete(winner);
        }, 20L);
    }

    private List<UUID> determineWinner(Duel duel) {
        return new ArrayList<>(duel.getFirst());
    }

    private void processRoundResults(TournamentEntry entry) {
        List<UUID> winners = new ArrayList<>();
        for (Duel duel : activeDuels.keySet()) {
            try {
                List<UUID> winner = activeDuels.get(duel).get();
                winners.addAll(winner);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        activeDuels.clear();
        entry.getParticipants().retainAll(winners);
        runTournamentRound(entry);
    }

    public Collection<TournamentEntry> getAllTournaments() {
        return tournaments.values();
    }

    public TournamentEntry getTournamentEntry(UUID tournamentId) {
        return tournaments.get(tournamentId);
    }

    public boolean canJoinTournament(Player player, UUID tournamentId) {
        if (!Utils.getProfiles(player.getUniqueId()).isInState(ProfileState.FREE)) {
            player.sendMessage(ChatColor.RED + "You must be in FREE state to join a tournament!");
            return false;
        }

        TournamentEntry entry = tournaments.get(tournamentId);
        if (entry == null) {
            return false;
        }

        PartyManager.PartyEntry party = Utils.getPartyByUUID(player.getUniqueId());
        int teamSize = entry.getTeamSize();

        if (teamSize == 1) {
            return true;
        }

        if (party != null && party.getMembers().size() == teamSize) {
            return true;
        }

        return false;
    }

    public void joinTournament(Player player, UUID tournamentId) {
        if (!canJoinTournament(player, tournamentId)) {
            return;
        }
        TournamentEntry entry = tournaments.get(tournamentId);
        if (entry != null) {
            int teamSize = entry.getTeamSize();
            PartyManager.PartyEntry party = Utils.getPartyByUUID(player.getUniqueId());
            int miniToStart = teamSize * 2;
            if (entry.getParticipants().size() == miniToStart) {
                startCountdown(entry);
            }
            if (teamSize == 1) {
                entry.getParticipants().add(player.getUniqueId());
                notifyParticipants(entry, player.getName() + " has joined the tournament!");
            } else if (party != null && party.getMembers().size() == teamSize) {
                entry.getParticipants().addAll(party.getMembers());
                notifyParticipants(entry, "Party led by " + player.getName() + " has joined the tournament!");
            } else {
                player.sendMessage(ChatColor.RED + "You must be in a party of " + teamSize + " to join this tournament!");
            }
        }
    }

    private void notifyParticipants(TournamentEntry entry, String message) {
        for (UUID participant : entry.getParticipants()) {
            Player player = Bukkit.getPlayer(participant);
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + message);
            }
        }
    }

    public void leaveTournament(final UUID playerId) {
        for (TournamentEntry entry : tournaments.values()) {
            if (entry.getParticipants().contains(playerId)) {
                if (entry.getTeamSize() > 1) {
                    disqualifyTeam(entry, playerId);
                } else {
                    entry.getParticipants().remove(playerId);
                }
                notifyParticipants(entry, Bukkit.getPlayer(playerId).getName() + " has left the tournament!");
                break;
            }
        }
    }

    private void disqualifyTeam(TournamentEntry entry, UUID playerId) {
        PartyManager.PartyEntry party = Utils.getPartyByUUID(playerId);
        if (party != null) {
            entry.getParticipants().removeAll(party.getMembers());
        }
    }

    public void handleTournamentDeath(Player killed, Player killer, TournamentEntry tournamentEntry) {
        int teamSize = tournamentEntry.getTeamSize();

        if (teamSize == 1) {
            tournamentEntry.getParticipants().remove(killed.getUniqueId());
            notifyParticipants(tournamentEntry, (killer != null ? killer.getName() : "Environment") + " has eliminated " + killed.getName() + " from the tournament!");

            if (tournamentEntry.getParticipants().size() == 1) {
                UUID winnerId = tournamentEntry.getParticipants().get(0);
                Bukkit.broadcastMessage(ChatColor.GOLD + "Tournament ended! Winner: " + Bukkit.getPlayer(winnerId).getName());
                return;
            }
        } else {
            Duel duel = Utils.getDuelByUUID(killed.getUniqueId());
            PartyManager.PartyEntry party = Utils.getPartyByUUID(killed.getUniqueId());
            if (duel != null) {
                if (duel.getFirstAlives().contains(killed.getUniqueId())) duel.getFirstAlives().remove(killed.getUniqueId());
                if (duel.getSecondAlives().contains(killed.getUniqueId())) duel.getFirstAlives().remove(killed.getUniqueId());
                if (duel.getFirstAlives().isEmpty() || duel.getSecondAlives().isEmpty()) {
                    tournamentEntry.getParticipants().removeAll(duel.getFirst().contains(killed.getUniqueId()) ? duel.getFirst() : duel.getSecond());
                    notifyParticipants(tournamentEntry, "Team led by " + Bukkit.getPlayer(party.getCreator()).getName() + " has been eliminated from the tournament!");

                    if (tournamentEntry.getParticipants().size() <= teamSize) {
                        UUID winnerId = tournamentEntry.getParticipants().get(0);
                        Bukkit.broadcastMessage(ChatColor.GOLD + "Tournament ended! Winner: " + Bukkit.getPlayer(winnerId).getName());
                        return;
                    }
                }
            }
        }

        if (activeDuels.isEmpty()) {
            runTournamentRound(tournamentEntry);
        }
    }
}

package akyto.practice.handler.manager;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.kit.Kit;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.tournament.TournamentState;
import akyto.practice.utils.Utils;
import akyto.core.profile.ProfileState;
import akyto.practice.utils.chat.MessageUtils;
import akyto.practice.utils.match.TagUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
public class TournamentManager implements Listener {

    private final Practice main;
    private final Map<UUID, TournamentEntry> tournaments;
    private List<Duel> activeDuels;
    private final int countdownDuration = 60;

    public TournamentManager(Practice main) {
        this.main = main;
        this.tournaments = new HashMap<>();
        this.activeDuels = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    public UUID createTournament(Player creator, Kit kit, int teamSize) {
        if (!Utils.getProfiles(creator.getUniqueId()).isInState(ProfileState.FREE)) {
            creator.sendMessage(ChatColor.RED + "You must be in FREE state to create a tournament!");
            return null;
        }
        if (teamSize > 1) {
            if (Utils.getPartyByUUID(creator.getUniqueId()) == null) {
                creator.sendMessage(ChatColor.RED + "You must be into party for launch a tournament into " + teamSize + " vs " + teamSize + ".");
                return null;
            }
            if (Utils.getPartyByUUID(creator.getUniqueId()).getMembers().size() != teamSize) {
                creator.sendMessage(ChatColor.RED + "You must be " + teamSize + " in your party to launch a tournament of this size.");
                return null;
            }
        }
        UUID tournamentId = UUID.randomUUID();
        TournamentEntry entry = new TournamentEntry(creator.getUniqueId(), tournamentId, kit, teamSize);
        tournaments.put(tournamentId, entry);
        creator.sendMessage(ChatColor.GREEN + "Tournament has been created in " + ChatColor.stripColor(kit.displayName()) + " in " + teamSize + "vs" + teamSize + " team size!");
        Bukkit.getOnlinePlayers().forEach(player -> {
            MessageUtils.sendTournamentRequestMessage(player, String.valueOf(tournamentId), kit.displayName(), teamSize);
        });
        return tournamentId;
    }

    private void startCountdown(TournamentEntry entry) {
        BukkitRunnable countdown = new BukkitRunnable() {
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
        };
        countdown.runTaskTimer(main, 0L, 20L);
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
        List<UUID> participants = new ArrayList<>(entry.getParticipants());
        int teamSize = entry.getTeamSize();

        if (!entry.getByePlayer().isEmpty()) {
            entry.getParticipants().addAll(entry.getByePlayer());
            entry.getByePlayer().clear();
        }

        if (participants.size() < teamSize) {
            UUID winnerId = participants.get(0);
            for (UUID participant : participants) {
                Player player = Bukkit.getPlayer(participant);
                if (player != null) {
                    player.sendMessage(ChatColor.GOLD + "Tournament ended! Winner: " + Bukkit.getPlayer(winnerId).getName());
                }
            }
            return;
        }

        if (teamSize == 1 && participants.size() % 2 != 0) {
            UUID byePlayer = participants.remove(participants.size() - 1);
            Player bye = Bukkit.getPlayer(byePlayer);
            if (bye != null) {
                bye.sendMessage(ChatColor.YELLOW + "You have been qualified for the next round due to an odd number of players.");
            }
            entry.getByePlayer().add(byePlayer);
        }

        Collections.shuffle(participants);

        for (int i = 0; i < participants.size(); i += 2 * teamSize) {
            List<UUID> team1 = new ArrayList<>(participants.subList(i, Math.min(i + teamSize, participants.size())));
            List<UUID> team2 = new ArrayList<>(participants.subList(Math.min(i + teamSize, participants.size()), Math.min(i + 2 * teamSize, participants.size())));

            if (team1.isEmpty() || team2.isEmpty()) continue;

            entry.setTournamentState(TournamentState.FIGHT);
            Duel duel = new Duel(main, new HashSet<>(team1), new HashSet<>(team2), false, entry.getKit(), Duel.DuelType.SPLIT, null);
            activeDuels.add(duel);
        }
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
            int minToStart = teamSize * 2;
            int inTourney = entry.getParticipants().size() + 1;
            if (inTourney == minToStart) {
                startCountdown(entry);
            }
            if (teamSize == 1) {
                entry.getParticipants().add(player.getUniqueId());
                notifyParticipants(entry, player.getName() + " has joined the tournament!");
                Profile profile = Utils.getProfiles(player.getUniqueId());
                profile.setProfileState(ProfileState.TOURNAMENT);
                this.main.getManagerHandler().getItemManager().giveItems(player.getUniqueId(), false);
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
        final String leaverName = CoreUtils.getName(playerId);
        for (TournamentEntry entry : tournaments.values()) {
            if (entry.getParticipants().contains(playerId)) {
                if (entry.getTeamSize() > 1) {
                    disqualifyTeam(entry, playerId);
                    if (entry.getTournamentState().equals(TournamentState.WAITING)) {
                        PartyManager.PartyEntry partyEntry = Utils.getPartyByUUID(playerId);
                        partyEntry.getMembers().forEach(member -> {
                            Player player = Bukkit.getPlayer(member);
                            Utils.sendToSpawn(member, true);
                            player.sendMessage(ChatColor.RED + leaverName + " left the tournament, the rest of the party left too.");
                        });
                    }
                } else {
                    entry.getParticipants().remove(playerId);
                }
                notifyParticipants(entry, leaverName + " has left the tournament!");
                if (entry.getTournamentState().equals(TournamentState.WAITING)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        Utils.sendToSpawn(playerId, true);
                        player.sendMessage(ChatColor.RED + "You left the tournament.");
                    }
                }
                if (entry.getParticipants().isEmpty()) {
                    tournaments.remove(entry.getId());
                }
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
        Duel duel = Utils.getDuelByUUID(killed.getUniqueId());
        if (teamSize == 1) {
            notifyParticipants(tournamentEntry, (killer != null ? killer.getName() : "Environment") + " has eliminated " + killed.getName() + " from the tournament!");
            this.activeDuels.remove(Utils.getDuelByUUID(killed.getUniqueId()));
        } else {
            PartyManager.PartyEntry party = Utils.getPartyByUUID(killed.getUniqueId());
            if (duel != null) {
                if (duel.getFirstAlives().isEmpty() || duel.getSecondAlives().isEmpty()) {
                    tournamentEntry.getParticipants().removeAll(duel.getFirst().contains(killed.getUniqueId()) ? duel.getFirst() : duel.getSecond());
                    notifyParticipants(tournamentEntry, "Team led by " + Bukkit.getPlayer(party.getCreator()).getName() + " has been eliminated from the tournament!");
                    this.activeDuels.remove(duel);
                }
            }
        }
        if (tournamentEntry.getParticipants().size() < teamSize) {
            UUID winnerId = tournamentEntry.getParticipants().get(0);
            Bukkit.broadcastMessage(ChatColor.GOLD + "Tournament ended! Winner: " + Bukkit.getPlayer(winnerId).getName());
            return;
        }
        if (duel != null) {
            final Set<UUID> winner = duel.getFirst().contains(killed.getUniqueId()) ? duel.getSecond() : duel.getFirst();
            final Set<UUID> looser = duel.getFirst().contains(killed.getUniqueId()) ? duel.getFirst() : duel.getSecond();
            tournamentEntry.getParticipants().removeAll(looser);
            TagUtils.clearEntries(Arrays.asList(winner, looser));

            if (duel.getTimer() != null) {
                duel.getTimer().cancel();
            }

            winner.forEach(uuid -> Practice.API.getManagerHandler().getInventoryManager().generatePreviewInventory(uuid, looser.stream().toList().getFirst()));
            duel.setState(DuelState.FINISHING);
            MessageUtils.sendPartyComponent(Arrays.asList(winner, looser));
            Arrays.asList(winner, looser).forEach(uuids -> uuids.forEach(uuid -> {
                if (Bukkit.getPlayer(uuid) != null) {
                    Core.API.getManagerHandler().getInventoryManager().generateProfileInventory(uuid, this.main.getKits().size(), this.main.getKitNames());
                }
            }));
            duel.getFirst().forEach(first -> {
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    Utils.sendToSpawn(first, true);
                }, 10L);
            });

            duel.getSecond().forEach(first -> {
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    Utils.sendToSpawn(first, true);
                }, 10L);
            });
        }
        if (activeDuels.isEmpty()) {
            tournamentEntry.getParticipants().forEach(participant -> {
                final Player player = Bukkit.getPlayer(participant);
                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "Next round gonna start!");
                }
            });
            Bukkit.getScheduler().runTaskLater(Practice.API, () -> {
                runTournamentRound(tournamentEntry);
            }, 60L);
        }
    }
}

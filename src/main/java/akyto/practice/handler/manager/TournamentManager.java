package akyto.practice.handler.manager;

import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.kit.Kit;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.tournament.TournamentState;
import akyto.practice.utils.Utils;
import akyto.practice.utils.chat.MessageHelper;
import akyto.practice.utils.chat.MessageUtils;
import akyto.practice.utils.match.MatchUtils;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class TournamentManager implements Listener {

    private final Practice main;
    private final Map<UUID, TournamentEntry> tournaments;
    private final List<Duel> activeDuels;
    public static final String prefix = ChatColor.GRAY + "[" + ChatColor.DARK_RED + "TOURNAMENT" + ChatColor.GRAY + "] ";

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
        UUID tournamentId = UUID.randomUUID();
        TournamentEntry entry = new TournamentEntry(creator.getUniqueId(), tournamentId, kit, teamSize);
        tournaments.put(tournamentId, entry);
        creator.sendMessage(ChatColor.GREEN + "Tournament has been created in " + ChatColor.stripColor(kit.displayName()) + " in " + teamSize + "vs" + teamSize + " team size!");
        Bukkit.getOnlinePlayers().forEach(player -> {
            MessageUtils.sendTournamentRequestMessage(player, String.valueOf(tournamentId), kit.displayName(), teamSize);
        });
        return tournamentId;
    }

    public void startTournament(TournamentEntry entry) {
        List<UUID> participants = entry.getParticipants();
        int teamSize = entry.getTeamSize();

        if (participants.size() < 2) {
            MessageHelper.notifyUUID(entry.getParticipants(), prefix + "&cNot enough player to start!");
            return;
        }

        Map<UUID, List<UUID>> parties = new HashMap<>();
        for (UUID playerId : participants) {
            if (Utils.getPartyByUUID(playerId) != null) {
                UUID partyLeader = Utils.getPartyByUUID(playerId).getCreator();
                parties.computeIfAbsent(partyLeader, k -> new ArrayList<>()).add(playerId);
            } else {
                parties.computeIfAbsent(playerId, k -> new ArrayList<>()).add(playerId);
            }
        }

        List<UUID> validParticipants = Lists.newArrayList();
        for (List<UUID> party : parties.values()) {
            if (party.size() <= teamSize) {
                validParticipants.addAll(party);
            } else {
                MessageHelper.notifyUUID(party, prefix + "&cYour party is too large for this tournament!");
            }
        }

        if (validParticipants.size() < 2) {
            MessageHelper.notifyUUID(entry.getParticipants(), prefix + "&cNot enough valid players to start!");
            return;
        }

        entry.setTournamentState(TournamentState.STARTED);

        List<List<UUID>> originalTeams = new ArrayList<>();
        List<UUID> remainingPlayers = new ArrayList<>(validParticipants);

        for (List<UUID> party : parties.values()) {
            if (party.size() <= teamSize && new HashSet<>(validParticipants).containsAll(party)) {
                originalTeams.add(new ArrayList<>(party));
                remainingPlayers.removeAll(party);
            }
        }

        while (!remainingPlayers.isEmpty()) {
            List<UUID> team = new ArrayList<>();
            int spotsLeft = teamSize;

            Iterator<UUID> iterator = remainingPlayers.iterator();
            while (iterator.hasNext() && spotsLeft > 0) {
                UUID playerId = iterator.next();
                team.add(playerId);
                iterator.remove();
                spotsLeft--;
            }

            if (!team.isEmpty()) {
                originalTeams.add(team);
            }
        }

        List<List<UUID>> mergedTeams = new ArrayList<>();
        Set<UUID> usedPlayers = new HashSet<>();

        List<List<UUID>> teamsToProcess = new ArrayList<>(originalTeams);

        for (int i = 0; i < teamsToProcess.size(); i++) {
            List<UUID> teamA = teamsToProcess.get(i);

            if (teamA.stream().anyMatch(usedPlayers::contains)) continue;

            if (teamA.size() == teamSize) {
                mergedTeams.add(teamA);
                usedPlayers.addAll(teamA);
            } else {
                boolean merged = false;
                for (int j = i + 1; j < teamsToProcess.size(); j++) {
                    List<UUID> teamB = teamsToProcess.get(j);

                    if (teamB.stream().anyMatch(usedPlayers::contains)) continue;

                    if (teamA.size() + teamB.size() == teamSize) {
                        List<UUID> mergedTeam = new ArrayList<>();
                        mergedTeam.addAll(teamA);
                        mergedTeam.addAll(teamB);
                        mergedTeams.add(mergedTeam);
                        usedPlayers.addAll(mergedTeam);
                        merged = true;
                        break;
                    }
                }
                if (!merged && !usedPlayers.containsAll(teamA)) {
                    mergedTeams.add(teamA);
                    usedPlayers.addAll(teamA);
                }
            }
        }

        entry.setOriginalTeams(mergedTeams);

        createTournamentMatches(entry);
    }

    private void createTournamentMatches(TournamentEntry entry) {
        List<List<UUID>> originalTeams = entry.getOriginalTeams();
        List<List<UUID>> remainingTeams = new ArrayList<>(originalTeams);

        Collections.shuffle(remainingTeams);

        while (remainingTeams.size() >= 2) {
            List<UUID> team1 = remainingTeams.removeFirst();
            List<UUID> team2 = remainingTeams.removeFirst();
            System.out.println("Team1: " + team1);
            System.out.println("Team2: " + team2);
            MatchUtils.createTournamentMatch(entry, team1, team2);
        }

        if (!remainingTeams.isEmpty()) {
            List<UUID> teamWithBye = remainingTeams.getFirst();
            MessageHelper.notifyUUID(teamWithBye, prefix + "&aYour team gets a bye in this round!");
        }
    }

    public Collection<TournamentEntry> getAllTournaments() {
        return tournaments.values();
    }

    public TournamentEntry getTournamentEntry(UUID tournamentId) {
        return tournaments.get(tournamentId);
    }

    public void joinTournament(Player player, UUID tournamentId) {
        joinTournament(player, tournamentId, null);
    }

    public void joinTournament(Player player, UUID tournamentId, UUID partyLeader) {
        if (!Utils.getProfiles(player.getUniqueId()).isInState(ProfileState.FREE)) {
            player.sendMessage(ChatColor.RED + "You must be in FREE state to join a tournament!");
            return;
        }

        TournamentEntry entry = tournaments.get(tournamentId);
        if (entry == null) {
            player.sendMessage(ChatColor.RED + "No tournament found with this ID!");
            return;
        }

        int teamSize = entry.getTeamSize();

        if (partyLeader != null) {
            if (teamSize == 1) {
                player.sendMessage(ChatColor.RED + "You cannot join a 1v1 tournament with a party!");
                return;
            }

            Set<UUID> partyMembers = Utils.getPartyByUUID(partyLeader).getMembers();
            if (partyMembers.size() > teamSize) {
                player.sendMessage(ChatColor.RED + "Your party is too large for this tournament!");
                return;
            }
        }

        if (Utils.getPartyByUUID(player.getUniqueId()) != null) {
            if (!Utils.getPartyByUUID(player.getUniqueId()).getCreator().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Only the creator of the party decide to join a tournament!");
                return;
            }
            entry.getParticipants().addAll(Utils.getPartyByUUID(player.getUniqueId()).getMembers());
        } else {
            entry.getParticipants().add(player.getUniqueId());
        }

        int minToStart = teamSize > 1 ? (teamSize > 2 ? 4 : 3) : 2;
        int inTourney = entry.getParticipants().size();

        if (inTourney >= minToStart && !entry.isCountdownStarted()) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(main, () -> {
                entry.getCountdown().runTaskTimer(main, 20L, 20L);
            }, 120L);
        }
        MessageHelper.notifyUUID(entry.getParticipants(), prefix + player.getName() + "&a joined!");

        if (partyLeader != null) {
            MessageHelper.notifyUUID(entry.getParticipants(), prefix + player.getName() + "&a joined with a party!");
        }
        main.getManagerHandler().getInventoryManager().refreshTournamentInventory();
        this.main.getManagerHandler().getItemManager().giveItems(player.getUniqueId(), false);
    }

    public void leaveTournament(final UUID playerId) {
        final String leaverName = CoreUtils.getName(playerId);
        for (TournamentEntry entry : tournaments.values()) {
            if (entry.getParticipants().contains(playerId)) {
                entry.getParticipants().remove(playerId);
                MessageHelper.notifyUUID(entry.getParticipants(), prefix + leaverName + " &cleft!");

                if (entry.getTournamentState().equals(TournamentState.WAITING)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        Utils.sendToSpawn(playerId, true);
                        player.sendMessage(ChatColor.RED + "You left the tournament.");
                    }
                }

                if (entry.getParticipants().isEmpty()) {
                    main.getManagerHandler().getInventoryManager().refreshTournamentInventory();
                    if (entry.isCountdownStarted()) {
                        entry.getCountdown().cancel();
                    }
                    tournaments.remove(entry.getId());
                }
                break;
            }
        }
    }
}
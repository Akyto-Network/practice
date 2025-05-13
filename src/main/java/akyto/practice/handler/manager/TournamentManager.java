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
    public static String prefix = ChatColor.GRAY + "[" + ChatColor.DARK_RED + "TOURNAMENT" + ChatColor.GRAY + "] ";

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
                if (timeLeft % 20 == 0) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        MessageUtils.sendTournamentRequestMessage(player, String.valueOf(entry.getId()), entry.getKit().displayName(), entry.getTeamSize());
                    });
                }
                if (timeLeft <= 10 || timeLeft % 10 == 0) {
                    MessageHelper.notifyUUID(entry.getParticipants(), prefix + ChatColor.WHITE + "Starting in " + ChatColor.RED + timeLeft + ChatColor.WHITE + " seconds!");
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
            MessageHelper.notifyUUID(entry.getParticipants(), prefix + "&cNot enough player to start!");
            return;
        }
        entry.setTournamentState(TournamentState.STARTED);
        MatchUtils.runTournamentRound(entry);
    }

    public Collection<TournamentEntry> getAllTournaments() {
        return tournaments.values();
    }

    public TournamentEntry getTournamentEntry(UUID tournamentId) {
        return tournaments.get(tournamentId);
    }

    public void joinTournament(Player player, UUID tournamentId) {
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
        int minToStart = teamSize * 2;
        int inTourney = entry.getParticipants().size() + 1;
        if (inTourney == minToStart) {
            Bukkit.getScheduler().runTaskLater(main, () -> {
                startCountdown(entry);
            }, 120L);
        }
        entry.getParticipants().add(player.getUniqueId());
        MessageHelper.notifyUUID(entry.getParticipants(), prefix + player.getName() + "&a join!");
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
                    tournaments.remove(entry.getId());
                }
                break;
            }
        }
    }
}

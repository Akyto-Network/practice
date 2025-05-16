package akyto.practice.runnable.countdown;

import akyto.practice.Practice;
import akyto.practice.event.EventState;
import akyto.practice.handler.manager.TournamentManager;
import akyto.practice.runnable.sumo.SumoEventRunnable;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.utils.chat.MessageHelper;
import akyto.practice.utils.chat.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class CountdownTournament extends BukkitRunnable {

    private final Practice main;
    private Integer counter;
    private TournamentEntry entry;
    public boolean isRunning;

    public CountdownTournament(final Practice main, final TournamentEntry entry) {
        this.main = main;
        this.counter = 10;
        this.entry = entry;
        this.isRunning = false;
    }

    @Override
    public void run() {
        this.isRunning = true;
        if (counter == 0) {
            this.isRunning = false;
            main.getManagerHandler().getTournamentManager().startTournament(entry);
            cancel();
            return;
        }
        if (counter % 20 == 0) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                MessageUtils.sendTournamentRequestMessage(player, String.valueOf(entry.getId()), entry.getKit().displayName(), entry.getTeamSize());
            });
        }
        if (counter <= 10 || counter % 10 == 0) {
            MessageHelper.notifyUUID(entry.getParticipants(), TournamentManager.prefix + ChatColor.WHITE + "Starting in " + ChatColor.RED + counter + ChatColor.WHITE + " seconds!");
        }

        counter--;
    }

}
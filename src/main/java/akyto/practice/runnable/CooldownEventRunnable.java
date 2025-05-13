package akyto.practice.runnable;

import akyto.practice.Practice;
import akyto.practice.event.EventState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class CooldownEventRunnable extends BukkitRunnable {

    private final Practice main;
    private Integer counter;
    private final String nameOne;
    private final String nameTwo;

    public CooldownEventRunnable(final Practice main, final String name1, final String name2) {
        this.main = main;
        this.counter = 6;
        this.nameOne = name1;
        this.nameTwo = name2;
        this.run();
    }

    @Override
    public void run() {
        if (this.main.getManagerHandler().getEventManager().getEventState().equals(EventState.FREE) || this.main.getManagerHandler().getEventManager().getEventState().equals(EventState.FIGHT) || this.main.getManagerHandler().getEventManager().getEventState().equals(EventState.ENDING) || this.main.getManagerHandler().getEventManager().getEventState().equals(EventState.WAITING)) {
            this.cancel();
            return;
        }
        counter--;
        if (counter <= 0) {
            this.main.getManagerHandler().getEventManager().getMembers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1f, 1f);
                    player.sendMessage(ChatColor.RED + this.nameOne + ChatColor.WHITE + " against " + ChatColor.GREEN + this.nameTwo + ChatColor.WHITE + " has begun!");
                }
            });
            this.main.getManagerHandler().getEventManager().setEventState(EventState.FIGHT);
            new SumoEventRunnable(main).runTaskTimer(this.main, 5L, 5L);
            this.cancel();
            return;
        }
        this.main.getManagerHandler().getEventManager().getMembers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.5f, 1.5f);
                player.sendMessage(ChatColor.RED.toString() + counter + "s" + ChatColor.GRAY + "...");
            }
        });
    }

}
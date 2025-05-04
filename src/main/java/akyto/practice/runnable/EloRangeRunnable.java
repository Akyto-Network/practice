package akyto.practice.runnable;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.handler.manager.QueueManager;
import akyto.practice.utils.Utils;
import akyto.practice.utils.match.MatchUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EloRangeRunnable extends BukkitRunnable {

    private UUID uuid;
    private int updateTime;
    private int timer;
    private QueueManager.QueueEntry queueEntry;

    public EloRangeRunnable(final UUID uuid, final QueueManager.QueueEntry queueEntry) {
        this.uuid = uuid;
        this.timer = 0;
        this.updateTime = 10; // Every 10seconds (1second per tick)
        this.queueEntry = queueEntry;
    }

    @Override
    public void run() {
        final Profile profile = Utils.getProfiles(uuid);
        if (!profile.isInState(ProfileState.QUEUE)) {
            this.cancel();
            return;
        }
        this.timer++;
        if (this.queueEntry.getEloRange().getMinRange() < 0) {
            MatchUtils.researchMatch(uuid, this.queueEntry.getKit(), true);
            return;
        }
        if (timer == updateTime) {
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.GRAY + " » " + ChatColor.WHITE + "Elo range as been updated from " + ChatColor.GRAY + "[" + ChatColor.RED + this.queueEntry.getEloRange().getMinRange() + ChatColor.GRAY + "->" + ChatColor.GREEN + this.queueEntry.getEloRange().getMaxRange() + ChatColor.GRAY + "] " + ChatColor.WHITE + " to " + ChatColor.GRAY + "[" + ChatColor.RED + (this.queueEntry.getEloRange().getMinRange()-100) + ChatColor.GRAY + "->" + ChatColor.GREEN + (this.queueEntry.getEloRange().getMaxRange()+100) + ChatColor.GRAY + "]");
            this.queueEntry.getEloRange().setNewRange(100);
            timer = 0;
            MatchUtils.researchMatch(uuid, this.queueEntry.getKit(), true);
        }
    }
}

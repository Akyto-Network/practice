package akyto.practice.handler.manager;

import java.util.UUID;
import java.util.Map.Entry;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.practice.runnable.EloRangeRunnable;
import akyto.practice.utils.match.EloUtils;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.google.common.collect.Sets;

import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.kit.Kit;
import akyto.practice.utils.Utils;
import lombok.Getter;

@Getter
public class QueueManager {
    
    private final Practice main;
    
    public QueueManager(final Practice main) { this.main = main; }
    
    public void addPlayerToQueue(final UUID uuid, final Kit kit, final boolean ranked) {
        if (!ranked) this.researchMatch(uuid, kit, ranked);
        this.main.getQueue().put(uuid, new QueueEntry(uuid, kit, ranked));
        Utils.getProfiles(uuid).setProfileState(ProfileState.QUEUE);
        this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
        this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);
        Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + "You have successfully joined the " + (ranked ? "" : "un") + "ranked " + ChatColor.stripColor(kit.displayName()) + " queue");
        if (ranked) {
            this.main.getQueue().get(uuid).getEloRangeRunnable().runTaskTimer(main, 20L, 20L);
        }
    }

    public void removePlayerFromQueue(final UUID uuid) {
        if (this.main.getQueue().containsKey(uuid)) {
        	final QueueEntry queue = this.main.getQueue().get(uuid);
            if (queue.isRanked()) {
                queue.getEloRangeRunnable().cancel();
            }
        	this.main.getQueue().remove(uuid);
            Utils.getProfiles(uuid).setProfileState(ProfileState.FREE);
            this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
            this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(queue.isRanked(), queue.getKit());
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "You have successfully left the queue");
        }
    }

    public void researchMatch(final UUID uuid, final Kit kit, final boolean ranked) {
        for (Entry<UUID, QueueEntry> entry : main.getQueue().entrySet()) {
            QueueEntry queueEntry = entry.getValue();
            if (queueEntry.getKit().equals(kit) && queueEntry.isRanked() == ranked && !entry.getKey().equals(uuid)) {
                if (queueEntry.isRanked()) {
                    if (!this.checkCompatibilityElo(entry.getKey(), uuid)) {
                        return;
                    }
                    queueEntry.getEloRangeRunnable().cancel();
                }
                new Duel(this.main, Sets.newHashSet(entry.getKey()), Sets.newHashSet(uuid), queueEntry.isRanked(), queueEntry.getKit(), DuelType.SINGLE, null);
                main.getQueue().remove(entry.getKey());
                if (ranked) main.getQueue().remove(uuid);
                this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);
                return;
            }
        }
    }


    private boolean checkCompatibilityElo(final UUID playerA, final UUID playerB) {
        final QueueEntry queueEntryA = this.main.getQueue().get(playerA);
        final QueueEntry queueEntryB = this.main.getQueue().get(playerB);
        final int kitId = queueEntryA.getKit().id();
        final int maxRangeB = queueEntryB == null ? EloUtils.getElo(playerB, kitId)+100 : queueEntryB.getEloRange().getMaxRange();
        final int minRangeB = queueEntryB == null ? EloUtils.getElo(playerB, kitId)-100 : queueEntryB.getEloRange().getMinRange();
        return EloUtils.getElo(playerB, kitId) <= queueEntryA.getEloRange().getMaxRange() && EloUtils.getElo(playerB, kitId) >= queueEntryA.getEloRange().getMinRange() && EloUtils.getElo(playerA, kitId) <= maxRangeB && EloUtils.getElo(playerA, kitId) >= minRangeB;
    }

    @Getter
    public static class EloRange {
        @Setter
        private int minRange;
        @Setter
        private int maxRange;

        public EloRange(final int elo) {
            this.minRange = elo-100;
            this.maxRange = elo+100;
        }

        public void setNewRange(final int i) {
            this.minRange -= i;
            this.maxRange += i;
        }
    }
    
    @Getter
    public static class QueueEntry {
        private final UUID uuid;
        private final Kit kit;
        private final boolean ranked;
        private EloRange eloRange;
        public EloRangeRunnable eloRangeRunnable;
        
        public QueueEntry(final UUID uuid, final Kit kit, final boolean ranked) {
            this.uuid = uuid;
            this.kit = kit;
            this.ranked = ranked;
            if (ranked) {
                this.eloRange = new EloRange(Core.API.getManagerHandler().getProfileManager().getProfile(uuid).getStats().get(2)[kit.id()]);
                this.eloRangeRunnable = new EloRangeRunnable(uuid, this);
            }
        }
    }
}

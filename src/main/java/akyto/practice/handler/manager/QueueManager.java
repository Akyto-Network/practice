package akyto.practice.handler.manager;

import java.util.UUID;
import java.util.Map.Entry;
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
        for (Entry<UUID, QueueEntry> entry : main.getQueue().entrySet()) {
            QueueEntry queueEntry = entry.getValue();
            if (queueEntry.getKit().equals(kit) && queueEntry.isRanked() == ranked && !entry.getKey().equals(uuid)) {
                new Duel(this.main, Sets.newHashSet(entry.getKey()), Sets.newHashSet(uuid), queueEntry.isRanked(), queueEntry.getKit(), DuelType.SINGLE, null);
                main.getQueue().remove(entry.getKey());
                this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);
                return;
            }
        }
        this.main.getQueue().put(uuid, new QueueEntry(uuid, kit, ranked));
        Utils.getProfiles(uuid).setProfileState(ProfileState.QUEUE);
        this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
        this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);
        Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + "You have successfully joined the " + (ranked ? "" : "un") + "ranked " + ChatColor.stripColor(kit.displayName()) + " queue");
    }
    
    public void removePlayerFromQueue(final UUID uuid) {
        if (this.main.getQueue().containsKey(uuid)) {
        	final QueueEntry queue = this.main.getQueue().get(uuid);
        	this.main.getQueue().remove(uuid);
            Utils.getProfiles(uuid).setProfileState(ProfileState.FREE);
            this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
            this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(queue.isRanked(), queue.getKit());
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "You have successfully left the queue");
        }
    }
    
    @Getter
    public static class QueueEntry {
        private final UUID uuid;
        private final Kit kit;
        private final boolean ranked;
        
        public QueueEntry(final UUID uuid, final Kit kit, final boolean ranked) {
            this.uuid = uuid;
            this.kit = kit;
            this.ranked = ranked;
        }
    }
}

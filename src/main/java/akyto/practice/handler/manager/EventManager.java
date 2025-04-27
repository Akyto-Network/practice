package akyto.practice.handler.manager;


import akyto.core.event.EventState;
import akyto.practice.Practice;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

@Getter
public class EventManager {

    private Practice main;
    @Setter
    private EventState eventState;
    private List<UUID> alives;
    private List<UUID> members;
    private Location waitingLocation = new Location(Bukkit.getWorld("world"), 10980.572d, 101.00000d, 10999.431d, -89.3f, -0.4f);
    private List<UUID> playersSelected;
    private Integer minimumToStart = 4;
    private Long cooldown = 0L;

    public EventManager(final Practice main) {
        this.main = main;
        this.eventState = EventState.FREE;
    }

    public void startEvent(final UUID creator, String type) {
        if (this.getCooldown() > 0L) {
            final double time = this.getCooldown() / 1000.0D;
            final DecimalFormat df  = new DecimalFormat("##.##");
            Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "The cooldown to launch an event is currently underway please be patient" + ": " + df.format(time) + "'s.");
            return;
        }
        if(!this.getEventState().equals(EventState.FREE)) {
            Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "Another event is currently underway, please \"/join\" to join!");
        }
        if (Bukkit.getPlayer(creator) != null) {
            Bukkit.getPlayer(creator).teleport(main.getSumoEvent().getLocation());
        }
        this.setEventState(EventState.WAITING);
        this.alives = Lists.newArrayList(creator);
    }

    public long getCooldown() {
        return Math.max(0L, this.cooldown - System.currentTimeMillis());
    }

}
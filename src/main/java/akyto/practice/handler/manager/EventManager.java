package akyto.practice.handler.manager;


import akyto.core.event.EventState;
import akyto.practice.Practice;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class EventManager {

    private Practice main;
    private EventState eventState;
    public EventState getEventState() { return eventState; }
    public void setEventState(EventState eventState) { this.eventState = eventState; }
    private java.util.List<UUID> alives;
    public java.util.List<UUID> getAlives() { return alives; }
    private java.util.List<UUID> members;
    public java.util.List<UUID> getMembers() { return members; }
    private Location waitingLocation = new Location(Bukkit.getWorld("world"), 10980.572d, 101.00000d, 10999.431d, -89.3f, -0.4f);
    private java.util.List<UUID> playersSelected;
    public List<UUID> getPlayersSelected() { return playersSelected; }
    private Integer minimumToStart = 4;
    public Integer getMinimumToStart() { return minimumToStart; }
    private Long cooldown = 0L;

    public EventManager(final Practice main) {
        this.main = main;
        this.eventState = EventState.FREE;
    }

    public void startEvent(final UUID creator) {
        if (this.getCooldown() > 0L) {
            final double time = this.getCooldown() / 1000.0D;
            final DecimalFormat df  = new DecimalFormat("##.##");
            Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "The cooldown to launch an event is currently underway please be patient" + ": " + df.format(time) + "'s.");
            return;
        }
        if(!this.getEventState().equals(EventState.FREE)) {
            Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "Another event is currently underway, please \"/join\" to join!");
        }
        this.setEventState(EventState.WAITING);
        this.alives = Lists.newArrayList(creator);
    }

    public long getCooldown() {
        return Math.max(0L, this.cooldown - System.currentTimeMillis());
    }



    }

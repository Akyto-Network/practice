package akyto.practice.handler.manager;


import akyto.core.Core;
import akyto.practice.event.EventState;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.runnable.CooldownEventRunnable;
import akyto.practice.runnable.EventRunnable;
import akyto.practice.utils.Utils;
import akyto.practice.utils.location.LocationUtil;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Getter
public class EventManager {

    private Practice main;
    @Setter
    private EventState eventState;
    private List<UUID> alives;
    private List<UUID> members;
    private List<UUID> playersSelected;
    private Integer minimumToStart = 2;
    private Long cooldown = 0L;
    private String type;

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
        this.type = type.toLowerCase();
        this.setEventState(EventState.WAITING);
        this.alives = Lists.newArrayList(creator);
        this.members = Lists.newArrayList(creator);
        this.playersSelected = Lists.newArrayList();
        Core.API.getManagerHandler().getProfileManager().getProfiles().get(creator).setProfileState(ProfileState.EVENT);
        this.main.getManagerHandler().getItemManager().giveItems(creator, false);
        final TextComponent requestMessage = new TextComponent(ChatColor.GRAY + " (*) " + ChatColor.WHITE + Bukkit.getPlayer(creator).getName() + ChatColor.RED + " to launch a " + type.toLowerCase() + " event! " + ChatColor.GRAY + "(Click to join)");
        requestMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join"));
        requestMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Join " + type + " event!").create()));
        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(requestMessage));
        if (Bukkit.getPlayer(creator) != null) {
            Bukkit.getPlayer(creator).teleport(this.type.equals("sumo") ? this.main.getSumoEvent().getLocation() : this.main.getOitcEvent().getLocation()); // TODO: Conditions with type to teleport to the correct event locations.
        }
        this.setEventState(EventState.WAITING);
        this.alives = Lists.newArrayList(creator);
    }

    public void joinEvent(final UUID joiner) {
        final Profile profile = Core.API.getManagerHandler().getProfileManager().getProfiles().get(joiner);
        final Player player = Bukkit.getPlayer(joiner);
        if (!profile.isInState(ProfileState.FREE)) {
            player.sendMessage(ChatColor.RED + "You are not available to do this right now.");
        }
        if (this.getEventState().equals(EventState.FREE)) {
            player.sendMessage(ChatColor.RED + "We're sorry, but there are no " + this.type + " events currently available.");
            return;
        }
        this.members.forEach(uuid ->
                Bukkit.getPlayer(uuid).sendMessage(player.getName() + ChatColor.GREEN + (this.getEventState().equals(EventState.WAITING) ? " joined " : " viewing ") + " the " + this.type + " event now!")
        );
        this.members.add(joiner);
        if (this.getEventState().equals(EventState.WAITING)) {
            if (this.type.equals("sumo")) this.alives.add(joiner);
            if (this.members.size() == this.minimumToStart) {
                new EventRunnable(main, this.type).runTaskTimerAsynchronously(main, 20L, 20L);;
            }
        }
        profile.setProfileState(ProfileState.EVENT);
        this.main.getManagerHandler().getItemManager().giveItems(joiner, false);
        player.teleport(this.type.equals("sumo") ? this.main.getSumoEvent().getLocation() : this.main.getOitcEvent().getLocation());
    }

    public void leaveEvent(final UUID leaver, final boolean disconnect) {
        if (this.getEventState().equals(EventState.FREE)) {
            Bukkit.getPlayer(leaver).sendMessage(ChatColor.RED + "We're sorry, but there are no " + this.type + " events currently available.");
            return;
        }
        if (this.getEventState().equals(EventState.WAITING)) {
            this.members.forEach(uuid -> Bukkit.getPlayer(uuid).sendMessage(Bukkit.getPlayer(leaver).getName() + ChatColor.RED + " has left " + this.type + " event!"));
        }
        if (this.getType().equalsIgnoreCase("sumo")) {
            if (this.getEventState().equals(EventState.STARTING) || this.getEventState().equals(EventState.FIGHT)) {
                final UUID winner = this.getPlayersSelected().get(0).equals(leaver) ? this.getPlayersSelected().get(1) : this.getPlayersSelected().get(0);
                this.endMatch(winner, leaver);
            }
        }
        this.members.remove(leaver);
        if (this.alives.contains(leaver) && this.type.equals("sumo")) this.alives.remove(leaver);
        if (this.alives.isEmpty()) {
            this.setEventState(EventState.FREE);
        }
        if (!disconnect) {
            Utils.sendToSpawn(leaver, true);
        }
    }

    public void launchOitc() {
        this.setEventState(EventState.FIGHT);
        final Iterator<LocationUtil> loc = this.main.getOitcLoc().iterator();
        this.members.forEach(members -> {
            if (Bukkit.getPlayer(members) != null && loc.hasNext()) {
                final Player player = Bukkit.getPlayer(members);
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "OITC" + ChatColor.GRAY + "] " + ChatColor.WHITE + "Event has begun!");
                this.main.getManagerHandler().getItemManager().giveItems(members, false);
                player.teleport(loc.next().getLocation());
            }
        });
    }

    public void pickPlayer() {
        this.setEventState(EventState.STARTING);
        Collections.shuffle(this.alives);
        this.playersSelected.clear();
        this.playersSelected.add(this.alives.get(0));
        this.playersSelected.add(this.alives.get(1));
        this.playersSelected.forEach(uuid -> {
            Bukkit.getPlayer(uuid).getInventory().clear();
            Bukkit.getPlayer(uuid).updateInventory();
            Bukkit.getPlayer(uuid).teleport(uuid == this.alives.get(0) ? this.main.getSumoFirst().getLocation() : this.main.getSumoSecond().getLocation());
        });
        final String name1 = Bukkit.getPlayer(this.playersSelected.get(0)).getDisplayName();
        final String name2 = Bukkit.getPlayer(this.playersSelected.get(1)).getDisplayName();
        new CooldownEventRunnable(this.main, name1, name2).runTaskTimer(main, 20L, 20L);
    }

    public void endMatch(final UUID winner, final UUID killed) {
        this.members.forEach(uuid -> {
            Bukkit.getPlayer(uuid).sendMessage(" ");
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + Bukkit.getPlayer(winner).getName() + ChatColor.WHITE + " won this round!");
            Bukkit.getPlayer(uuid).sendMessage(" ");
        });
        if (this.type.equals("sumo")) {
            this.alives.remove(killed);
            this.playersSelected.forEach(uuid -> {
                Bukkit.getPlayer(uuid).teleport(this.main.getSumoEvent().getLocation());
                this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
            });
            if (this.alives.size() == 1) {
                this.members.forEach(uuid -> {
                    Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + Bukkit.getPlayer(winner).getName() + ChatColor.GRAY + " is the last survivor and the best of this sumo event, congratulations to him.");
                    Utils.sendToSpawn(uuid, true);
                });
                this.members.clear();
                this.alives.clear();
                this.setEventState(EventState.FREE);
                this.applyCooldown();
                return;
            }
            this.pickPlayer();
        }
        if (this.type.equals("oitc")){
            this.members.forEach(uuid -> {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + Bukkit.getPlayer(winner).getName() + ChatColor.GRAY + " is the the best of this oitc event, congratulations to him.");
                Utils.sendToSpawn(uuid, true);
            });
        }
        this.setEventState(EventState.ENDING);
    }

    public double limit(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public long getCooldown() {
        return Math.max(0L, this.cooldown - System.currentTimeMillis());
    }

    public void applyCooldown() {
        this.cooldown = Long.valueOf(System.currentTimeMillis() + 1800 * 1000);
    }

}
package akyto.practice.runnable;

import akyto.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class EventRunnable extends BukkitRunnable {

    private Practice main;
    private Integer counter;
    private String type;

    public EventRunnable(final Practice main, final String type) {
        this.main = main;
        this.counter = 61;
        this.type = type;
        this.run();
    }

    @Override
    public void run() {
        if (this.main.getManagerHandler().getEventManager().getAlives().size() < this.main.getManagerHandler().getEventManager().getMinimumToStart()) {
            this.cancel();
            return;
        }
        counter -= 1;
        if (counter <= 0) {
            this.main.getManagerHandler().getEventManager().getMembers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1f, 1f);
                    player.sendMessage(ChatColor.DARK_GRAY + "The " + this.type + " event has begun" + ChatColor.GRAY + "," + ChatColor.WHITE + " good luck.");
                }
            });
            this.main.getManagerHandler().getEventManager().pickPlayer();
            this.cancel();
        }
        if (counter == 30) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                final TextComponent requestMessage = new TextComponent(ChatColor.GRAY + " (*) " + ChatColor.RED + this.type.toUpperCase() + " event " + ChatColor.YELLOW + " start in 30's " + ChatColor.GRAY + "(Click to join)");
                requestMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join"));
                requestMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Join " + this.type + " event!").create()));
                player.spigot().sendMessage(requestMessage);
            });
        }
        if (counter == 15) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                final TextComponent requestMessage = new TextComponent(ChatColor.GRAY + " (*) " + ChatColor.RED + this.type.toUpperCase() + " event " + ChatColor.YELLOW + " start in 15's " + ChatColor.GRAY + "(Click to join)");
                requestMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join"));
                requestMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Join " + this.type + " event!").create()));
                player.spigot().sendMessage(requestMessage);
            });
        }
        if (counter <= 4) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                final TextComponent requestMessage = new TextComponent(ChatColor.GRAY + " (*) " + ChatColor.RED + this.type.toUpperCase() + " event " + ChatColor.YELLOW + " start in " + counter + "'s " + ChatColor.GRAY + "(Click to join)");
                requestMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join"));
                requestMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Join " + this.type + " event!").create()));
                player.spigot().sendMessage(requestMessage);
            });
        }
    }

}

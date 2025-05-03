package akyto.practice.utils.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class MessageHelper {

    public static void notifyPlayer(final List<Player> player, final String message) {
        player.forEach(p -> {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        });
    }

    public static void notifyUUID(final List<UUID> listUUID, final String message) {
        listUUID.forEach( uuid -> {
            final Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        });
    }
}

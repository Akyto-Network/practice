package kezukdev.akyto.handler.command;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.utils.format.FormatUtils;
import gg.potted.idb.DB;
import kezukdev.akyto.Practice;
import kezukdev.akyto.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ResetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akyto.stats.management")) {
            sender.sendMessage(ChatColor.RED + "Insuffiscient permissions!");
            return false;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "/reset <target>");
            return false;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        String playerName = Bukkit.getPlayer(args[0]) == null ? Bukkit.getOfflinePlayer(args[0]).getName() : Bukkit.getPlayer(args[0]).getName();
        if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsKey(args[0])) {
            playerName = Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().get(args[0]);
        }
        final UUID uuid = Bukkit.getPlayer(args[0]) == null ? Bukkit.getOfflinePlayer(args[0]).getUniqueId() : Bukkit.getPlayer(args[0]).getUniqueId();
        final int[] newArrayElo = new int[Practice.getAPI().getKits().size()];
        final int[] newArrayWin = new int[Practice.getAPI().getKits().size()];
        final int[] newArrayPlayed = new int[Practice.getAPI().getKits().size()];
        for (int i = 0; i < Practice.getAPI().getKits().size(); i++) {
            newArrayElo[i] = 1000;
            newArrayWin[i] = 0;
            newArrayPlayed[i] = 0;
        }
        final String finalPlayerName = playerName;
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            Core.API.getDatabaseSetup().resetElos(finalPlayerName, newArrayElo, newArrayWin, newArrayPlayed);
        });
        future.whenCompleteAsync((t, u) -> {
            Practice.getAPI().getManagerHandler().getInventoryManager().refreshLeaderboard();
            Bukkit.getLogger().warning(finalPlayerName + " global data is now cleared by " + sender.getName());
        });
        if (target != null) {
            final Profile profile = Utils.getProfiles(target.getUniqueId());
            profile.getStats().set(2, newArrayElo);
            profile.getStats().set(1, newArrayWin);
            profile.getStats().set(0, newArrayPlayed);
            Core.API.getManagerHandler().getInventoryManager().generateProfileInventory(target.getUniqueId(), Practice.getAPI().getKits().size(), Practice.getAPI().getKitNames());
        }
        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "Admin" + ChatColor.GRAY + "] " + ChatColor.YELLOW + args[0] + ChatColor.RED + " statistics has been clear!");
        return false;
    }
}

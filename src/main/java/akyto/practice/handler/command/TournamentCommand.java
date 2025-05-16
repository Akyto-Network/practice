package akyto.practice.handler.command;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.handler.manager.InventoryManager;
import akyto.practice.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TournamentCommand implements CommandExecutor {

    private final Practice main;
    public TournamentCommand(final Practice main) { this.main = main;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(ChatColor.RED + "Unavailable for CMD");
            return false;
        }
        final Player player = Bukkit.getPlayer(sender.getName());
        final Profile pm = Utils.getProfiles(player.getUniqueId());
        final boolean inTournament = Utils.getTournamentByPlayerUUID(player.getUniqueId()) != null;

        if (pm.getProfileState() != ProfileState.FREE || inTournament) {
            sender.sendMessage(ChatColor.RED + "You cannot do this right now");
            return false;
        }
        final InventoryManager inventoryManager = main.getManagerHandler().getInventoryManager();
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("start") && sender.hasPermission("akyto.tournament")) {
                if (!sender.hasPermission("akyto.tournament")) {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions !.");
                    return false;
                }
                playerSender.openInventory(inventoryManager.getTournamentSizeInventory());
            }
        }
        else {
            inventoryManager.refreshTournamentInventory();
            inventoryManager.getTournamentMultipage().open(playerSender, 1);
        }
        return false;
    }
}

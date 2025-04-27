package akyto.practice.handler.command;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {

    private final Practice main;
    public BuildCommand(Practice practice) { this.main = practice; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akyto.command.build")){
            sender.sendMessage(ChatColor.RED + "Insufficient permissions !.");
            return false;
        }
        final Player player = (Player) sender;
        final Profile pm = Utils.getProfiles(player.getUniqueId());
        if (pm.getProfileState().equals(ProfileState.BUILD)) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setFlying(false);
            pm.setProfileState(ProfileState.FREE);
            player.getPlayer().teleport(new Location(player.getWorld(),135.558d, 124.00000d, 6.626d, 91.5f, -1.1f ));
            this.main.getManagerHandler().getItemManager().giveItems(player.getPlayer().getUniqueId(), false);
            return false;
        }

        if (pm.getProfileState() != ProfileState.FREE) {
            sender.sendMessage(ChatColor.RED + "You cannot do that right now!");
        }


        if (pm.getProfileState().equals(ProfileState.FREE)) {
            pm.setProfileState(ProfileState.BUILD);
            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.closeInventory();
            return false;
        }

        return false;
    }
}
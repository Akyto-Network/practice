package akyto.practice.handler.command;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HostCommand implements CommandExecutor {

    private final Practice main;
    public HostCommand(final Practice main) { this.main = main;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "Unvailable for CMD"); return false; }
        if (args.length == 1){
            final Player player = Bukkit.getPlayer(sender.getName());
            final Profile pm = Utils.getProfiles(player.getUniqueId());
            if (pm.getProfileState() != ProfileState.FREE) {
                sender.sendMessage(ChatColor.RED + "You cannot do this right now");
                return false;
            }
            if(!sender.hasPermission("akyto.host")) {
                sender.sendMessage(ChatColor.RED + "Insufficient Permissions !.");
                return false;
            }
            final String arg = args[0];
            if (arg.equals("sumo") || arg.equals("oitc")){
                if (main.getSumoEvent().getLocation() == null || main.getOitcEvent().getLocation() == null) {
                    sender.sendMessage(ChatColor.RED + "Sorry but no maps was set!");
                    return false;
                }
                this.main.getManagerHandler().getEventManager().startEvent(player.getUniqueId(), args[0].toLowerCase());
                return false;
            }
            sender.sendMessage(ChatColor.RED + "/host <sumo/oitc>");
            return false;
        }
        else {
            sender.sendMessage(ChatColor.RED + " /host <sumo/oitc>");
            return false;
        }
    }
}

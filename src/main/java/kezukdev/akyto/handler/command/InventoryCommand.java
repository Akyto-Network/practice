package kezukdev.akyto.handler.command;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.profile.ProfileState;
import net.md_5.bungee.api.ChatColor;

public class InventoryCommand implements CommandExecutor {
	
	private final Practice main;
	
	public InventoryCommand(final Practice main) { this.main = main; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		if (args.length != 1) {
			sender.sendMessage(ChatColor.GRAY + " * " + ChatColor.RED + "/" + cmd.getName() + " <player>");
			return false;
		}
        if (this.main.getManagerHandler().getProfileManager().getProfiles().get(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.FIGHT) || this.main.getManagerHandler().getProfileManager().getProfiles().get(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.EDITOR)) {
            if (this.main.getManagerHandler().getProfileManager().getProfiles().get(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.FIGHT) && !this.main.getUtils().getDuelByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()).getState().equals(DuelState.FINISHING)) {
                sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
                return false;
            }
            if (this.main.getManagerHandler().getProfileManager().getProfiles().get(Bukkit.getPlayer(sender.getName()).getUniqueId()).getProfileState().equals(ProfileState.EDITOR)) {
                sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
                return false;
            }
        }
        final UUID targetUUID = Bukkit.getPlayer(args[0]) == null ? Bukkit.getOfflinePlayer(args[0]).getUniqueId() : Bukkit.getPlayer(args[0]).getUniqueId();
        if (this.main.getManagerHandler().getInventoryManager().getPreviewInventory().get(targetUUID) == null) {
            sender.sendMessage(ChatColor.GRAY + " * " + ChatColor.WHITE + args[0] + ChatColor.RED + " inventory does not exist.");
            return false;
        }
        Bukkit.getPlayer(sender.getName()).openInventory(this.main.getManagerHandler().getInventoryManager().getPreviewInventory().get(targetUUID));
        return false;
	}

}

package kezukdev.akyto.handler.command;

import java.util.UUID;

import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.utils.Utils;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "You must be a player to do that");
            return false;
        }

        final Player playerSender = (Player) sender;

		if (args.length != 1) {
			sender.sendMessage(ChatColor.GRAY + " * " + ChatColor.RED + "/" + cmd.getName() + " <player>");
			return false;
		}

        final Profile senderProfile = this.main.getManagerHandler().getProfileManager().getProfiles().get(playerSender.getUniqueId());

        if ((senderProfile.getProfileState().equals(ProfileState.FIGHT) && !Utils.getDuelByUUID(playerSender.getUniqueId()).getState().equals(DuelState.FINISHING))
                || senderProfile.getProfileState().equals(ProfileState.EDITOR)) {
            sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
            return false;
        }

        final UUID targetUUID = Utils.getUUID(args[0]);

        if (this.main.getManagerHandler().getInventoryManager().getPreviewInventory().get(targetUUID) == null) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " inventory does not exist.");
            return false;
        }

        playerSender.openInventory(this.main.getManagerHandler().getInventoryManager().getPreviewInventory().get(targetUUID));
        return false;
	}
}

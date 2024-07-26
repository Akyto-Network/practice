package akyto.practice.handler.command;

import java.util.UUID;

import akyto.practice.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.cache.DuelState;
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

        final Profile senderProfile = Utils.getProfiles(playerSender.getUniqueId());

        if ((senderProfile.isInState(ProfileState.FIGHT, ProfileState.EDITOR) && !Utils.getDuelByUUID(playerSender.getUniqueId()).getState().equals(DuelState.FINISHING))) {
            sender.sendMessage(ChatColor.RED + "You cannot do this right now!");
            return false;
        }

        String name = args[0];
        if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsValue(args[0])) {
            sender.sendMessage(org.bukkit.ChatColor.RED + args[0] + " not found on akyto.");
            return false;
        }
        if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsKey(args[0])) {
            name = Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().get(args[0]);
        }
        final UUID targetUUID = CoreUtils.getUUID(name);

        if (this.main.getManagerHandler().getInventoryManager().getPreviewInventory().get(targetUUID) == null) {
            sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.RED + " inventory does not exist.");
            return false;
        }

        playerSender.openInventory(this.main.getManagerHandler().getInventoryManager().getPreviewInventory().get(targetUUID));
        return false;
	}
}

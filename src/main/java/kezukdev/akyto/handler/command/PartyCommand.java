package kezukdev.akyto.handler.command;

import kezukdev.akyto.handler.manager.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {
	
	private final Practice main;
	private final PartyManager partyManager;
	private final static String[] usageMessage = new String[] {
		ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
				ChatColor.GRAY + "/party create",
				ChatColor.GRAY + "/party invite <player>",
				ChatColor.GRAY + "/party join <player>",
				ChatColor.GRAY + "/party kick <player>",
				ChatColor.GRAY + "/party leave",
				ChatColor.GRAY + "/party open",
				ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
	};
	
	public PartyCommand(Practice practice) {
		this.main = practice;
		this.partyManager = practice.getManagerHandler().getPartyManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(org.bukkit.ChatColor.RED + "You must be a player to do that");
			return false;
		}

		final Player playerSender = (Player) sender;

		if (args.length == 0) {
			sender.sendMessage(usageMessage);
			return false;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("create")) {
				partyManager.createParty(playerSender.getUniqueId());
				return false;
			}

			if (args[0].equalsIgnoreCase("leave")) {
				partyManager.leaveParty(playerSender.getUniqueId());
				return false;
			}

			if (args[0].equalsIgnoreCase("open")) {
				final PartyManager.PartyEntry senderParty = this.partyManager.getPartyByUUID(playerSender.getUniqueId());

				if (senderParty == null) {
					sender.sendMessage(ChatColor.RED + "You must be in a party to do this!");
					return false;
				}

                if (!senderParty.getCreator().equals(playerSender.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + "You're not the leader of that party!");
                    return false;
                }

                senderParty.setOpen(!senderParty.isOpen());
                sender.sendMessage(senderParty.isOpen() ? ChatColor.GREEN + "You've just opened your party to the public!" : ChatColor.RED + "You've just closed your party to the public!");
                return false;
            }
		}
		else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("invite")) {
				final PartyManager.PartyEntry senderParty = this.partyManager.getPartyByUUID(playerSender.getUniqueId());

				if (senderParty == null) {
					sender.sendMessage(ChatColor.RED + "You must be in a party to do this!");
					return false;
				}

				final Player target = Bukkit.getPlayer(args[1]);

				if (target == null) {
					sender.sendMessage(ChatColor.RED + args[1] + " is not online.");
					return false;
				}

				if (target.getUniqueId() == playerSender.getUniqueId()) {
					sender.sendMessage(ChatColor.RED + "You cannot invite yourself!");
					return false;
				}

				final PartyManager.PartyEntry targetParty = this.partyManager.getPartyByUUID(target.getUniqueId());

				if (targetParty != null) {
					sender.sendMessage(ChatColor.RED + "You cannot invite him because he(r) is already in other party!");
					return false;
				}

				final UUID request = this.main.getManagerHandler().getRequestManager().getPartyRequest().get(target.getUniqueId());

				if (request != null && request.equals(playerSender.getUniqueId())) {
					sender.sendMessage(ChatColor.RED + "You can't invite " + target.getDisplayName() + " because you've already invited him!");
				}

                this.main.getManagerHandler().getRequestManager().createPartyRequest(playerSender.getUniqueId(), target.getUniqueId());
                return false;
            }

			if (args[0].equalsIgnoreCase("kick")) {
				partyManager.kickParty(playerSender.getUniqueId(), Bukkit.getPlayer(args[1]).getUniqueId());
				return false;
			}

			if (args[0].equalsIgnoreCase("join")) {
				final Player target = Bukkit.getPlayer(args[1]);

				if (target == null) {
					sender.sendMessage(ChatColor.RED + "This player is not online");
					return false;
				}

				if (target.getUniqueId().equals(playerSender.getUniqueId())) {
					sender.sendMessage(ChatColor.RED + "You cannot join your party!");
					return false;
				}

				final PartyManager.PartyEntry targetParty = this.partyManager.getPartyByUUID(target.getUniqueId());

				if (targetParty == null) {
					sender.sendMessage(ChatColor.RED + "This player is not in a party!");
					return false;
				}

				if (this.partyManager.getPartyByUUID(playerSender.getUniqueId()) != null) {
					sender.sendMessage(ChatColor.RED + "You cannot do this because you are already in a party!");
					return false;
				}

                if (targetParty.isOpen()) {
                    partyManager.joinParty(target.getUniqueId(), playerSender.getUniqueId());
                    return false;
                }

				final UUID inviter = this.main.getManagerHandler().getRequestManager().getPartyRequest().get(target.getUniqueId());

				if (inviter == null || !inviter.equals(playerSender.getUniqueId())) {
					sender.sendMessage(ChatColor.RED + "You doesn't have any party invitation from " + target.getDisplayName() + " !");
					return false;
				}

                partyManager.joinParty(target.getUniqueId(), playerSender.getUniqueId());
                this.main.getManagerHandler().getRequestManager().removeRequest(target.getUniqueId());
                return false;
            }
		}
		return false;
	}
}
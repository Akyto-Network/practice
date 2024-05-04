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
	
	public PartyCommand(Practice practice) { this.main = practice; }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		final Player playerSender = (Player) sender;

		String[] usageMessage = new String[] {
				ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
				ChatColor.GRAY + "/party create",
				ChatColor.GRAY + "/party invite <player>",
				ChatColor.GRAY + "/party join <player>",
				ChatColor.GRAY + "/party kick <player>",
				ChatColor.GRAY + "/party leave",
				ChatColor.GRAY + "/party open",
				ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------",
		};
		if (args.length == 0) {
			sender.sendMessage(usageMessage);
			return false;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("create")) {
				this.main.getManagerHandler().getPartyManager().createParty(playerSender.getUniqueId());
			}
			if (args[0].equalsIgnoreCase("leave")) {
				this.main.getManagerHandler().getPartyManager().leaveParty(playerSender.getUniqueId());
			}
			if (args[0].equalsIgnoreCase("open")) {
				if (this.main.getUtils().getPartyByUUID(playerSender.getUniqueId()) != null) {
					if (!this.main.getUtils().getPartyByUUID(playerSender.getUniqueId()).getCreator().equals(playerSender.getUniqueId())) {
						sender.sendMessage(ChatColor.RED + "You're not the leader of that party!");
						return false;
					}
					this.main.getUtils().getPartyByUUID(playerSender.getUniqueId()).setOpen(!this.main.getUtils().getPartyByUUID(playerSender.getUniqueId()).isOpen());
					sender.sendMessage(this.main.getUtils().getPartyByUUID(playerSender.getUniqueId()).isOpen() ? ChatColor.GREEN + "You've just opened your party to the public!" : ChatColor.RED + "You've just closed your party to the public!");
					return false;
				}
				sender.sendMessage(ChatColor.RED + "You cannot do this!");
			}
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("invite")) {
				final PartyManager.PartyEntry senderParty = this.main.getUtils().getPartyByUUID(playerSender.getUniqueId());

				if (senderParty == null) {
					sender.sendMessage(ChatColor.RED + "You must be in a party to do this!");
					return false;
				}

				final Player target = Bukkit.getPlayer(args[1]);

				if (target == null) {
					sender.sendMessage(ChatColor.RED + args[1]+ "'s is not online.");
					return false;
				}

				if (target.getUniqueId() == playerSender.getUniqueId()) {
					sender.sendMessage(ChatColor.RED + "You cannot invite yourself!");
					return false;
				}

				final PartyManager.PartyEntry targetParty = this.main.getUtils().getPartyByUUID(target.getUniqueId());

				if (targetParty != null) {
					sender.sendMessage(ChatColor.RED + "You cannot invite him because he(r) is already in other party!");
					return false;
				}

				final UUID request = this.main.getManagerHandler().getRequestManager().getPartyRequest().get(target.getUniqueId());

				if (request != null && request.equals(playerSender.getUniqueId())) {
					sender.sendMessage(ChatColor.RED + "You can't invite " + args[1] + " because you've already invited him!");
				}

                this.main.getManagerHandler().getRequestManager().createPartyRequest(playerSender.getUniqueId(), target.getUniqueId());
                return false;
            }

			if (args[0].equalsIgnoreCase("kick")) {
				this.main.getManagerHandler().getPartyManager().kickParty(playerSender.getUniqueId(), Bukkit.getPlayer(args[1]).getUniqueId());
			}

			if (args[0].equalsIgnoreCase("join")) {
				if (Bukkit.getPlayer(args[1]) == null) {
					sender.sendMessage(ChatColor.RED + "This player is not online");
					return false;
				}
				if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()) == null) {
					sender.sendMessage(ChatColor.RED + "This player is not in any party at this time!");
					return false;
				}
				if (args[1].equals(sender.getName())) {
					sender.sendMessage(ChatColor.RED + "You cannot join your party!");
					return false;
				}
				if (this.main.getUtils().getPartyByUUID(playerSender.getUniqueId()) != null) {
					sender.sendMessage(ChatColor.RED + "You cannot do this because you are already in party!");
					return false;
				}
				if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()) != null) {
					if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()).isOpen()) {
						this.main.getManagerHandler().getPartyManager().joinParty(Bukkit.getPlayer(args[1]).getUniqueId(), playerSender.getUniqueId());
						return false;
					}
					if (!this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()).isOpen()) {
						if (this.main.getManagerHandler().getRequestManager().getPartyRequest().containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
							if (!this.main.getManagerHandler().getRequestManager().getPartyRequest().get(Bukkit.getPlayer(args[1]).getUniqueId()).equals(playerSender.getUniqueId())) {
								sender.sendMessage(ChatColor.RED + "You doesn't have any party invitation from " + args[1] + " !");
							}
						}
						this.main.getManagerHandler().getPartyManager().joinParty(Bukkit.getPlayer(args[1]).getUniqueId(), playerSender.getUniqueId());
						this.main.getManagerHandler().getRequestManager().removeRequest(Bukkit.getPlayer(args[1]).getUniqueId());
						return false;
					}
				}
			}
		}
		return false;
	}

}
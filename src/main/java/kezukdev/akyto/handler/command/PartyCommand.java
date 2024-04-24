package kezukdev.akyto.handler.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;

public class PartyCommand implements CommandExecutor {
	
	private Practice main;
	
	public PartyCommand(Practice practice) { this.main = practice; }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
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
				this.main.getManagerHandler().getPartyManager().createParty(Bukkit.getPlayer(sender.getName()).getUniqueId());
			}
			if (args[0].equalsIgnoreCase("leave")) {
				this.main.getManagerHandler().getPartyManager().leaveParty(Bukkit.getPlayer(sender.getName()).getUniqueId());
			}
			if (args[0].equalsIgnoreCase("open")) {
				if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()) != null) {
					if (!this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()).getCreator().equals(Bukkit.getPlayer(sender.getName()).getUniqueId())) {
						sender.sendMessage(ChatColor.RED + "You're not the leader of that party!");
						return false;
					}
					this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()).setOpen(this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()).isOpen() ? false : true);
					sender.sendMessage(this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()).isOpen() ? ChatColor.GREEN + "You've just opened your party to the public!" : ChatColor.RED + "You've just closed your party to the public!");
					return false;
				}
				sender.sendMessage(ChatColor.RED + "You cannot do this!");
			}
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("invite")) {
				if (args[1].equals(sender.getName())) {
					sender.sendMessage(ChatColor.RED + "You cannot invite yourself!");
					return false;
				}
				if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()) == null) {
					sender.sendMessage(ChatColor.RED + "You cannot invite him because he(r) your not in any party!");
					return false;
				}
				if (Bukkit.getPlayer(args[1]) == null) {
					sender.sendMessage(ChatColor.RED + args[1]+ "'s is not online on akyto.");
					return false;
				}
				if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()) != null) {
					sender.sendMessage(ChatColor.RED + "You cannot invite him because he(r) is already in other party!");
					return false;
				}
				if (Bukkit.getPlayer(args[1]) != null) {
					if (this.main.getManagerHandler().getRequestManager().getPartyRequest().containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
						if (this.main.getManagerHandler().getRequestManager().getPartyRequest().get(Bukkit.getPlayer(args[1]).getUniqueId()).equals(Bukkit.getPlayer(sender.getName()).getUniqueId())) {
							sender.sendMessage(ChatColor.RED + "You can't invitate " + args[1] + " because you've already invited him!!");
						}
					}
					this.main.getManagerHandler().getRequestManager().createPartyRequest(Bukkit.getPlayer(sender.getName()).getUniqueId(), Bukkit.getPlayer(args[1]).getUniqueId());
					return false;
				}
				sender.sendMessage(ChatColor.RED + "No player found.");
				return false;
			}
			if (args[0].equalsIgnoreCase("kick")) {
				this.main.getManagerHandler().getPartyManager().kickParty(Bukkit.getPlayer(sender.getName()).getUniqueId(), Bukkit.getPlayer(args[1]).getUniqueId());
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
				if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(sender.getName()).getUniqueId()) != null) {
					sender.sendMessage(ChatColor.RED + "You cannot do this because you are already in party!");
					return false;
				}
				if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()) != null) {
					if (this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()).isOpen()) {
						this.main.getManagerHandler().getPartyManager().joinParty(Bukkit.getPlayer(args[1]).getUniqueId(), Bukkit.getPlayer(sender.getName()).getUniqueId());
						return false;
					}
					if (!this.main.getUtils().getPartyByUUID(Bukkit.getPlayer(args[1]).getUniqueId()).isOpen()) {
						if (this.main.getManagerHandler().getRequestManager().getPartyRequest().containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
							if (!this.main.getManagerHandler().getRequestManager().getPartyRequest().get(Bukkit.getPlayer(args[1]).getUniqueId()).equals(Bukkit.getPlayer(sender.getName()).getUniqueId())) {
								sender.sendMessage(ChatColor.RED + "You doesn't have any party invitation from " + args[1] + " !");
							}
						}
						this.main.getManagerHandler().getPartyManager().joinParty(Bukkit.getPlayer(args[1]).getUniqueId(), Bukkit.getPlayer(sender.getName()).getUniqueId());
						this.main.getManagerHandler().getRequestManager().removeRequest(Bukkit.getPlayer(args[1]).getUniqueId());
						return false;
					}
				}
			}
		}
		return false;
	}

}
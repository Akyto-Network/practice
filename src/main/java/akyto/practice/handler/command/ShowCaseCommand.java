package akyto.practice.handler.command;

import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.utils.NPCUtils;
import akyto.practice.utils.Utils;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShowCaseCommand implements CommandExecutor {
    private final Practice main;
    private final HashMap<UUID, EntityPlayer> showcasing = new HashMap<>();

    public ShowCaseCommand(final Practice main) {
        this.main = main;
//        main.registerListeners(new Listener() {
//            @EventHandler
//            public void onPlayerInteract(PlayerInteractEvent event) {
//                Player player = event.getPlayer();
//
//                if (showcasing.contains(player.getUniqueId()) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
//                    event.setUseInteractedBlock(Event.Result.DENY);
//                    event.setUseItemInHand(Event.Result.DENY);
//                    event.setCancelled(true);
//                    if (event.getClickedBlock().getType().equals(Material.TRAP_DOOR)) {
//                        player.sendMessage(net.md_5.bungee.api.ChatColor.GREEN + "You've returned to the spawn");
//                        showcasing.remove(player.getUniqueId());
//                        Utils.sendToSpawn(player.getUniqueId(), true);
//                    }
//                }
//            }
//        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to do that");
            return false;
        }
        final Player player = (Player) sender;
        if (!Utils.getProfiles(player.getUniqueId()).isInState(ProfileState.FREE) || Utils.getPartyByUUID(player.getUniqueId()) != null) {
            sender.sendMessage(ChatColor.RED + "You cannot do that right now!");
            return false;
        }

        if (showcasing.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "You've returned to the spawn");
            EntityPlayer npc = showcasing.get(player.getUniqueId());
            NPCUtils.removeNPC(player, List.of(npc));
            showcasing.remove(player.getUniqueId());
            Utils.sendToSpawn(player.getUniqueId(), true);
            return false;
        }

        Location showcase = main.showcase.getLocation();

        if (showcase == null) {
            showcase = main.spawn.getLocation();
        }
        if (showcase == null) {
            sender.sendMessage(ChatColor.RED + "Resource pack showcase is not available at the moment.");
            return false;
        }

        EntityPlayer npc = NPCUtils.createNPC(player.getUniqueId(), "Akyto", showcase);
        NPCUtils.spawnNPCs(player, List.of(npc), true);
        showcasing.put(player.getUniqueId(), npc);
        player.teleport(showcase);
        player.getInventory().clear();
        player.sendMessage(ChatColor.GREEN + "You are in pack showcase room. Use /showcase again to leave.");
        return false;
    }
}

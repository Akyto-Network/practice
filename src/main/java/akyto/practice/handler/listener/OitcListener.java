package akyto.practice.handler.listener;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.event.EventState;
import akyto.practice.handler.manager.EventManager;
import akyto.practice.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class OitcListener implements Listener {

    public Practice main;

    public OitcListener(final Practice main) {
        this.main = main;
    }

    @EventHandler
    public void onDamageByAnotherEntity(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            final Profile victimProfile = Utils.getProfiles(event.getEntity().getUniqueId());
            final Profile profileDamager = Utils.getProfiles(event.getDamager().getUniqueId());
            if (profileDamager.isInState(ProfileState.EVENT) && victimProfile.isInState(ProfileState.EVENT)) {
                final EventManager eventManager = this.main.getManagerHandler().getEventManager();
                if (eventManager.getEventState().equals(EventState.FIGHT)) {
                    if (eventManager.getType().equals("oitc")) {
                        if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                            event.setDamage(20.0f);
                            victimProfile.setDeath(victimProfile.getDeath()+1);
                            profileDamager.setKill(victimProfile.getKill()+1);
                            eventManager.getMembers().forEach(member -> {
                                if (Bukkit.getPlayer(member) != null) {
                                    final Player player = Bukkit.getPlayer(member);
                                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "OITC" + ChatColor.GRAY + "] " + ChatColor.GREEN + event.getDamager().getName() + ChatColor.GRAY + " shot " + ChatColor.RED + event.getEntity().getName());
                                }
                            });
                            if (Bukkit.getPlayer(event.getDamager().getUniqueId()) != null) {
                                final Player player = Bukkit.getPlayer(event.getDamager().getUniqueId());
                                player.getInventory().addItem(new ItemStack(Material.ARROW));
                                player.updateInventory();
                            }
                            if (profileDamager.getKill() >= 45) {
                                eventManager.endMatch(event.getDamager().getUniqueId(), event.getEntity().getUniqueId());
                            }
                            return;
                        }
                        event.setDamage(2.5f);
                        return;
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        final Player killed = event.getEntity();
        final Player killer = killed.getKiller();
        event.setDeathMessage(null);
        event.getDrops().clear();

        final Profile victimProfile = Utils.getProfiles(killed.getUniqueId());
        final Profile profileDamager = Utils.getProfiles(killer.getUniqueId());
        if (victimProfile.isInState(ProfileState.EVENT) && profileDamager.isInState(ProfileState.EVENT)) {
            final EventManager eventManager = this.main.getManagerHandler().getEventManager();
            if (eventManager.getType().equals("oitc") && eventManager.getEventState().equals(EventState.FIGHT)) {
                if (!event.getEntity().getLastDamageCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                    victimProfile.setDeath(victimProfile.getDeath()+1);
                    profileDamager.setKill(profileDamager.getKill()+1);
                    if (profileDamager.getKill() >= 45) {
                        eventManager.endMatch(killer.getUniqueId(), killed.getUniqueId());
                    }
                    eventManager.getMembers().forEach(member -> {
                        if (Bukkit.getPlayer(member) != null) {
                            final Player player = Bukkit.getPlayer(member);
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "OITC" + ChatColor.GRAY + "] " + ChatColor.GREEN + killer.getName() + ChatColor.GRAY + " slashed " + ChatColor.RED + killed.getName());
                        }
                    });
                }
                killed.spigot().respawn();
            }
        }
    }
}

package akyto.practice.utils.timer.impl;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.utils.Utils;
import akyto.practice.utils.timer.PlayerTimer;
import com.bizarrealex.aether.scoreboard.Board;
import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class EnderpearlTimer extends PlayerTimer implements Listener {
    public EnderpearlTimer() {
        super("Enderpearl", TimeUnit.SECONDS.toMillis(15L));
    }

    @Override
    protected void handleExpiry(Player player, UUID playerUUID) {
        super.handleExpiry(player, playerUUID);
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "You can reuse your enderpearl now!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && event.hasItem()) {
            Player player = event.getPlayer();
            final Profile profile = Utils.getProfiles(player.getUniqueId());
            if (profile.isInState(ProfileState.FIGHT, ProfileState.EDITOR)) {
                if (profile.isInState(ProfileState.EDITOR)) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setCancelled(true);
                    return;
                }
                final Duel duel = Utils.getDuelByUUID(event.getPlayer().getUniqueId());
                if (duel.getState().equals(DuelState.PLAYING)) {
                    if (event.getItem().getType() == Material.ENDER_PEARL) {
                        long cooldown = this.getRemaining(player);
                        if (cooldown > 0L) {
                            event.setCancelled(true);
                            player.sendMessage(
                                    ChatColor.RED
                                            + "You cannot use your enderpearl for another "
                                            + DurationFormatUtils.formatDurationWords(cooldown, true, true).replace(" ", ChatColor.RESET.toString() + ChatColor.RED + " ")
                                            + " !"
                            );
                            player.updateInventory();
                        }
                        new BoardCooldown(Objects.requireNonNull(Board.getByPlayer(player)), "enderpearl", 16.0);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof EnderPearl) {
            Player player = (Player)event.getEntity().getShooter();
            this.setCooldown(player, player.getUniqueId());
        }
    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            if (this.getRemaining(player) != 0L && event.isCancelled()) {
                this.clearCooldown(player);
            }
        }
    }
}

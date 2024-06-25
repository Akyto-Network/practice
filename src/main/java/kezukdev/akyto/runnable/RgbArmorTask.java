package kezukdev.akyto.runnable;

import kezukdev.akyto.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class RgbArmorTask implements Runnable {
    private static final Color[] rgb = new Color[64];

    static {
        final double f = (6.48 / (double) rgb.length);
        for (int i = 0; i < rgb.length; ++i) {
            double r = Math.sin(f * i + 0.0D) * 127.0D + 128.0D;
            double g = Math.sin(f * i + (2 * Math.PI / 3)) * 127.0D + 128.0D;
            double b = Math.sin(f * i + (4 * Math.PI / 3)) * 127.0D + 128.0D;
            rgb[i] = Color.fromRGB((int) r, (int) g, (int) b);
        }
    }

    private int i = 0;
    private final LivingEntity entity;
    private final ItemStack[] previousArmorContent;
    @Setter
    private BukkitTask task = null;
    @Getter
    private static final HashSet<UUID> armored = new HashSet<>();

    public RgbArmorTask(final LivingEntity entity) {
        if (entity == null)
            throw new RuntimeException("Cannot apply rgb armor to null entity.");

        this.entity = entity;
        this.previousArmorContent = Arrays.copyOf(entity.getEquipment().getArmorContents(), 4);

        final ItemStack[] armor = Arrays.asList(
                new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_HELMET))
                .toArray(new ItemStack[4]);

        entity.getEquipment().setArmorContents(null);
        entity.getEquipment().setArmorContents(armor);
        armored.add(entity.getUniqueId());
    }

    public void stopTask() {
        if (this.task != null)
            this.task.cancel();
        this.entity.getEquipment().setArmorContents(this.previousArmorContent);
        armored.remove(this.entity.getUniqueId());
    }

    @Override
    public void run() {

        final Profile profile = Utils.getProfiles(this.entity.getUniqueId());

        if (this.entity.isDead() || profile == null || profile.isInState(ProfileState.FIGHT, ProfileState.EDITOR)) {
            this.stopTask();
            return;
        }

        boolean any = false;
        for (ItemStack it : this.entity.getEquipment().getArmorContents()) {
            if (it == null || !(it.getItemMeta() instanceof LeatherArmorMeta))
                continue;
            any = true;

            LeatherArmorMeta lam = (LeatherArmorMeta) it.getItemMeta();
            lam.setColor(rgb[i]);
            it.setItemMeta(lam);
        }

        if (!any) {
            this.stopTask();
            return;
        }

        if (++i == rgb.length) i = 0;
    }
}
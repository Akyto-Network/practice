package kezukdev.akyto.runnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class RgbArmorTask extends BukkitRunnable {
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

    public RgbArmorTask(LivingEntity entity) {
        if (entity == null)
            throw new RuntimeException("Cannot apply rgb armor to null entity.");

        ItemStack[] armor = Arrays.asList(
                new ItemStack(Material.LEATHER_HELMET),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_BOOTS))
                .toArray(new ItemStack[4]);

        entity.getEquipment().clear();
        entity.getEquipment().setArmorContents(armor);

        this.entity = entity;
    }

    @Override
    public void run() {
        Profile profile = Practice.getAPI().getManagerHandler().getProfileManager().getProfiles().get(this.entity.getUniqueId());
        if (this.entity.isDead() || profile == null || !profile.getProfileState().equals(ProfileState.FREE)) {
            this.cancel();
            return;
        }

        boolean any = false;
        for (ItemStack it : this.entity.getEquipment().getArmorContents()) {
            if (it == null || it.getType().name().startsWith("LEATHER_"))
                continue;
            any = true;

            LeatherArmorMeta lam = (LeatherArmorMeta) it.getItemMeta();
            lam.setColor(rgb[i]);
            it.setItemMeta(lam);
        }

        if (!any) {
            this.cancel();
            return;
        }

        if (++i == rgb.length) i = 0;
    }
}
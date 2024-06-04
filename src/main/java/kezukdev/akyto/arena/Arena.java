package kezukdev.akyto.arena;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.*;

import kezukdev.akyto.Practice;
import kezukdev.akyto.utils.location.LocationSerializer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;

@Getter
public class Arena {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Arena.class);
    private final String name;
    private final List<LocationSerializer> position;
    @Setter
    private ArenaType arenaType;
    @Setter
    private Material icon;

    public Arena(final Practice main, final String name, final LocationSerializer loc1, final LocationSerializer loc2, final ArenaType arenaType, final Material icon) {
        this.name = name;
        position = Arrays.asList(loc1, loc2);
        this.arenaType = arenaType;
        this.icon = icon;
        main.getArenas().add(this);
        loadChunks();
    }

    private void loadChunks() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Loading chunks for arena " + this.name + "...");
        LocationSerializer a = this.position.get(0);
        LocationSerializer b = this.position.get(1);
        World world = a.toBukkitWorld();

        int minX = (int) Math.min(a.getX(), b.getX());
        int maxX = (int) Math.max(a.getX(), b.getX());
        int minZ = (int) Math.min(a.getZ(), b.getZ());
        int maxZ = (int) Math.max(a.getZ(), b.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.loadChunk(x, z, false);
            }
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded chunks for arena " + this.name + "!");
    }
}

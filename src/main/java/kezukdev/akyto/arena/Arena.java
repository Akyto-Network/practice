package kezukdev.akyto.arena;

import java.util.Arrays;
import java.util.List;

import org.bukkit.*;

import akyto.core.utils.location.LocationSerializer;
import kezukdev.akyto.Practice;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Arena {

    private final String name;
    private final List<LocationSerializer> position;
    private ArenaType arenaType;
    private Material icon;
    private LocationSerializer corner1;
    private LocationSerializer corner2;
    private boolean save;

    public Arena(final Practice main, final String name, final LocationSerializer loc1, final LocationSerializer loc2, final ArenaType arenaType, final Material icon) {
        this.name = name;
        position = Arrays.asList(loc1, loc2);
        this.arenaType = arenaType;
        this.icon = icon;
        main.getArenas().add(this);
    }

    public void loadChunks() throws Exception {
//        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Loading chunks for arena " + this.name + "...");
        if (!hasCorners())
            throw new Exception("Corners are not set");

        LocationSerializer a = corner1;
        LocationSerializer b = corner2;
        World world = a.toBukkitWorld();

        int minX = (int) Math.min(a.getX(), b.getX());
        int maxX = (int) Math.max(a.getX(), b.getX());
        int minZ = (int) Math.min(a.getZ(), b.getZ());
        int maxZ = (int) Math.max(a.getZ(), b.getZ());

        // TODO Can async load ?
        // += 1 to += 16 ?
        for (int x = minX; x <= maxX; x += 1) {
            for (int z = minZ; z <= maxZ; z += 1) {
                Chunk chunk = world.getChunkAt(x, z);
                if (chunk.isLoaded())
                    continue;
                boolean success = chunk.load();
                if (Practice.getAPI().isDebug()) {
                    System.out.printf((success ? "Successful" : "Unsuccessful") + " chunk load " + (this.save ? "with save" : "") + " at x=%d z=%d", x, z);
//                    if (!success)
//                        throw new ChunkLoadException(x, z, "Failed to load chunk x=" + x + " z=" + z);
                }
            }
        }
//        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded chunks for arena " + this.name + "!");
    }

    public void unloadChunks() throws Exception {
//        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Unloading chunks for arena " + this.name + "...");
        if (!hasCorners())
            throw new Exception("Corners are not set");

        LocationSerializer a = corner1;
        LocationSerializer b = corner2;
        World world = a.toBukkitWorld();

        int minX = (int) Math.min(a.getX(), b.getX());
        int maxX = (int) Math.max(a.getX(), b.getX());
        int minZ = (int) Math.min(a.getZ(), b.getZ());
        int maxZ = (int) Math.max(a.getZ(), b.getZ());

        // TODO Can async unload ?
        // += 1 to += 16 ?
        for (int x = minX; x <= maxX; x += 1) {
            for (int z = minZ; z <= maxZ; z += 1) {
                Chunk chunk = world.getChunkAt(x, z);
                if (!save && !chunk.isLoaded())
                    continue;
                boolean success = chunk.unload(this.save);
                if (Practice.getAPI().isDebug()) {
                    System.out.printf((success ? "Successful" : "Unsuccessful") + " chunk unload at x=%d z=%d", x, z);
//                    if (!success)
//                        throw new ChunkLoadException(x, z, "Failed to unload chunk x=" + x + " z=" + z);
                }
            }
        }
//        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Unloaded chunks for arena " + this.name + "!");
    }

    public boolean hasCorners() {
        return corner1 != null && corner2 != null;
    }

    @Getter
    public static class ChunkLoadException extends Exception {
        private final int x;
        private final int z;

        public ChunkLoadException(int x, int z, String message) {
            super(message);
            this.x = x;
            this.z = z;
        }
    }
}

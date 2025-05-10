package akyto.practice.handler.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import akyto.practice.arena.ArenaCopy;
import akyto.practice.duel.Duel;
import akyto.practice.kit.Kit;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import akyto.core.utils.config.Config;
import akyto.core.utils.location.LocationSerializer;
import akyto.practice.Practice;
import akyto.practice.arena.Arena;
import akyto.practice.arena.ArenaType;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class ArenaManager {
	
	private final Practice main;
	private final Config config;
	private final Random rng = new Random();
	
	public ArenaManager(final Practice main) { 
		this.main = main;
		this.config = new Config("arenas", this.main);
		this.loadArenas();
	}

	private void loadArenas() {
		FileConfiguration fileConfig = this.config.getConfig();
		ConfigurationSection arenaSection = fileConfig.getConfigurationSection("arenas");
		if (arenaSection == null) return;
		arenaSection.getKeys(false).forEach(name -> {
			name = name.toLowerCase();
			final Arena arena = new Arena(
					main,
					name,
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".first")),
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".second")),
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".corner1")),
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".corner2")),
					ArenaType.valueOf(arenaSection.getString(name + ".type")),
					Material.valueOf(arenaSection.getString(name + ".icon")),
					arenaSection.getBoolean(name + ".build")
			);

			this.main.getArenas().putIfAbsent(name, arena);
		});
	}

	public void saveArenas() {
		if (this.main.getArenas().isEmpty()) return;
		FileConfiguration fileConfig = this.config.getConfig();
		fileConfig.set("arenas", null);

		this.main.getArenas().forEach((arenaName, arena) -> {
			fileConfig.set("arenas." + arenaName + ".first", LocationSerializer.locationToString(arena.getPosition().get(0)));
			fileConfig.set("arenas." + arenaName + ".second", LocationSerializer.locationToString(arena.getPosition().get(1)));
			fileConfig.set("arenas." + arenaName + ".type", arena.getArenaType().toString());
			fileConfig.set("arenas." + arenaName + ".icon", arena.getIcon().toString());
			if (arena.getCorner1() != null)
				fileConfig.set("arenas." + arenaName + ".corner1", LocationSerializer.locationToString(arena.getCorner1()));
			if (arena.getCorner2() != null)
				fileConfig.set("arenas." + arenaName + ".corner2", LocationSerializer.locationToString(arena.getCorner2()));
			fileConfig.set("arenas." + arenaName + ".build", arena.isBuild());
		});
		this.config.save();
	}

	public void reloadArenas() {
		saveArenas();
		this.main.getArenas().clear();
		loadArenas();
	}

    public Arena getRandomArena(Kit kit) {
        List<Arena> availableArena = this.main.getArenas().values().stream().filter(arenaManager -> arenaManager.getArenaType() == kit.arenaType() || arenaManager.isBuild() == kit.buildable()).toList();
        return availableArena.isEmpty() ? null : availableArena.get(rng.nextInt(availableArena.size()));
    }

	public Arena getArena(String name) {
		return this.main.getArenas().get(name.toLowerCase());
	}

	public void removeCopy(ArenaCopy arena) {
		World world = arena.getPosition().getFirst().toBukkitWorld();
		int mny = min(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int mxy = max(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int mnx = min(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int mxx = max(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int mnz = min(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());
		int mxz = max(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());
		for (int y = mny ; y <= mxy; y++) {
			for (int x = mnx ; x <= mxx; x++) {
				for (int z = mnz ; z <= mxz; z++) {
					Location newBlock = new Location(world, x, y, z);
					newBlock.getBlock().setType(Material.AIR);
				}
			}
		}
		this.main.getArenaCopies().remove(arena);
	}

	public UUID createCopy(Arena arena, double addToLoc, final Duel duel) {
		UUID id = UUID.randomUUID();
		World world = arena.getPosition().getFirst().toBukkitWorld();

		int minY = Math.min(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int maxY = Math.max(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int minX = Math.min(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int maxX = Math.max(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int minZ = Math.min(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());
		int maxZ = Math.max(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());

		Bukkit.getScheduler().runTask(Practice.API, () -> {
			Block[][][] blocks = new Block[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {
						Location blockLocation = new Location(world, x, y, z);
						blocks[x - minX][y - minY][z - minZ] = blockLocation.getBlock();
					}
				}
			}

			Bukkit.getScheduler().runTask(Practice.API, () -> {
				double centerX = (arena.getPosition().getFirst().toBukkitLocation().getX() + arena.getPosition().getLast().toBukkitLocation().getX()) / 2;
				double centerZ = (arena.getPosition().getFirst().toBukkitLocation().getZ() + arena.getPosition().getLast().toBukkitLocation().getZ()) / 2;
				double xAdd = centerX + addToLoc;
				double zAdd = centerZ + addToLoc;
				Location pasteLocation = new Location(world, xAdd, 50.0d, zAdd);
				int radiusAdded = 1000;
				while (!isAreaClear(pasteLocation, 1000)) {
					xAdd += 1000;
					zAdd += 1000;
					radiusAdded += 1000;
					pasteLocation = new Location(world, xAdd, 80.0d, zAdd);
				}

				int pasteX = pasteLocation.getBlockX();
				int pasteY = pasteLocation.getBlockY();
				int pasteZ = pasteLocation.getBlockZ();

				for (int y = 0; y < blocks[0].length; y++) {
					for (int x = 0; x < blocks.length; x++) {
						for (int z = 0; z < blocks[0][0].length; z++) {
							Block block = blocks[x][y][z];
							Location newLocation = new Location(world, pasteX + x, pasteY + y, pasteZ + z);
							newLocation.getBlock().setType(block.getType());
						}
					}
				}

				final double firstXRadiusAdded = arena.getPosition().getFirst().getX() + radiusAdded;
				final double firstZRadiusAdded = arena.getPosition().getFirst().getZ() + radiusAdded;
				final LocationSerializer firstCopies = new LocationSerializer(firstXRadiusAdded, arena.getPosition().getFirst().getY(), firstZRadiusAdded);

				final double secondXRadiusAdded = arena.getPosition().getLast().getX() + radiusAdded;
				final double secondZRadiusAdded = arena.getPosition().getLast().getZ() + radiusAdded;
				final LocationSerializer secondCopies = new LocationSerializer(secondXRadiusAdded, arena.getPosition().getLast().getY(), secondZRadiusAdded);

				final List<LocationSerializer> serializers = Lists.newArrayList();
				serializers.add(firstCopies);
				serializers.add(secondCopies);

				final List<UUID> players = Lists.newArrayList();
				players.addAll(duel.getFirst());
				players.addAll(duel.getSecond());

				LocationSerializer newCorner1 = new LocationSerializer(pasteX + (minX - arena.getCorner1().toBukkitLocation().getBlockX()), pasteY, pasteZ + (minZ - arena.getCorner1().toBukkitLocation().getBlockZ()));
				LocationSerializer newCorner2 = new LocationSerializer(pasteX + (maxX - arena.getCorner1().toBukkitLocation().getBlockX()), pasteY + (maxY - minY), pasteZ + (maxZ - arena.getCorner1().toBukkitLocation().getBlockZ()));
				ArenaCopy copy = new ArenaCopy(id, serializers, players, newCorner1, newCorner2);
				this.main.getArenaCopies().add(copy);
			});
		});
		return id;
	}

	private boolean isAreaClear(Location center, int radius) {
		World world = center.getWorld();
		int centerX = center.getBlockX();
		int centerY = center.getBlockY();
		int centerZ = center.getBlockZ();

		for (int x = centerX - radius; x <= centerX + radius; x++) {
			for (int y = centerY - radius; y <= centerY + radius; y++) {
				for (int z = centerZ - radius; z <= centerZ + radius; z++) {
					Block block = world.getBlockAt(x, y, z);
					if (block.getType() != Material.AIR) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
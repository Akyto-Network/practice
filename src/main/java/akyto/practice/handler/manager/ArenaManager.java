package akyto.practice.handler.manager;

import java.util.*;

import akyto.practice.arena.ArenaCopy;
import akyto.practice.duel.Duel;
import akyto.practice.kit.Kit;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.block.Block;
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
		List<Arena> availableArena = this.main.getArenas().values().stream().filter(arenaManager -> arenaManager.getArenaType() == kit.arenaType() && kit.buildable() == arenaManager.isBuild()).toList();
		return availableArena.isEmpty() ? null : availableArena.get(rng.nextInt(availableArena.size()));
	}

	public Arena getArena(String name) {
		return this.main.getArenas().get(name.toLowerCase());
	}

	public void removeCopy(ArenaCopy arena) {
		World world = arena.getPosition().getFirst().toBukkitWorld();
		int minY = min(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int maxY = max(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int minX = min(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int maxX = max(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int minZ = min(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());
		int maxZ = max(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());
		Block[] blocks = new Block[(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)];

		int i = 0;
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocks[i] = world.getBlockAt(x, y, z);
					i++;
				}
			}
		}
		TaskManager.IMP
				.async(
						() -> {
							EditSession editSession = new EditSessionBuilder(world.getName())
									.fastmode(true)
									.allowedRegionsEverywhere()
									.autoQueue(false)
									.limitUnlimited()
									.build();

							try {
								for (Block entry : blocks) {
									editSession.setBlock(
											new Vector(entry.getX(), entry.getY(), entry.getZ()),
											new BaseBlock(0)
									);
								}
							} catch (MaxChangedBlocksException e) {
								throw new RuntimeException(e);
							}

							editSession.flushQueue();
						}
				);
		this.main.getArenaCopies().remove(arena);
	}

	public UUID createCopy(Arena arena, final Duel duel) {
		UUID id = UUID.randomUUID();
		World world = arena.getPosition().getFirst().toBukkitWorld();
		int minY = Math.min(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int maxY = Math.max(arena.getCorner1().toBukkitLocation().getBlockY(), arena.getCorner2().toBukkitLocation().getBlockY());
		int minX = Math.min(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int maxX = Math.max(arena.getCorner1().toBukkitLocation().getBlockX(), arena.getCorner2().toBukkitLocation().getBlockX());
		int minZ = Math.min(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());
		int maxZ = Math.max(arena.getCorner1().toBukkitLocation().getBlockZ(), arena.getCorner2().toBukkitLocation().getBlockZ());

		Block[] blocks = new Block[(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)];

		int i = 0;
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocks[i] = world.getBlockAt(x, y, z);
					i++;
				}
			}
		}

		int addZ = 1000;
		int threshold = 500; // Si il trouve rien au bout de 500 Area, on stop
		while (!isAreaClear(blocks, addZ) && threshold > 0) {
			addZ += 1000;
			threshold--;
		}
		if (threshold == 0) {
			return null;
		}

		int finalAddZ = addZ;
		TaskManager.IMP
				.async(
						() -> {
							EditSession editSession = new EditSessionBuilder(world.getName())
									.fastmode(true)
									.allowedRegionsEverywhere()
									.autoQueue(false)
									.limitUnlimited()
									.build();

							try {
								for (Block entry : blocks) {
									int Z = entry.getZ() + finalAddZ;
									editSession.setBlock(
											new Vector(entry.getX(), entry.getY(), Z),
											new BaseBlock(entry.getTypeId(), entry.getData())
									);
								}
							} catch (MaxChangedBlocksException e) {
								throw new RuntimeException(e);
							}

							editSession.flushQueue();
						}
				);
		double tempAddZ = addZ;
		final List<LocationSerializer> serializers = arena.getPosition().stream().map(locationSerializer ->
				locationSerializer.add(0, 0, tempAddZ)
		).toList();

		final List<UUID> players = Lists.newArrayList();
		players.addAll(duel.getFirst());
		players.addAll(duel.getSecond());

		LocationSerializer newCorner1 = arena.getCorner1().add(0, 0, tempAddZ);
		LocationSerializer newCorner2 = arena.getCorner2().add(0, 0, tempAddZ);
		ArenaCopy copy = new ArenaCopy(id, serializers, players, newCorner1, newCorner2);
		this.main.getArenaCopies().add(copy);

		return id;
	}

	private boolean isAreaClear(Block[] originalBlocks, int addZ) {
		for (Block block : originalBlocks) {
			if (block.getLocation().add(0, 0, addZ).getBlock().getType() != Material.AIR)
				return false;
		}
		return true;
	}
}
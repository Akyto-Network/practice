package akyto.practice.handler.manager;

import akyto.core.utils.config.Config;
import akyto.core.utils.location.LocationSerializer;
import akyto.practice.Practice;
import akyto.practice.arena.Arena;
import akyto.practice.arena.ArenaCopy;
import akyto.practice.arena.ArenaType;
import akyto.practice.duel.Duel;
import akyto.practice.kit.Kit;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ArenaManager {

	private final Practice main;
	private final Config config;
	private final Random rng = new Random();
	private static final int COPY_THRESHOLD = 200;

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
		long start = System.currentTimeMillis();
		TaskManager.IMP
				.async(
						() -> {
							BaseBlock airBlock = new BaseBlock(0);
							World world = arena.getPosition().getFirst().toBukkitWorld();
							double minY = min(arena.getCorner1().getY(), arena.getCorner2().getY());
							double maxY = max(arena.getCorner1().getY(), arena.getCorner2().getY());
							double minX = min(arena.getCorner1().getX(), arena.getCorner2().getX());
							double maxX = max(arena.getCorner1().getX(), arena.getCorner2().getX());
							double minZ = min(arena.getCorner1().getZ(), arena.getCorner2().getZ());
							double maxZ = max(arena.getCorner1().getZ(), arena.getCorner2().getZ());

							EditSession editSession = new EditSessionBuilder(world.getName())
									.fastmode(true)
									.allowedRegionsEverywhere()
									.autoQueue(false)
									.limitUnlimited()
									.build();

							CuboidRegion region = new CuboidRegion(editSession.getWorld(), new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));

							try {
								editSession.setBlocks(region, airBlock);
							} catch (MaxChangedBlocksException e) {
								throw new RuntimeException(e);
							}

							editSession.flushQueue();
							long end = System.currentTimeMillis();
							this.main.getLogger().info("Arena remove copy took " + (end - start) + "ms");
						}
				);
		this.main.getArenaCopies().remove(arena);
	}

	// TODO Optimize even more
	public UUID createCopy(Arena arena, final Duel duel) {
		UUID id = UUID.randomUUID();

		long start = System.currentTimeMillis();
		CompletableFuture<Void> future = new CompletableFuture<>();

		TaskManager.IMP.async(() -> {
			try {
				World world = arena.getPosition().getFirst().toBukkitWorld();
				double minY = min(arena.getCorner1().getY(), arena.getCorner2().getY());
				double maxY = max(arena.getCorner1().getY(), arena.getCorner2().getY());
				double minX = min(arena.getCorner1().getX(), arena.getCorner2().getX());
				double maxX = max(arena.getCorner1().getX(), arena.getCorner2().getX());
				double minZ = min(arena.getCorner1().getZ(), arena.getCorner2().getZ());
				double maxZ = max(arena.getCorner1().getZ(), arena.getCorner2().getZ());

				EditSession editSession = new EditSessionBuilder(world.getName())
						.fastmode(true)
						.allowedRegionsEverywhere()
						.autoQueue(false)
						.limitUnlimited()
						.build();

				int addZ = 1000;

				CuboidRegion copyRegion = new CuboidRegion(editSession.getWorld(), new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
				CuboidRegion pasteRegion = new CuboidRegion(editSession.getWorld(), new Vector(minX, minY, minZ + addZ), new Vector(maxX, maxY, maxZ + addZ));

				int area = pasteRegion.getArea();
				int i = 0;
				while (i < COPY_THRESHOLD && editSession.countBlock(pasteRegion, Set.of(0)) < area) {
					pasteRegion.setPos1(pasteRegion.getPos1().add(0, 0, addZ));
					pasteRegion.setPos2(pasteRegion.getPos2().add(0, 0, addZ));
					i++;
				}

				if (i == COPY_THRESHOLD) {
					throw new Exception("Failed to find an empty area in " + COPY_THRESHOLD + " tries");
				}

				BlockArrayClipboard clipboard = new BlockArrayClipboard(copyRegion);

				ForwardExtentCopy copyOp = new ForwardExtentCopy(editSession, copyRegion, clipboard, copyRegion.getMinimumPoint());
				Operations.complete(copyOp);

				ClipboardHolder holder = new ClipboardHolder(clipboard, editSession.getWorld().getWorldData());
				Operation pasteOp = holder.createPaste(editSession, editSession.getWorld().getWorldData())
						.to(pasteRegion.getMinimumPoint())
						.ignoreAirBlocks(false)
						.build();
				Operations.complete(pasteOp);

				editSession.flushQueue();
				long end = System.currentTimeMillis();
				this.main.getLogger().info("Arena create copy took " + (end - start) + "ms");

				final List<LocationSerializer> serializers = arena.getPosition().stream().map(locationSerializer ->
						locationSerializer.add(0, 0, addZ)
				).toList();

				final List<UUID> players = Lists.newArrayList();
				players.addAll(duel.getFirst());
				players.addAll(duel.getSecond());

				LocationSerializer newCorner1 = arena.getCorner1().add(0, 0, addZ);
				LocationSerializer newCorner2 = arena.getCorner2().add(0, 0, addZ);
				ArenaCopy copy = new ArenaCopy(id, serializers, players, newCorner1, newCorner2);
				this.main.getArenaCopies().add(copy);

				future.complete(null);
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});

		future.whenComplete((_, throwable) -> {
			if (throwable != null) {
				this.main.getLogger().severe("Failed to create arena copy: " + throwable.getMessage());
				return;
			}

			main.getManagerHandler().getDuelManager().teleport(duel, id);
		});

		return id;
	}
}
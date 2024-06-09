package kezukdev.akyto.handler.manager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.arena.ArenaType;
import kezukdev.akyto.utils.config.Config;
import kezukdev.akyto.utils.location.LocationSerializer;

public class ArenaManager {
	
	private final Practice main;
	private final Config config;
	
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
			final Arena arena = new Arena(
					main,
					name,
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".first")),
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".second")),
					ArenaType.valueOf(arenaSection.getString(name + ".type")),
					Material.valueOf(arenaSection.getString(name + ".icon"))
			);

			if (arenaSection.isSet(name + ".corner1")) {
				arena.setCorner1(LocationSerializer.stringToLocation(arenaSection.getString(name + ".corner1")));
			}

			if (arenaSection.isSet(name + ".corner2")) {
				arena.setCorner2(LocationSerializer.stringToLocation(arenaSection.getString(name + ".corner2")));
			}

			this.main.getArenasMap().putIfAbsent(name, arena);

			try {
				arena.loadChunks();
			} catch (Exception ex) {
				this.main.getLogger().severe("Failed to load chunks for arena " + arena.getName() + ": " + ex.getMessage());
			}
		});
	}

	public void saveArenas() {
		if (this.main.getArenas().isEmpty()) return;
		FileConfiguration fileConfig = this.config.getConfig();
		fileConfig.set("arenas", null);
		this.main.getArenasMap().forEach((arenaName, arena) -> {
			fileConfig.set("arenas." + arenaName + ".first", LocationSerializer.locationToString(arena.getPosition().get(0)));
			fileConfig.set("arenas." + arenaName + ".second", LocationSerializer.locationToString(arena.getPosition().get(1)));
			fileConfig.set("arenas." + arenaName + ".type", arena.getArenaType().toString());
			fileConfig.set("arenas." + arenaName + ".icon", arena.getIcon().toString());
			if (arena.getCorner1() != null)
				fileConfig.set("arenas." + arenaName + ".corner1", LocationSerializer.locationToString(arena.getCorner1()));
			if (arena.getCorner2() != null)
				fileConfig.set("arenas." + arenaName + ".corner2", LocationSerializer.locationToString(arena.getCorner2()));
		});
		this.config.save();
	}

	public void reloadArenas() {
		saveArenas();
		this.main.getArenasMap().clear();
		loadArenas();
	}

    public Arena getRandomArena(ArenaType arenaType) {
        List<Arena> availableArena = this.main.getArenas().stream().filter(arenaManager -> arenaManager.getArenaType() == arenaType).collect(Collectors.toList());
        Collections.shuffle(availableArena);
        return availableArena.isEmpty() ? null : availableArena.get(0);
    }

	public Arena getArena(String name) {
		return this.main.getArenasMap().get(name.toLowerCase());
	}
}
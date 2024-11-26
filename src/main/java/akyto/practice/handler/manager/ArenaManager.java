package akyto.practice.handler.manager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import akyto.core.utils.config.Config;
import akyto.core.utils.location.LocationSerializer;
import akyto.practice.Practice;
import akyto.practice.arena.Arena;
import akyto.practice.arena.ArenaType;

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
			name = name.toLowerCase();
			final Arena arena = new Arena(
					main,
					name,
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".first")),
					LocationSerializer.stringToLocation(arenaSection.getString(name + ".second")),
					ArenaType.valueOf(arenaSection.getString(name + ".type")),
					Material.valueOf(arenaSection.getString(name + ".icon"))
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
		});
		this.config.save();
	}

	public void reloadArenas() {
		saveArenas();
		this.main.getArenas().clear();
		loadArenas();
	}

    public Arena getRandomArena(ArenaType arenaType) {
        List<Arena> availableArena = this.main.getArenas().values().stream().filter(arenaManager -> arenaManager.getArenaType() == arenaType).collect(Collectors.toList());
        Collections.shuffle(availableArena);
        return availableArena.isEmpty() ? null : availableArena.getFirst();
    }

	public Arena getArena(String name) {
		return this.main.getArenas().get(name.toLowerCase());
	}
}
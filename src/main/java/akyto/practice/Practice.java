package akyto.practice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import akyto.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bizarrealex.aether.Aether;

import akyto.practice.arena.Arena;
import akyto.practice.board.SideBoard;
import akyto.practice.database.FileSetup;
import akyto.practice.duel.Duel;
import akyto.practice.handler.ManagerHandler;
import akyto.practice.handler.MiscHandler;
import akyto.practice.handler.manager.QueueManager.QueueEntry;
import akyto.practice.kit.Kit;
import akyto.practice.kit.gametype.Axe;
import akyto.practice.kit.gametype.Debuff;
import akyto.practice.kit.gametype.Gapple;
import akyto.practice.kit.gametype.NoDebuff;
import akyto.practice.kit.gametype.NoEnchant;
import akyto.practice.kit.gametype.Soup;
import akyto.practice.kit.gametype.Sumo;
import akyto.practice.utils.location.LocationUtil;
import lombok.Getter;

@Getter
public class Practice extends JavaPlugin {

	private static Practice api;

	private ManagerHandler managerHandler;
	private MiscHandler miscHandler;
	public File locationFile;
	public YamlConfiguration locationConfig;
	public LocationUtil spawn = new LocationUtil("spawn");
	public LocationUtil editor = new LocationUtil("editor");

	private List<Kit> kits = Arrays.asList(
			new NoDebuff(),
			new NoEnchant(),
			new Debuff(),
			new Gapple(),
			new Sumo(),
			new Soup(),
			new Axe()
	);

	private final List<Duel> duels = new ArrayList<>();
	private ConcurrentMap<UUID, QueueEntry> queue;
	private final HashMap<String, Arena> arenas = new HashMap<>();
	private FileSetup fileSetup;
	private boolean debug;

	private String[] kitNames;

	public void onEnable() {
		api = this;
		this.loadConfig();
		this.loadKit();
		this.queue = new ConcurrentHashMap<>();
		this.managerHandler = new ManagerHandler(this);
		this.miscHandler = new MiscHandler(this);
		this.fileSetup = new FileSetup(this);
		new Aether(this, new SideBoard(this));
	}

	public void onDisable() {
		if (!Bukkit.getOnlinePlayers().isEmpty()) {
			Bukkit.getOnlinePlayers().forEach(player -> {
				Core.API.getDatabaseSetup().exit(player.getUniqueId());
				player.kickPlayer(ChatColor.RED + "Restart is going on.");
			});
		}
		this.getFileSetup().saveEditor();
		LocationUtil.getAll().forEach(locationHelper -> locationHelper.save(this));
		try {
			this.managerHandler.getArenaManager().saveArenas();
			this.locationConfig.save(locationFile);
		} catch (IOException e) { e.printStackTrace(); }
	}

	private void loadConfig() {
		debug = getServer().getOptions().has("debug");
		if (debug)
			getLogger().info("Debug mode enabled");
		this.saveResource("locations.yml", false);
		this.locationFile = new File(getDataFolder() + "/locations.yml");
		this.locationConfig = YamlConfiguration.loadConfiguration(locationFile);
		for (LocationUtil locationHelper : LocationUtil.getAll()) {
			this.getServer().getConsoleSender().sendMessage(locationHelper.load(this) ? "The location " + locationHelper.getName() + " is successfully registered!" : "The location " + locationHelper.getName() + " is not registered!");
		}
	}

	private void loadKit() {
		this.kitNames = new String[this.kits.size()];
		for (Kit kit : this.kits) { kitNames[kit.id()] = kit.displayName(); }
	}

	public static Practice getAPI() {
		return api;
	}
}

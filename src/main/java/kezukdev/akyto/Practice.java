package kezukdev.akyto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bizarrealex.aether.Aether;

import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.board.SideBoard;
import kezukdev.akyto.database.FileSetup;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.handler.ManagerHandler;
import kezukdev.akyto.handler.MiscHandler;
import kezukdev.akyto.handler.manager.QueueManager.QueueEntry;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.kit.gametype.Axe;
import kezukdev.akyto.kit.gametype.Debuff;
import kezukdev.akyto.kit.gametype.Gapple;
import kezukdev.akyto.kit.gametype.NoDebuff;
import kezukdev.akyto.kit.gametype.NoEnchant;
import kezukdev.akyto.kit.gametype.Soup;
import kezukdev.akyto.kit.gametype.Sumo;
import kezukdev.akyto.utils.location.LocationUtil;
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
    private final List<Arena> arenas = new ArrayList<>();
    private final HashMap<String, Arena> arenasMap = new HashMap<>();
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
		this.saveDefaultConfig();
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

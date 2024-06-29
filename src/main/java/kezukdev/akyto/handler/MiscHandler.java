package kezukdev.akyto.handler;

import java.util.Arrays;

import kezukdev.akyto.handler.command.*;
import org.bukkit.Bukkit;

import kezukdev.akyto.Practice;
import kezukdev.akyto.handler.listener.CoreListener;
import kezukdev.akyto.handler.listener.EntityListener;
import kezukdev.akyto.handler.listener.InventoryListener;
import kezukdev.akyto.handler.listener.PlayerListener;
import org.bukkit.command.PluginCommand;

public class MiscHandler {
	
	private final Practice main;
	
	public MiscHandler(final Practice main) {
		this.main = main;
		Arrays.asList(new PlayerListener(main), new EntityListener(main), new InventoryListener(main), new CoreListener()).forEach(list -> Bukkit.getPluginManager().registerEvents(list, this.main));

		PluginCommand arenaCommand = main.getCommand("arena");
		arenaCommand.setExecutor(new ArenaCommand(main));
		arenaCommand.setTabCompleter(new ArenaCommand.Completer());
		main.getCommand("locations").setExecutor(new LocationsCommand());
		main.getCommand("inventory").setExecutor(new InventoryCommand(main));
		main.getCommand("spectate").setExecutor(new SpectateCommand(main));
		main.getCommand("duel").setExecutor(new DuelCommand(main));
		main.getCommand("stats").setExecutor(new StatisticsCommand(main));
		main.getCommand("leaderboard").setExecutor(new LeaderboardCommand(main));
		main.getCommand("party").setExecutor(new PartyCommand(main));
		main.getCommand("reset").setExecutor(new ResetCommand());
	}

}

package kezukdev.akyto.database;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import kezukdev.akyto.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import co.aikar.idb.BukkitDB;
import co.aikar.idb.DB;
import gym.core.utils.format.FormatUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.profile.Profile;

public class DatabaseSetup {
	
	private final Practice main;
	
	public DatabaseSetup(final Practice main) {
		this.main = main;
		this.setupHikariCP();
		this.setupDatabase();
		this.main.getFileSetup().loadEditor();
	}
	
    private void setupDatabase() {
        if (this.main.connection != null) {
        	this.main.getMySQL().createPlayerManagerTableAsync();
            return;
        }
        System.out.println("WARNING enter valid database information (" + this.main.getHikariPath() + ") \n You will not be able to access many features");
    }
    
    public void setupHikariCP() {
        try {
            final HikariConfig config = new HikariConfig(this.main.getHikariPath());
            @SuppressWarnings("resource")
			final HikariDataSource ds = new HikariDataSource(config);
            final String passwd = (config.getDataSourceProperties().getProperty("password") == null) ? "" : config.getDataSourceProperties().getProperty("password");
            BukkitDB.createHikariDatabase(this.main, config.getDataSourceProperties().getProperty("user"), passwd, config.getDataSourceProperties().getProperty("databaseName"), config.getDataSourceProperties().getProperty("serverName") + ":" + config.getDataSourceProperties().getProperty("portNumber"));
            this.main.connection = ds.getConnection();
        }
        catch (SQLException e) {
            System.out.println("Error could not connect to SQL database.");
            e.printStackTrace();
        }
        System.out.println("Successfully connected to the SQL database.");
    }

	
	public void closeConnection() {
		this.main.getFileSetup().saveEditor();
		if (!Bukkit.getOnlinePlayers().isEmpty()) {
			Bukkit.getOnlinePlayers().forEach(player -> {
				final Profile data = this.main.getManagerHandler().getProfileManager().getProfiles().get(player.getUniqueId());
		        try {
			        DB.executeUpdate("UPDATE playersdata SET scoreboard=? WHERE name=?", String.valueOf(data.getSettings().get(0).booleanValue()), player.getName());
			        DB.executeUpdate("UPDATE playersdata SET duelRequest=? WHERE name=?", String.valueOf(data.getSettings().get(1).booleanValue()), player.getName());
			        DB.executeUpdate("UPDATE playersdata SET time=? WHERE name=?", String.valueOf(data.getSettings().get(2).booleanValue()), player.getName());
			        DB.executeUpdate("UPDATE playersdata SET displaySpectate=? WHERE name=?", String.valueOf(data.getSpectateSettings().get(0).booleanValue()), player.getName());
			        DB.executeUpdate("UPDATE playersdata SET flySpeed=? WHERE name=?", String.valueOf(data.getSpectateSettings().get(1).booleanValue()), player.getName());
			        DB.executeUpdate("UPDATE playersdata SET played=? WHERE name=?", FormatUtils.getStringValue(data.getStats().get(0), ":"), player.getName());
			    	DB.executeUpdate("UPDATE playersdata SET win=? WHERE name=?", FormatUtils.getStringValue(data.getStats().get(1), ":"), player.getName());
				} catch (SQLException e) { e.printStackTrace(); }
			});
		}
	}
	
	public void update(final UUID uuid) {
	    try {
	        Player player = Bukkit.getPlayer(uuid);
	        if (player == null) return;
	        if (!this.main.getMySQL().existPlayerManagerAsync(uuid).get()) {
	            this.main.getMySQL().createPlayerManagerAsync(uuid, player.getName());
	            return;
	        }
	        if (this.main.getMySQL().existPlayerManagerAsync(uuid).get()) {
	            this.main.getMySQL().updatePlayerManagerAsync(player.getName(), uuid);
	        }
            this.loadAsync(uuid);
	    } catch (InterruptedException | ExecutionException e) {
	        e.printStackTrace();
	    }
	}
	
	public void exitAsync(final UUID uuid) {
		final Profile data = this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid);

		if (data != null) {
			final String playerName = Utils.getName(uuid);

	        DB.executeUpdateAsync("UPDATE playersdata SET scoreboard=? WHERE name=?", String.valueOf(data.getSettings().get(0).booleanValue()), playerName).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET duelRequest=? WHERE name=?", String.valueOf(data.getSettings().get(1).booleanValue()), playerName).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET time=? WHERE name=?", String.valueOf(data.getSettings().get(2).booleanValue()), playerName).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET displaySpectate=? WHERE name=?", String.valueOf(data.getSpectateSettings().get(0).booleanValue()), playerName).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET flySpeed=? WHERE name=?", String.valueOf(data.getSpectateSettings().get(1).booleanValue()), playerName).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET played=? WHERE name=?", FormatUtils.getStringValue(data.getStats().get(0), ":"), playerName).join();
	    	DB.executeUpdateAsync("UPDATE playersdata SET win=? WHERE name=?", FormatUtils.getStringValue(data.getStats().get(1), ":"), playerName).join();
		}

		this.main.getManagerHandler().getInventoryManager().removeUselessInventory(uuid);
		this.main.getManagerHandler().getProfileManager().getProfiles().remove(uuid);
	}
	
	public void loadAsync(final UUID uuid) {
	    final Profile data = this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid);
		final String playerName = Utils.getName(uuid);

        CompletableFuture<String> scoreboardFuture = DB.getFirstRowAsync("SELECT scoreboard FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("scoreboard"));
        scoreboardFuture.thenAccept(accept -> data.getSettings().set(0, Boolean.valueOf(accept)));
        
        CompletableFuture<String> duelRequestFuture = DB.getFirstRowAsync("SELECT duelRequest FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("duelRequest"));
        duelRequestFuture.thenAccept(accept -> data.getSettings().set(1, Boolean.valueOf(accept)));
        
        CompletableFuture<String> timeFuture = DB.getFirstRowAsync("SELECT time FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("time"));
        timeFuture.thenAccept(accept -> data.getSettings().set(2, Boolean.valueOf(accept)));
        
        CompletableFuture<String> flySpeedFuture = DB.getFirstRowAsync("SELECT flySpeed FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("flySpeed"));
        flySpeedFuture.thenAccept(accept -> data.getSpectateSettings().set(0, Boolean.valueOf(accept)));
        
        CompletableFuture<String> displaySpectateFuture = DB.getFirstRowAsync("SELECT displaySpectate FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("displaySpectate"));
        displaySpectateFuture.thenAccept(accept -> data.getSpectateSettings().set(1, Boolean.valueOf(accept)));
        
        CompletableFuture<int[]> elosFuture = DB.getFirstRowAsync("SELECT elos FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> FormatUtils.getSplitValue(row.getString("elos"), ":"));
        elosFuture.thenAccept(accept -> data.getStats().set(2, accept));
        
        CompletableFuture<int[]> winFuture = DB.getFirstRowAsync("SELECT win FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> FormatUtils.getSplitValue(row.getString("win"), ":"));
        winFuture.thenAccept(accept -> data.getStats().set(1, accept));
        
        CompletableFuture<int[]> playedFuture = DB.getFirstRowAsync("SELECT played FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> FormatUtils.getSplitValue(row.getString("played"), ":"));
        playedFuture.thenAccept(accept -> data.getStats().set(0, accept));
	}
    
}

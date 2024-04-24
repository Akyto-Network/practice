package kezukdev.akyto.utils.database;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import co.aikar.idb.BukkitDB;
import co.aikar.idb.DB;
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
			        DB.executeUpdate("UPDATE playersdata SET played=? WHERE name=?", this.main.getUtils().getStringValue(data.getStats().get(0), ":"), player.getName());
			    	DB.executeUpdate("UPDATE playersdata SET win=? WHERE name=?", this.main.getUtils().getStringValue(data.getStats().get(1), ":"), player.getName());
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
	        DB.executeUpdateAsync("UPDATE playersdata SET scoreboard=? WHERE name=?", String.valueOf(data.getSettings().get(0).booleanValue()), Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET duelRequest=? WHERE name=?", String.valueOf(data.getSettings().get(1).booleanValue()), Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET time=? WHERE name=?", String.valueOf(data.getSettings().get(2).booleanValue()), Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET displaySpectate=? WHERE name=?", String.valueOf(data.getSpectateSettings().get(0).booleanValue()), Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET flySpeed=? WHERE name=?", String.valueOf(data.getSpectateSettings().get(1).booleanValue()), Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()).join();
	        DB.executeUpdateAsync("UPDATE playersdata SET played=? WHERE name=?", this.main.getUtils().getStringValue(data.getStats().get(0), ":"), Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()).join();
	    	DB.executeUpdateAsync("UPDATE playersdata SET win=? WHERE name=?", this.main.getUtils().getStringValue(data.getStats().get(1), ":"), Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()).join();
		}
		this.main.getManagerHandler().getInventoryManager().removeUselessInventory(uuid);
		this.main.getManagerHandler().getProfileManager().getProfiles().remove(uuid);
	}
	
	public void loadAsync(final UUID uuid) {
	    final Player player = Bukkit.getPlayer(uuid);
	    final Profile data = this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid);
        String playerName = player.getName();
        CompletableFuture<String> scoreboardFuture = DB.getFirstRowAsync("SELECT scoreboard FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("scoreboard"));
        CompletableFuture<String> duelRequestFuture = DB.getFirstRowAsync("SELECT duelRequest FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("duelRequest"));
        CompletableFuture<String> timeFuture = DB.getFirstRowAsync("SELECT time FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("time"));
        CompletableFuture<String> flySpeedFuture = DB.getFirstRowAsync("SELECT flySpeed FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("flySpeed"));
        CompletableFuture<String> displaySpectateFuture = DB.getFirstRowAsync("SELECT displaySpectate FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> row.getString("displaySpectate"));
        CompletableFuture<int[]> elosFuture = DB.getFirstRowAsync("SELECT elos FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> this.main.getUtils().getSplitValue(row.getString("elos"), ":"));
        CompletableFuture<int[]> winFuture = DB.getFirstRowAsync("SELECT win FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> this.main.getUtils().getSplitValue(row.getString("win"), ":"));
        CompletableFuture<int[]> playedFuture = DB.getFirstRowAsync("SELECT played FROM playersdata WHERE name=?", playerName)
                .thenApply(row -> this.main.getUtils().getSplitValue(row.getString("played"), ":"));
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(scoreboardFuture, duelRequestFuture, timeFuture, flySpeedFuture, displaySpectateFuture, elosFuture, winFuture, playedFuture);
        allOfFuture.join();
        data.getSettings().set(0, Boolean.valueOf(scoreboardFuture.join()));
        data.getSettings().set(1, Boolean.valueOf(duelRequestFuture.join()));
        data.getSettings().set(2, Boolean.valueOf(timeFuture.join()));
        data.getSpectateSettings().set(0, Boolean.valueOf(flySpeedFuture.join()));
        data.getSpectateSettings().set(1, Boolean.valueOf(displaySpectateFuture.join()));
        data.getStats().set(2, elosFuture.join());
        data.getStats().set(1, winFuture.join());
        data.getStats().set(0, playedFuture.join());
	}
    
}

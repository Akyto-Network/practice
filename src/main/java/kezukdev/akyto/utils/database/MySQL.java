package kezukdev.akyto.utils.database;

import co.aikar.idb.DB;
import co.aikar.idb.DbStatement;
import kezukdev.akyto.Practice;

import org.bukkit.Bukkit;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQL {
    
    private Practice main;
    
    public MySQL(final Practice main) { 
    	this.main = main;
    }

    public CompletableFuture<Void> createPlayerManagerTableAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(this.main, () -> {
            try {
                if (this.main.connection == null) {
                    System.out.println("Connection is null. Cannot create tables.");
                    future.completeExceptionally(new NullPointerException("Connection is null"));
                    return;
                }

                DatabaseMetaData dbm = this.main.connection.getMetaData();
                ResultSet tables = dbm.getTables(null, null, "playersdata", null);

                if (!tables.next()) {
                    // Table doesn't exist
                    DB.createTransaction(stm -> createPlayerManagerTable(stm));
                    System.out.println("The SQL database was successfully installed with tables.");
                }

                future.complete(null);
            } catch (SQLException e) {
                System.out.println("An error occurred with the Practice database!");
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private boolean createPlayerManagerTable(DbStatement stm) {
        String player_manager = "CREATE TABLE IF NOT EXISTS playersdata ("
                + "ID INT(64) NOT NULL AUTO_INCREMENT,"
                + "name VARCHAR(16) NOT NULL,"
                + "uuid VARCHAR(64) NOT NULL,"
                + "scoreboard VARCHAR(16) DEFAULT 'true',"
                + "duelRequest VARCHAR(16) DEFAULT 'true',"
                + "time VARCHAR(16) DEFAULT 'day',"
                + "flySpeed VARCHAR(16) DEFAULT 'true',"
                + "displaySpectate VARCHAR(16) DEFAULT 'true',"
                + "played VARCHAR(255) DEFAULT '0:0:0:0:0:0:0',"
                + "win VARCHAR(255) DEFAULT '0:0:0:0:0:0:0',"
                + "elos VARCHAR(255) DEFAULT '1000:1000:1000:1000:1000:1000:1000',"
                + "PRIMARY KEY (`ID`))";
        try {
            DatabaseMetaData dbm = this.main.connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "playersdata", null);
            if (tables.next()) {
                ResultSet columns = dbm.getColumns(null, null, "playersdata", null);
                List<String> existingColumns = new ArrayList<>();
                while (columns.next()) {
                    existingColumns.add(columns.getString("COLUMN_NAME"));
                }
                for (String column : Arrays.asList("ID", "name", "uuid", "scoreboard", "duelRequest", "time", "flySpeed", "displaySpectate","played", "win", "elos")) {
                    if (!existingColumns.contains(column)) {
                        stm.executeUpdateQuery("ALTER TABLE playersdata ADD COLUMN " + column + " VARCHAR(255) DEFAULT 'true'");
                    }
                }
                return true;
            } else {
                stm.executeUpdateQuery(player_manager);
                System.out.println("SUCESS create playersdata table.");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("ERROR while creating playersdata table.");
            e.printStackTrace();
        }
        return false;
    }

    public CompletableFuture<Boolean> createPlayerManagerAsync(UUID uuid, String name) {
        return CompletableFuture.supplyAsync(() -> DB.createTransaction(stm -> createPlayerManager(uuid, name, stm)));
    }

    private boolean createPlayerManager(UUID uuid, String name, DbStatement stm) {
        String query = "INSERT INTO playersdata (uuid, name) VALUES (?, ?)";
        try {
            return stm.executeUpdateQuery(query, uuid.toString(), name) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CompletableFuture<Boolean> existPlayerManagerAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> DB.createTransaction(stm -> existPlayerManager(uuid, stm)));
    }

    private boolean existPlayerManager(UUID uuid, DbStatement stm) {
        String query = "SELECT * FROM playersdata WHERE uuid=?";
        try {
            return stm.executeQueryGetFirstRow(query, uuid.toString()) != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CompletableFuture<Boolean> updatePlayerManagerAsync(String name, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> DB.createTransaction(stm -> updatePlayerManager(name, uuid, stm)));
    }

    private boolean updatePlayerManager(String name, UUID uuid, DbStatement stm) {
        String query = "UPDATE playersdata SET name=? WHERE uuid=?";
        try {
            return stm.executeUpdateQuery(query, name, uuid.toString()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

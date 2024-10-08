package akyto.practice.handler.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import gg.potted.idb.DB;
import akyto.core.Core;
import akyto.core.utils.format.FormatUtils;
import akyto.practice.Practice;
import akyto.core.utils.leaderboard.Top;
import lombok.Getter;

// CREDITS TO TETELIE FOR THIS CLASS //

public class LeaderboardManager {

	private final Practice main;
	
    @Getter
    final Top[] top = new Top[99];
    @Getter
    Top global;

    public LeaderboardManager(final Practice main) { this.main = main; }
    
    public int getRowNumber(final String table) {
        try {
            final PreparedStatement sts = Core.API.getConnection().prepareStatement("select count(*) from " + table);
            final ResultSet rs = sts.executeQuery();
            if (rs.next()) {
                return rs.getInt("count(*)");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("Error while getting row numbers");
    }

   public Map<String, int[]> getTopElo() {
       final Map<String, int[]> top_elo = new HashMap<>();
       try {
    	   if (Core.API.getConnection().isClosed()) {
    		   Core.API.setupHikariCP();
    	   }
           final PreparedStatement sts = Core.API.getConnection().prepareStatement("SELECT * FROM playersdata");
           final ResultSet rs = sts.executeQuery();
           //final ResultSetMetaData resultSetMetaData = rs.getMetaData();
           if (getRowNumber("playersdata") == 0) return top_elo;
           if (rs.next()) {
               for (int i = 1; i <= getRowNumber("playersdata"); ++i) {
                   int[] elos = FormatUtils.getSplitValue(DB.getFirstRow("SELECT elos FROM playersdata WHERE ID=?", i).getString("elos"), ":");
                   String player_name = DB.getFirstRow("SELECT name FROM playersdata WHERE ID=?", i).getString("name");
                   top_elo.put(player_name, elos);
               }
               return top_elo;
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       throw new NullPointerException("The player does not have any information in the table");
   }

   public void refresh() {
       Map<String, int[]> map = getTopElo();
       this.main.getKits().forEach(ladder -> top[ladder.id()] = new Top(ladder.id(), map));
       global = new Top(map);
   }

}
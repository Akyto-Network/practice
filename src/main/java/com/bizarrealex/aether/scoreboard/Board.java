package com.bizarrealex.aether.scoreboard;

import lombok.Getter;
import org.bukkit.entity.*;
import com.bizarrealex.aether.scoreboard.cooldown.*;
import com.bizarrealex.aether.*;
import org.bukkit.scoreboard.*;
import org.bukkit.*;
import java.util.*;

public class Board {
    @Getter
    private static Set<Board> boards;
    @Getter
    private Scoreboard scoreboard;
    @Getter
    private Player player;
    @Getter
    private Objective objective;
    @Getter
    private Set<String> keys;
    @Getter
    private List<BoardEntry> entries;
    private final Set<BoardCooldown> cooldowns;
    private final Aether aether;
    private final AetherOptions options;
    
    static {
        Board.boards = new HashSet<>();
    }
    
    public Board(final Player player, final Aether aether, final AetherOptions options) {
        this.player = player;
        this.aether = aether;
        this.options = options;
        this.keys = new HashSet<>();
        this.cooldowns = new HashSet<>();
        this.entries = new ArrayList<>();
        this.setup();
    }
    
    private void setup() {
        if (this.options.hook() && !this.player.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            this.scoreboard = this.player.getScoreboard();
        }
        else {
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        (this.objective = this.scoreboard.registerNewObjective("glaedr_is_shit", "dummy")).setDisplaySlot(DisplaySlot.SIDEBAR);
        if (this.aether.getAdapter() != null) {
            this.objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.aether.getAdapter().getTitle(this.player)));
        }
        else {
            this.objective.setDisplayName("Default Title");
        }
        Board.boards.add(this);
    }
    
    public String getNewKey(final BoardEntry entry) {
        ChatColor[] values;
        for (int length = (values = ChatColor.values()).length, i = 0; i < length; ++i) {
            final ChatColor color = values[i];
            String colorText = String.valueOf(color) + ChatColor.WHITE;
            if (entry.getText().length() > 16) {
                final String sub = entry.getText().substring(0, 16);
                colorText += ChatColor.getLastColors(sub);
            }
            if (!this.keys.contains(colorText)) {
                this.keys.add(colorText);
                return colorText;
            }
        }
        throw new IndexOutOfBoundsException("No more keys available!");
    }
    
    public List<String> getBoardEntriesFormatted() {
        final List<String> toReturn = new ArrayList<>();
        for (final BoardEntry entry : new ArrayList<>(this.entries)) {
            toReturn.add(entry.getText());
        }
        return toReturn;
    }
    
    public BoardEntry getByPosition(final int position) {
        int i = 0;
        for (final BoardEntry board : this.entries) {
            if (i == position) {
                return board;
            }
            ++i;
        }
        return null;
    }
    
    public BoardCooldown getCooldown(final String id) {
        for (final BoardCooldown cooldown : this.getCooldowns()) {
            if (cooldown.getId().equals(id)) {
                return cooldown;
            }
        }
        return null;
    }
    
    public Set<BoardCooldown> getCooldowns() {
        this.cooldowns.removeIf(cooldown -> System.currentTimeMillis() >= cooldown.getEnd());
        return this.cooldowns;
    }
    
    public static Board getByPlayer(final Player player) {
        for (final Board board : Board.boards) {
            if (board.getPlayer().getName().equals(player.getName())) {
                return board;
            }
        }
        return null;
    }

}
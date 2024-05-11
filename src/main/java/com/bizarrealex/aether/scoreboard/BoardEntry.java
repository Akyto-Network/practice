package com.bizarrealex.aether.scoreboard;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.scoreboard.*;

@Getter
public class BoardEntry
{
    private final Board board;
    private String text;
    private final String originalText;
    private final String key;
    private Team team;
    
    public BoardEntry(final Board board, final String text) {
        this.board = board;
        this.text = text;
        this.originalText = text;
        this.key = board.getNewKey(this);
        this.setup();
    }
    
    public BoardEntry setup() {
        final Scoreboard scoreboard = this.board.getScoreboard();
        this.text = ChatColor.translateAlternateColorCodes('&', this.text);
        String teamName = this.key;
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }
        if (scoreboard.getTeam(teamName) != null) {
            this.team = scoreboard.getTeam(teamName);
        }
        else {
            this.team = scoreboard.registerNewTeam(teamName);
        }
        if (!this.team.getEntries().contains(this.key)) {
            this.team.addEntry(this.key);
        }
        if (!this.board.getEntries().contains(this)) {
            this.board.getEntries().add(this);
        }
        return this;
    }
    
    public BoardEntry send(final int position) {
        final Objective objective = this.board.getObjective();
        if (this.text.length() > 16) {
            final boolean fix = this.text.toCharArray()[15] == '\ufffd';
            final String prefix = fix ? this.text.substring(0, 15) : this.text.substring(0, 16);
            final String suffix = fix ? this.text.substring(15) : (ChatColor.getLastColors(prefix) + this.text.substring(16));
            this.team.setPrefix(prefix);
            if (suffix.length() > 16) {
                this.team.setSuffix(suffix.substring(0, 16));
            }
            else {
                this.team.setSuffix(suffix);
            }
        }
        else {
            this.team.setPrefix(this.text);
            this.team.setSuffix("");
        }
        final Score score = objective.getScore(this.key);
        score.setScore(position);
        return this;
    }
    
    public void remove() {
        this.board.getKeys().remove(this.key);
        this.board.getScoreboard().resetScores(this.key);
    }

    public BoardEntry setText(final String text) {
        this.text = text;
        return this;
    }

}

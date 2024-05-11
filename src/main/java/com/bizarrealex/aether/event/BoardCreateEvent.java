package com.bizarrealex.aether.event;

import lombok.Getter;
import org.bukkit.event.*;
import com.bizarrealex.aether.scoreboard.*;
import org.bukkit.entity.*;

@Getter
public class BoardCreateEvent extends Event
{
    private static final HandlerList handlers;
    private final Board board;
    private final Player player;
    
    static {
        handlers = new HandlerList();
    }
    
    public BoardCreateEvent(final Board board, final Player player) {
        this.board = board;
        this.player = player;
    }
    
    public HandlerList getHandlers() {
        return BoardCreateEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return BoardCreateEvent.handlers;
    }

}

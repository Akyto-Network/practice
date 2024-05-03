package kezukdev.akyto.arena;

import java.util.Arrays;
import java.util.List;

import kezukdev.akyto.Practice;
import kezukdev.akyto.utils.location.LocationSerializer;

public class Arena {

    private final String name;
    public String getName() { return this.name; }
    private List<LocationSerializer> position;
    public List<LocationSerializer> getPosition() { return position; }
    private ArenaType arenaType;
    public ArenaType getArenaType() { return arenaType; }
    public void setArenaType(ArenaType arenaType) { this.arenaType = arenaType; }
    
    public Arena(final Practice main, final String name, final LocationSerializer loc1, final LocationSerializer loc2, final ArenaType arenaType) {
        this.name = name;
        position = Arrays.asList(loc1, loc2);
        this.arenaType = arenaType;
        main.getArenas().add(this);
    }
    
    public Arena(final String name) {
        this.name = name;
    }
}

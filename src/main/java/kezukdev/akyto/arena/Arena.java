package kezukdev.akyto.arena;

import java.util.Arrays;
import java.util.List;

import kezukdev.akyto.Practice;
import kezukdev.akyto.utils.location.LocationSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Arena {

    private final String name;
    private List<LocationSerializer> position;
    @Setter
    private ArenaType arenaType;

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

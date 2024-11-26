package akyto.practice.arena;

import java.util.Arrays;
import java.util.List;

import org.bukkit.*;

import akyto.core.utils.location.LocationSerializer;
import akyto.practice.Practice;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Arena {

    private final String name;
    private final List<LocationSerializer> position;
    private ArenaType arenaType;
    private Material icon;
    private boolean save;

    public Arena(final Practice main, final String name, final LocationSerializer loc1, final LocationSerializer loc2, final ArenaType arenaType, final Material icon) {
        this.name = name;
        position = Arrays.asList(loc1, loc2);
        this.arenaType = arenaType;
        this.icon = icon;
    }
}

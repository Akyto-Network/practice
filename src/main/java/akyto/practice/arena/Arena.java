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
    private LocationSerializer corner1;
    private LocationSerializer corner2;
    private ArenaType arenaType;
    private Material icon;
    private boolean save;
    private boolean build;

    public Arena(final Practice main, final String name, final LocationSerializer loc1, final LocationSerializer loc2,final LocationSerializer corner1, final LocationSerializer corner2, final ArenaType arenaType, final Material icon, final boolean build) {
        this.name = name;
        this.position = Arrays.asList(loc1, loc2);
        this.arenaType = arenaType;
        this.icon = icon;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.build = build;
    }
}

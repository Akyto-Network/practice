package kezukdev.akyto.arena;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import kezukdev.akyto.Practice;
import kezukdev.akyto.utils.location.LocationSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Arena {

    private final String name;
    private final List<LocationSerializer> position;
    @Setter
    private ArenaType arenaType;
    @Setter
    private Material icon;

    public Arena(final Practice main, final String name, final LocationSerializer loc1, final LocationSerializer loc2, final ArenaType arenaType, final Material icon) {
        this.name = name;
        position = Arrays.asList(loc1, loc2);
        this.arenaType = arenaType;
        this.icon = icon;
        main.getArenas().add(this);
    }
}

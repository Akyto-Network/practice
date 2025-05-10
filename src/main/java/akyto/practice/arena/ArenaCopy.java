package akyto.practice.arena;

import akyto.core.utils.location.LocationSerializer;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class ArenaCopy {

    private final UUID id;
    private final List<UUID> players;
    private final List<LocationSerializer> position;
    private final LocationSerializer corner1;
    private final LocationSerializer corner2;

    public ArenaCopy(final UUID id, final List<LocationSerializer> position,final List<UUID> players, final LocationSerializer corner1, final LocationSerializer corner2) {
        this.id = id;
        this.position = position;
        this.players = players;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

}

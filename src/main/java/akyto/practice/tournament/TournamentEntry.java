package akyto.practice.tournament;

import akyto.practice.kit.Kit;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class TournamentEntry {

    private final UUID id;
    private final Kit kit;
    private final int teamSize;
    private final List<UUID> participants;

    public TournamentEntry(UUID id, Kit kit, int teamSize, List<UUID> participants) {
        this.id = id;
        this.kit = kit;
        this.teamSize = teamSize;
        this.participants = participants;
    }
}
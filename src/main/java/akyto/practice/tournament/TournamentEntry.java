package akyto.practice.tournament;

import akyto.practice.kit.Kit;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
public class TournamentEntry {

    private final UUID creator;
    private final UUID id;
    private final Kit kit;
    private final int teamSize;
    @Setter
    public TournamentState tournamentState;
    private final List<UUID> participants;
    private final List<UUID> byePlayer;

    public TournamentEntry(UUID creator, UUID id, Kit kit, int teamSize) {
        this.creator = creator;
        this.id = id;
        this.kit = kit;
        this.teamSize = teamSize;
        this.tournamentState = TournamentState.WAITING;
        this.participants = Lists.newArrayList();
        this.byePlayer = Lists.newArrayList();
    }
}
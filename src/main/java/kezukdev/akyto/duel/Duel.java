package kezukdev.akyto.duel;

import java.util.*;
import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.kit.Kit;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Duel {
	
	private DuelType duelType;
	private Set<UUID> first;
	private Set<UUID> firstAlives;
	private Set<UUID> second;
	private Set<UUID> secondAlives;
	private Set<UUID> spectator;
	private Set<UUID> dropped;
	private Set<UUID> alives;
	private boolean ranked;
	private DuelState state;
	private Kit kit;
    public long startTime;
    public long duration;
    public Timer timer;
    private List<UUID> winner;
    public Arena arena;
	
	public Duel(final Practice main, final Set<UUID> first, final Set<UUID> second, final boolean ranked, final Kit kit, final Duel.DuelType type, final Arena arena) {
		this.duelType = type;
		this.first = first;
		this.second = second;
		this.spectator = new HashSet<>();
		this.dropped = new HashSet<>();
		this.ranked = ranked;
		if (type.equals(DuelType.FFA)) {
			this.alives = new HashSet<>(first);
			this.alives.addAll(second);
		}
		if (type.equals(DuelType.SPLIT)) {
			this.firstAlives = new HashSet<>(first);
			this.secondAlives = new HashSet<>(second);
		}
		this.kit = kit;
		this.state = DuelState.STARTING;
        this.duration = 0;
        this.winner = new ArrayList<>();
        if (arena != null) this.arena = arena;
		main.getDuels().add(this);
		Arrays.asList(first, second).forEach(uuids -> uuids.forEach(uuid -> main.getManagerHandler().getProfileManager().getDuelStatistics().put(uuid, new DuelStatistics())));
		main.getManagerHandler().getDuelManager().start(new ArrayList<>(first), new ArrayList<>(second), kit);
	}
	
	public enum DuelType {
		SINGLE,
		SPLIT,
		FFA
	}

}

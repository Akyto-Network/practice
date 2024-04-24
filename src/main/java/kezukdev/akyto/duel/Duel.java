package kezukdev.akyto.duel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;
import java.util.stream.Collectors;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.kit.Kit;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Duel {
	
	private Set<UUID> first;
	private Set<UUID> second;
	private Set<UUID> spectator;
	private Set<UUID> dropped;
	private boolean ranked;
	private DuelState state;
	private Kit kit;
    public long startTime;
    public long duration;
    public Timer timer;
    private UUID winner;
	
	public Duel(final Practice main, final Set<UUID> first, final Set<UUID> second, final boolean ranked, final Kit kit) {
		this.first = first;
		this.second = second;
		this.spectator = new HashSet<UUID>();
		this.dropped = new HashSet<UUID>();
		this.ranked = ranked;
		this.kit = kit;
		this.state = DuelState.STARTING;
        this.duration = 0;
		main.getDuels().add(this);
		Arrays.asList(first, second).forEach(uuids -> uuids.forEach(uuid -> main.getManagerHandler().getProfileManager().getDuelStatistics().put(uuid, new DuelStatistics())));
		main.getManagerHandler().getDuelManager().startSingle(Arrays.asList(first.stream().collect(Collectors.toList()).get(0), second.stream().collect(Collectors.toList()).get(0)), kit);
	}

}

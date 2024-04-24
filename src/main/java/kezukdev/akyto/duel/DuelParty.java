package kezukdev.akyto.duel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;
import java.util.stream.Collectors;

import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.kit.Kit;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DuelParty {
	
	private Set<UUID> first;
	private Set<UUID> firstAlives;
	private Set<UUID> second;
	private Set<UUID> secondAlives;
	private Set<UUID> spectator;
	private Set<UUID> dropped;
	private Set<UUID> alives;
	private DuelState state;
	private Kit kit;
	public Arena arena;
	private String duelPartyType;
    public long startTime;
    public long duration;
    public Timer timer;
    private List<UUID> winner;
	
	public DuelParty(final Practice main, final Set<UUID> first, final Set<UUID> second, final String duelPartyType, final Kit kit) {
		this.duelPartyType = duelPartyType;
		this.first = first;
		this.second = second;
		this.spectator = new HashSet<UUID>();
		this.dropped = new HashSet<UUID>();
		if (duelPartyType.equals("split") || duelPartyType.equals("duel")) {
			this.firstAlives = new HashSet<UUID>();
			this.firstAlives.addAll(first);
			this.secondAlives = new HashSet<UUID>();
			this.secondAlives.addAll(second);
		}
		if (duelPartyType.equals("ffa")) {
			this.alives = new HashSet<UUID>();
			this.alives.addAll(first);
			this.alives.addAll(second);
		}
		this.kit = kit;
		this.state = DuelState.STARTING;
        this.duration = 0;
        this.winner = new ArrayList<UUID>();
		main.getDuelsParty().add(this);
		Arrays.asList(first, second).forEach(uuids -> uuids.forEach(uuid -> main.getManagerHandler().getProfileManager().getDuelStatistics().put(uuid, new DuelStatistics())));
		main.getManagerHandler().getDuelManager().startMultiple(Arrays.asList(first.stream().collect(Collectors.toList()), second.stream().collect(Collectors.toList())), kit);
	}

}
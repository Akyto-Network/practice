package akyto.practice.handler.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.editor.Edited;
import lombok.Getter;

@Getter
public class ProfileManager {
	
	private final ConcurrentMap<UUID, Map<String, Edited>> editor;
	private final HashMap<UUID, DuelStatistics> duelStatistics;
	public ConcurrentMap<UUID, String> editing;
	
	public ProfileManager() {
		this.editor = new ConcurrentHashMap<>();
		this.duelStatistics = new HashMap<>();
		this.editing = new ConcurrentHashMap<>();
	}
}

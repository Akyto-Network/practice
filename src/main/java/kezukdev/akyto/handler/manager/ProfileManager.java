package kezukdev.akyto.handler.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.kiteditor.Edited;
import lombok.Getter;

@Getter
public class ProfileManager {
	
	private final ConcurrentMap<UUID, Profile> profiles;
	private final ConcurrentMap<UUID, Map<String, Edited>> editor;
	private final HashMap<UUID, DuelStatistics> duelStatistics;
	public ConcurrentMap<UUID, String> editing;
	
	public ProfileManager() {
		this.profiles = new ConcurrentHashMap<>();
		this.editor = new ConcurrentHashMap<>();
		this.duelStatistics = new HashMap<>();
		this.editing = new ConcurrentHashMap<>();
	}
}

package akyto.practice.utils.data;

import java.util.UUID;

import akyto.practice.kit.Kit;
import akyto.practice.utils.Utils;

public class DataUtils {
	
	public static void addPlayedToData(final UUID uuid, final Kit kit) {
		Utils.getProfiles(uuid).getStats().get(0)[kit.id()] = Utils.getProfiles(uuid).getStats().get(0)[kit.id()]+1;
	}
	
	public static void addWinToData(final UUID uuid, final Kit kit) { 
		Utils.getProfiles(uuid).getStats().get(1)[kit.id()] = Utils.getProfiles(uuid).getStats().get(1)[kit.id()]+1; 
	}

	public static void resetData(final UUID uuid) {
		// RESET DATA HERE //
	}
}

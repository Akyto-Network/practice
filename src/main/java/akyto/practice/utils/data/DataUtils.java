package akyto.practice.utils.data;

import akyto.practice.kit.Kit;
import akyto.practice.utils.Utils;

import java.util.UUID;

public class DataUtils {
	
	public static void addPlayedToData(final UUID uuid, final Kit kit) {
		Utils.getProfiles(uuid).getStats().getFirst()[kit.id()]++;
	}
	
	public static void addWinToData(final UUID uuid, final Kit kit) { 
		Utils.getProfiles(uuid).getStats().get(1)[kit.id()]++;
	}

	public static void resetData(final UUID uuid) {
		// RESET DATA HERE //
	}
}

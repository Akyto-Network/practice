package akyto.practice.utils.match;

import java.util.UUID;

import akyto.practice.Practice;
import akyto.practice.kit.Kit;
import akyto.practice.utils.Utils;

public class EloUtils {
	
	public static void eloChanges(final UUID winner, final UUID looser, final Kit kit, final Practice main) {
        int winnersElo = Utils.getProfiles(winner).getStats().get(2)[kit.id()];
        int losersElo = Utils.getProfiles(looser).getStats().get(2)[kit.id()];
        final double expectedp = 1.0D / (1.0D + Math.pow(10.0D, (winnersElo - losersElo) / 400.0D));
        final int scoreChange = (int) limit((expectedp * 32.0D), 4, 40);
        Utils.getProfiles(winner).getStats().get(2)[kit.id()] = winnersElo + scoreChange;
        Utils.getProfiles(looser).getStats().get(2)[kit.id()] = losersElo - scoreChange;
    }
	
    public static double limit(double value, double min, double max) {
		return Math.min(Math.max(value, min), max);
	}

}

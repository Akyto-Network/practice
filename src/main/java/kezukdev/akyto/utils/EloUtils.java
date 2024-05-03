package kezukdev.akyto.utils;

import java.util.UUID;

import kezukdev.akyto.Practice;
import kezukdev.akyto.kit.Kit;

public class EloUtils {
	
	public static void eloChanges(final UUID winner, final UUID looser, final Kit kit, final Practice main) {
        int winnersElo = main.getManagerHandler().getProfileManager().getProfiles().get(winner).getStats().get(2)[kit.id()];
        int losersElo = main.getManagerHandler().getProfileManager().getProfiles().get(looser).getStats().get(2)[kit.id()];
        final double expectedp = 1.0D / (1.0D + Math.pow(10.0D, (winnersElo - losersElo) / 400.0D));
        final int scoreChange = (int) limit((expectedp * 32.0D), 4, 40);
        main.getManagerHandler().getProfileManager().getProfiles().get(winner).getStats().get(2)[kit.id()] = winnersElo + scoreChange;
        main.getManagerHandler().getProfileManager().getProfiles().get(looser).getStats().get(2)[kit.id()] = losersElo - scoreChange;
    }
	
    public static double limit(double value, double min, double max) {
		return Math.min(Math.max(value, min), max);
	}

}

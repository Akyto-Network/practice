package akyto.practice.utils.match;

import java.util.TimerTask;

import akyto.practice.duel.Duel;

public class DurationUtils {
	
    public static void startDuration(final Duel matchEntry) {
        matchEntry.getTimer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDuration(matchEntry);
            }
        }, 1000, 1000);
    }
    
    private static void updateDuration(final Duel matchEntry) {
        long currentTime = System.currentTimeMillis();
        matchEntry.duration = currentTime - matchEntry.getStartTime();
    }

}

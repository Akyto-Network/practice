package akyto.practice.utils.match;

import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.handler.manager.QueueManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DurationUtils {
	
    public static void startDuration(final Object object) {
        final Timer timer = object instanceof Duel ? ((Duel) object).getTimer() : Practice.API.getQueue().get((UUID) object).getTimer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDuration(object);
            }
        }, 1000, 1000);
    }
    
    private static void updateDuration(final Object object) {
        long currentTime = System.currentTimeMillis();
        if (object instanceof Duel) {
            ((Duel) object).duration = currentTime - ((Duel) object).getStartTime();
        }
        else if (object instanceof UUID) {
            Practice.API.getQueue().get((UUID) object).duration = currentTime - Practice.API.getQueue().get((UUID) object).getStartTime();
        }
        else {
            System.out.println("ERROR OCCURRED WHILE UPDATE DURATION OBJECT IS NOT RECOGNIZED BY FUNC");
        }
    }

    public static String getFormattedDuration(final long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
}

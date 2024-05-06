package kezukdev.akyto.utils;

import java.text.DecimalFormat;

public class FormatUtils {
	
	public static String formatTime(final long cooldown, final double dividend) {
		final double time = cooldown / dividend;
		final DecimalFormat df = new DecimalFormat("#.#");
		return df.format(time);
	}
	
    public static String formatTime(int time) {
        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = ((time / 60) / 60) % 24;
        int days = (((time / 60) / 60) / 24);
        String format;
        if(days > 0) {
            format = days + ":" + (hours >= 10 ? hours : "0" + hours) + ":" + (minutes >= 10 ? minutes : "0" + minutes) + ":" + (seconds >= 10 ? seconds : "0" + seconds);
            return format;
        }
        if(hours > 0) {
            format = hours + ":" + (minutes >= 10 ? minutes : "0" + minutes) + ":" + (seconds >= 10 ? seconds : "0" + seconds);
            return format;
        }
        if(minutes > 0) {
            format = minutes + ":" + (seconds >= 10 ? seconds : "0" + seconds);
            return format;
        }
        format = seconds + "s";
        return format;
    }

    public int getSeconds(int seconds) {
        return seconds;
    }

    public int getSeconds(int minutes, int seconds) {
        return (minutes * 60) + seconds;
    }

    public int getSeconds(int hours, int minutes, int seconds) {
        return ((hours * 60) * 60) + (minutes * 60) + seconds;
    }

    public int getSeconds(int days, int hours, int minutes, int seconds) {
        return (((days * 24) * 60) * 60) + ((hours * 60) * 60) + (minutes * 60) + seconds;
    }
    
	public static int[] getSplitValue(final String string, final String spliter) {
        final String[] split = string.split(spliter);
        final int[] board = new int[split.length];
        for (int i = 0; i <= split.length - 1; ++i) {
            board[i] = Integer.parseInt(split[i]);
        }
        return board;
    }
	
	public static String getStringValue(final int[] board, final String spliter) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <= board.length - 1; ++i) {
            stringBuilder.append(board[i]);
            if (i != board.length - 1) {
                stringBuilder.append(spliter);
            }
        }
        return stringBuilder.toString();
    }

}

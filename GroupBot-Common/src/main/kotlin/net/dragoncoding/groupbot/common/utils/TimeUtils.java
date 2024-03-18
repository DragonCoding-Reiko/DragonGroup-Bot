package net.dragoncoding.groupbot.common.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

	public static String formatMsToDurationInSeconds(Long durationInMs) {
		var seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMs);
		var milliseconds = durationInMs % 1000;

		return seconds + "." + milliseconds;
	}
}

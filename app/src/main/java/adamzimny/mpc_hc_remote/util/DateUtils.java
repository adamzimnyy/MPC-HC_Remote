package adamzimny.mpc_hc_remote.util;

/**
 * Created by adamz on 01.10.2016.
 */
public class DateUtils {


    public static String secondsToTime(long seconds) {
        long minutesInMilli = 60;
        long hoursInMilli = minutesInMilli * 60;
        long elapsedHours = seconds / hoursInMilli;
        seconds = seconds % hoursInMilli;
        long elapsedMinutes = seconds / minutesInMilli;
        seconds = seconds % minutesInMilli;
        long elapsedSeconds = seconds;

        return     String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);

    }
}

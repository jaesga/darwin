package models.utils;

import play.Logger;
import play.libs.Time;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * @param expiration 1s 2mn 1h 1d
     */
    public static boolean isDateExpired(Date date, String expiration) {
        Date currentDate = new Date();
        Date cookieExpirationDate = DateUtils.getDateAfterDuration(date, expiration);
        return currentDate.after(cookieExpirationDate);
    }

    /**
     * @param duration 1s 2mn 1h 1d
     */
    public static Date getDateAfterDuration(String duration) {
        return getDateAfterDuration(new Date(), duration);
    }

    /**
     * @param duration 1s 2mn 1h 1d
     */
    public static Date getDateAfterDuration(Date date, String duration) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, Time.parseDuration(duration));
            return calendar.getTime();
        } catch (IllegalArgumentException e) {
            Logger.error("IllegalArgumentException - " + duration + " is not a valid duration.");
            return new Date();
        }
    }

}

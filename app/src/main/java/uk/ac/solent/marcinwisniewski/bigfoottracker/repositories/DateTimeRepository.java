package uk.ac.solent.marcinwisniewski.bigfoottracker.repositories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class to manipulate dates.
 */
public class DateTimeRepository {

    private SimpleDateFormat sdf;

    /**
     * Get current time.
     *
     * @return
     */
    public String getCurrentTime() {
        sdf = new SimpleDateFormat("HH-mm-ss", Locale.UK);
        return sdf.format(new Date());
    }

    /**
     * Get current date.
     *
     * @return
     */
    public String getCurrentDate() {
        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
        return sdf.format(new Date());
    }

    /**
     * Parse given date.
     *
     * @param date
     * @return
     */
    public String parseDate(Date date) {
        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
        return sdf.format(date);
    }
}

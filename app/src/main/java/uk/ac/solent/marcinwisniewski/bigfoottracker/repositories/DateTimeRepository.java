package uk.ac.solent.marcinwisniewski.bigfoottracker.repositories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeRepository {

    private SimpleDateFormat sdf;

    public String getCurrentTime() {
        sdf = new SimpleDateFormat("HH-mm-ss", Locale.UK);
        return sdf.format(new Date());
    }

    public String getCurrentDate() {
        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
        return sdf.format(new Date());
    }

    public String parseDate(Date date) {
        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
        return sdf.format(date);
    }
}

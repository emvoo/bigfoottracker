package uk.ac.solent.marcinwisniewski.bigfoottracker.db;

public class Step {
    private int id, step;
    private double latitude, longitude, altitude, distance;
    private String date_created, time_created;

    public Step () {}

    public Step (int step, double latitude, double longitude, double altitude, double distance, String date_created, String time_created) {
        this.step = step;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.distance = distance;
        this.date_created = date_created;
        this.time_created = time_created;
    }

    public Step (int id, int step, double latitude, double longitude, double altitude, double distance, String date_created, String time_created) {
        this.id = id;
        this.step = step;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.distance = distance;
        this.date_created = date_created;
        this.time_created = time_created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getTime_created() {
        return time_created;
    }

    public void setTime_created(String time_created) {
        this.time_created = time_created;
    }
}

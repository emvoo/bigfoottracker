package uk.ac.solent.marcinwisniewski.bigfoottracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for connecting to application database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "bigfoottracker";

    // Table Names
    private static final String TABLE_STEPS = "steps";
    private static final String TABLE_USER = "user";

    // Common column names
    private static final String KEY_ID = "id";

    // steps table - column names
    private static final String STEP = "steps";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String ALTITUDE = "altitude";
    private static final String DISTANCE = "distance";
    private static final String DATE = "date_created";
    private static final String TIME = "time_created";

    // User table - column names
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    // Tables Create Statements
    private static final String CREATE_USER_DETAILS_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER
                    + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USERNAME + " TEXT, "
                    + PASSWORD + " TEXT);";

    private static final String CREATE_STEPS_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_STEPS
                    + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + STEP + " INTEGER NOT NULL, "
                    + LATITUDE + " REAL, "
                    + LONGITUDE + " REAL, "
                    + ALTITUDE + " REAL, "
                    + DISTANCE + " REAL, "
                    + DATE + " TEXT NOT NULL, "
                    + TIME + " TEXT NOT NULL);";


    /**
     * Constructor method.
     *
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_USER_DETAILS_TABLE_QUERY);
        sqLiteDatabase.execSQL(CREATE_STEPS_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    /**
     * Save new step to database.
     *
     * @param step
     */
    public void createStep(Step step) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(STEP, step.getStep());
        values.put(LATITUDE, step.getLatitude());
        values.put(LONGITUDE, step.getLongitude());
        values.put(ALTITUDE, step.getAltitude());
        values.put(DISTANCE, step.getDistance());
        values.put(DATE, step.getDate_created());
        values.put(TIME, step.getTime_created());
        db.insert(TABLE_STEPS, null, values);
    }

    /**
     * Get all steps from database.
     *
     * @return
     */
    public List<Step> getAllSteps() {
        List<Step> steps = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_STEPS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Step step = new Step();
                step.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                step.setStep((c.getInt(c.getColumnIndex(STEP))));
                step.setLatitude(c.getDouble(c.getColumnIndex(LATITUDE)));
                step.setLongitude(c.getDouble(c.getColumnIndex(LONGITUDE)));
                step.setAltitude(c.getDouble(c.getColumnIndex(ALTITUDE)));
                step.setDistance(c.getDouble(c.getColumnIndex(DISTANCE)));
                step.setDate_created(c.getString(c.getColumnIndex(DATE)));
                step.setTime_created(c.getString(c.getColumnIndex(TIME)));
                steps.add(step);
            } while (c.moveToNext());
        }

        return steps;
    }

    /**
     * Counts all steps in database.
     *
     * @return
     */
    public long countAllSteps() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM("+ STEP +") AS "+ STEP +" FROM " + TABLE_STEPS;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(STEP));
        }
        return 0;
    }

    /**
     * Counts all steps by given date.
     *
     * @param date
     * @return
     */
    public long countAllStepsByDay(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM("+ STEP +") AS "+ STEP +" FROM " + TABLE_STEPS + " WHERE " + DATE+"='"+date+"'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(STEP));
        }
        return 0;
    }

    /**
     * Count distance by given date.
     *
     * @param date
     * @return
     */
    public double countDistanceByDay(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM("+ DISTANCE +") AS "+ DISTANCE +" FROM " + TABLE_STEPS + " WHERE " + DATE+"='"+date+"'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(DISTANCE));
        }
        return 0;
    }

    /**
     * Count whole distance.
     *
     * @return
     */
    public double countDistance() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM("+ DISTANCE +") AS "+ DISTANCE +" FROM " + TABLE_STEPS + ";";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(DISTANCE));
        }
        return 0;
    }

    /**
     * Get last inserted step.
     *
     * @return
     */
    public Cursor getLastStep() {
        String selectQuery = "SELECT * FROM " + TABLE_STEPS + " ORDER BY id DESC LIMIT 1;";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            return c;
        }
        return null;
    }

    /**
     * Get last inserted date.
     *
     * @param c
     * @return
     */
    public String getLastInsertDate(Cursor c) {
        return c.getString(c.getColumnIndex(DATE));
    }

    /**
     * Get previous step latitude.
     *
     * @param c
     * @return
     */
    public double getPreviousStepLatitude(Cursor c) {
        return c.getDouble(c.getColumnIndex(LATITUDE));
    }

    /**
     * Get previous step longitude.
     *
     * @param c
     * @return
     */
    public double getPreviousStepLongitude(Cursor c) {
        return c.getDouble(c.getColumnIndex(LONGITUDE));
    }

    /**
     * Clear database.
     */
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STEPS, null, null);
        db.delete(TABLE_USER, null, null);
    }

    /**
     * Saves user in database.
     *
     * @param user
     * @return
     */
    public long createUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, user.getUsername());
        values.put(PASSWORD, user.getPassword());
        return db.insert(TABLE_USER, null, values);
    }

    /**
     * Get user by ID>
     *
     * @param id
     * @return
     */
    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_USER + " WHERE " + KEY_ID + "=" + id;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            User user = new User();
            user.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            user.setUsername(c.getString(c.getColumnIndex(USERNAME)));
            return user;
        }
        return null;
    }

    /**
     * Get all users.
     *
     * @return
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                User user = new User();
                user.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                user.setUsername(c.getString(c.getColumnIndex(USERNAME)));
                users.add(user);

            } while (c.moveToNext());
        }

        return users;
    }

    /**
     * Closing database
     */
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}

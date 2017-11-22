package uk.ac.solent.marcinwisniewski.bigfoottracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

import uk.ac.solent.marcinwisniewski.bigfoottracker.repositories.DateTimeRepository;

public class StepsDB extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private DateTimeRepository dateTimeRepository;

    private static final int DBVERSION = 1;
    private static final String DBNAME = "steps.db";
    private static final String TABLE_NAME = "steps";

    public static final String STEPS = "steps";
    public static final String ID = "id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String DISTANCE = "distance";
    public static final String DATE = "date_created";
    public static final String TIME = "time_created";
    private static final String CREATE_STEPS_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + STEPS + " INTEGER NOT NULL, "
            + LATITUDE + " REAL, "
            + LONGITUDE + " REAL, "
            + ALTITUDE + " REAL, "
            + DISTANCE + " REAL, "
            + DATE + " TEXT NOT NULL, "
            + TIME + " TEXT NOT NULL);";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STEPS_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public StepsDB(Context context)
    {
        super(context, DBNAME, null, DBVERSION);
        dateTimeRepository = new DateTimeRepository();
    }

    public long insert(double latitude, double longitude, double altitude, double distance)
    {
        openDB();
        ContentValues values = new ContentValues();
        values.put(STEPS, 1);
        values.put(LATITUDE, latitude);
        values.put(LONGITUDE, longitude);
        values.put(ALTITUDE, altitude);
        values.put(DISTANCE, distance);
        values.put(DATE, dateTimeRepository.getCurrentDate());
        values.put(TIME, dateTimeRepository.getCurrentTime());
        return db.insert(TABLE_NAME, null, values);
    }

    public void openDB()
    {
        if (db == null || !db.isOpen())
            db = getWritableDatabase();
    }

    public void closeDB()
    {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public Cursor getAllSteps()
    {
        openDB();
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    public Cursor getById(long id)
    {
        String whereArgs = "id = " + id;
        return db.query(true, TABLE_NAME, null, whereArgs, null, null, null, null, null);
    }

    public long countAllSteps()
    {
        openDB();
        String query = "SELECT SUM("+ STEPS +") AS "+ STEPS +" FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(STEPS));
        }
        return 0;
    }

    public long countAllStepsByDay(String date)
    {
        openDB();
        String query = "SELECT SUM("+ STEPS +") AS "+ STEPS +" FROM " + TABLE_NAME + " WHERE " + DATE+"='"+date+"'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(STEPS));
        }
        return 0;
    }

    public Cursor getLastRecord()
    {
        openDB();
        String query = "SELECT * FROM " + STEPS + " ORDER BY id DESC LIMIT 1;";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor;
        }
        return null;
    }

    public boolean hasRecords(Context context)
    {
        File dbFile = context.getDatabasePath(DBNAME);
        if (dbFile.exists()) {
            Cursor cursor = getAllSteps();
            int results = cursor.getCount();
            if (results < 1)
                return false;
            return true;
        }
        return false;
    }

    public double countDistanceByDay(String date)
    {
        openDB();
        String query = "SELECT SUM("+ DISTANCE +") AS "+ DISTANCE +" FROM " + TABLE_NAME + " WHERE " + DATE+"='"+date+"'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(DISTANCE));
        }
        return 0;
    }

    public double countDistance()
    {
        openDB();
        String query = "SELECT SUM("+ DISTANCE +") AS "+ DISTANCE +" FROM " + TABLE_NAME + ";";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(DISTANCE));
        }
        return 0;
    }

}

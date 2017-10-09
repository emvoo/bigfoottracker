package uk.ac.solent.marcinwisniewski.bigfoottracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class UserDetailsDB extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private static final int DBVERSION = 1;
    private static final String DBNAME = "user_details.db";
    private static final String TABLE_NAME = "user_details";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CREATE_USER_DETAILS_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                    + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USERNAME + " TEXT, "
                    + PASSWORD + " TEXT);";


    public UserDetailsDB(Context context)
    {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_DETAILS_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

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

    /**
     * Store user details.
     * @param username
     * @param password
     * @return
     */
    public long insert(String username, String password)
    {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        return db.insert(TABLE_NAME, null, values);
    }

    public int update(int id, String username, String password)
    {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        String where = "id = " +id;
        return db.update(TABLE_NAME, values, where, null);
    }

    public int delete(int id)
    {
        String where = "id = " +id;
        return db.delete(TABLE_NAME, where, null);
    }

    public Cursor getAllUsers()
    {
        openDB();
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    public Cursor getById(int id)
    {
        String whereArgs = "id = " + id;
        return db.query(true, TABLE_NAME, null, whereArgs, null, null, null, null, null);
    }

    public boolean hasRecords(Context context)
    {
        File dbFile = context.getDatabasePath(DBNAME);
        if (dbFile.exists()) {
            Cursor cursor = getAllUsers();
            int results = cursor.getCount();
            if (results < 1)
                return false;
            return true;
        }
        return false;
    }
}

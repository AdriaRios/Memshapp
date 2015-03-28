package org.adriarios.memshapp.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Adrian on 21/03/2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
    // Variable holding the name of the database to be created
    private static final String		DATABASE_NAME = "memories";
    private static final int		DATABASE_VERSION = 1;

    // Variables holding the name of the table and columns to be created
    public static final String		MEMORY_TABLE_NAME = "MEMORY";
    public static final String		_ID = "_id";
    public static final String		MEMORY_TITLE = "tile";
    public static final String		MEMORY_TEXT = "text";
    public static final String		MEMORY_AUDIO = "audio";
    public static final String		MEMORY_VIDEO = "video";
    public static final String		MEMORY_IMAGE = "image";
    public static final String		MEMORY_LATITUDE = "latitude";
    public static final String		MEMORY_LONGITUDE = "longitude";

    // Static variable containing the name of all the table's columns
    static final String[] COLUMNS = {
            _ID,
            MEMORY_TITLE,
            MEMORY_TEXT,
            MEMORY_AUDIO,
            MEMORY_VIDEO,
            MEMORY_IMAGE,
            MEMORY_LATITUDE,
            MEMORY_LONGITUDE
    };

    // Variable holding the SQL instruction to create a new table
    private static final String CREATE_MEMORY_TABLE =
            "CREATE TABLE " + MEMORY_TABLE_NAME + "( " +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MEMORY_TITLE + " TEXT, " +
                    MEMORY_TEXT + " TEXT, " +
                    MEMORY_AUDIO + " TEXT, " +
                    MEMORY_VIDEO + " TEXT, " +
                    MEMORY_IMAGE + " TEXT, " +
                    MEMORY_LATITUDE + " REAL, " +
                    MEMORY_LONGITUDE + " REAL)";
    // Public constructor. Delegates the construction to the SQLiteOpenHelper class
    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create and initialize the database if not present
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_MEMORY_TABLE);
    }

    // Update the database if any changes have occurred (changes in the version number)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

}

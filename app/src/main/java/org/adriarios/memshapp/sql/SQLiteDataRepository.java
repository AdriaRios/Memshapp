package org.adriarios.memshapp.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Adrian on 21/03/2015.
 */
public class SQLiteDataRepository {
    // Helper object to obtain a reference to the database
    private DatabaseOpenHelper databaseOpenHelper;

    // Reference to the SQLiteDatabase to store and retrieve data
    private SQLiteDatabase sqliteDatabase;

    // Private constructor for the SQLiteDataRepository class
    // Creates a new DatabaseOpenHelper object and holds a reference to the actual database
    public SQLiteDataRepository(Context context) {

        this.databaseOpenHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Opens the database for read-only operations
     */
    public void openDatabaseForReadOnly() {

        this.sqliteDatabase = this.databaseOpenHelper.getReadableDatabase();
    }

    /**
     * Opens the database for read and write operations
     */
    public void openDatabaseForWrite() {

        this.sqliteDatabase = this.databaseOpenHelper.getWritableDatabase();
    }

    /**
     * Returns all saved memories.
     *
     * @param projection The names of the columns to fetch.
     *
     * @return An iterator over the list of memories from the database.
     */
    public Cursor fetchAllMemories(String[] projection) {

        Cursor result = this.databaseOpenHelper.getReadableDatabase()
                .query(
                        DatabaseOpenHelper.MEMORY_TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        DatabaseOpenHelper._ID+" DESC");
        return result;
    }

    public long insert(ContentValues values) {

        return this.sqliteDatabase.insert(
                DatabaseOpenHelper.MEMORY_TABLE_NAME,
                null,
                values);
    }

    public int delete(String selection) {
        return this.sqliteDatabase.delete(DatabaseOpenHelper.MEMORY_TABLE_NAME, selection, null);
    }

    /**
     * Release the database resources
     */
    public void release() {

        if ( sqliteDatabase != null ) {
            sqliteDatabase.close();
            sqliteDatabase = null;
        }
    }



}

package org.adriarios.memshapp.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import org.adriarios.memshapp.sql.DatabaseOpenHelper;
import org.adriarios.memshapp.sql.SQLiteDataRepository;

/**
 * Created by Adrian on 21/03/2015.
 */
public class MemoriesProvider extends ContentProvider {
    /* ========COURSES_PROVIDER CONTRACT===============================================
	 * Defines
	 * 		. CONTENT_URI
	 * 		. COURSES_PROVIDER COLUMNS
	 */
    public static final Uri CONTENT_URI = Uri
            .parse("content://org.adriarios.memshapp.sqlite.provider/memories");

    public static final String MEMORY_ID = DatabaseOpenHelper._ID;
    public static final String MEMORY_TITLE = DatabaseOpenHelper.MEMORY_TITLE;
    public static final String MEMORY_TEXT = DatabaseOpenHelper.MEMORY_TEXT;
    public static final String MEMORY_AUDIO = DatabaseOpenHelper.MEMORY_AUDIO;
    public static final String MEMORY_VIDEO = DatabaseOpenHelper.MEMORY_VIDEO;
    public static final String MEMORY_IMAGE = DatabaseOpenHelper.MEMORY_IMAGE;
    public static final String MEMORY_LATITUDE = DatabaseOpenHelper.MEMORY_LATITUDE;
    public static final String MEMORY_LONGITUDE = DatabaseOpenHelper.MEMORY_LONGITUDE;
	/* ================================================================================= */

    private static final int ALL_MEMORIES = 1;
    private static final int MEMORY_BY_ID = 2;

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI("org.adriarios.memshapp.sqlite.provider",
                "memories", ALL_MEMORIES);

        uriMatcher.addURI("org.adriarios.memshapp.sqlite.provider",
                "memories/#", MEMORY_BY_ID);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDataRepository memoriesDb = new SQLiteDataRepository(getContext());
        memoriesDb.openDatabaseForReadOnly();
        Cursor result = null;

        switch ( uriMatcher.match(uri) ) {
            case ALL_MEMORIES:
                result = memoriesDb.fetchAllMemories(projection);
                break;

            case MEMORY_BY_ID:
                //int credits = Integer.parseInt(uri.getLastPathSegment());
                //result = memoriesDb.fetchCourseByCredits(projection, credits);

            default:
                break;
        }


        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if ( uriMatcher.match(uri) == ALL_MEMORIES ) {
            SQLiteDataRepository memoriesDb = new SQLiteDataRepository(getContext());
            memoriesDb.openDatabaseForWrite();

            long rowId = memoriesDb.insert(values);
            if ( rowId > 0 ) {
                Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
                return resultUri;
            }
        }

        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDataRepository memoriesDb = new SQLiteDataRepository(getContext());
        memoriesDb.openDatabaseForWrite();
        return (memoriesDb.delete(selection));

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

}

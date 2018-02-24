package com.proj.abhi.mytermplanner.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.proj.abhi.mytermplanner.utils.DBOpenHelper;
import com.proj.abhi.mytermplanner.utils.Constants;

public class CoursesProvider extends ContentProvider{

    private static final String AUTHORITY = "com.proj.abhi.coursesprovider";
    private static final String BASE_PATH = "courses";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int COURSES = 1;
    private static final int ID = 2;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, COURSES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if (uriMatcher.match(uri) == ID) {
            selection = Constants.ID + "=" + uri.getLastPathSegment();
        }

        return database.query(Constants.Tables.TABLE_COURSE, null,
                selection, null, null, null,
                Constants.CREATED);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(Constants.Tables.TABLE_COURSE,
                null, values);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(Constants.Tables.TABLE_COURSE, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(Constants.Tables.TABLE_COURSE,
                values, selection, selectionArgs);
    }
}

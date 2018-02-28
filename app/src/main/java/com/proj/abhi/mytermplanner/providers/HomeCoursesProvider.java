package com.proj.abhi.mytermplanner.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.Selection;

import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DBOpenHelper;

public class HomeCoursesProvider extends ContentProvider{

    private static final String AUTHORITY = "com.proj.abhi.homecoursesprovider";
    private static final String BASE_PATH = "coursesWithTerm";
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
    //query to join term
    private static final String QUERY_COURSES=
            "SELECT "
                +"c."+Constants.ID+", "
                +"c."+Constants.Course.COURSE_TITLE+" as courseTitle, "
                +"c."+Constants.Course.COURSE_START_DATE+","
                +"c."+Constants.Course.COURSE_END_DATE+","
                +"t."+Constants.Term.TERM_TITLE+" as termTitle"
            +" FROM "+Constants.Tables.TABLE_COURSE+" c "
            +" JOIN "+Constants.Tables.TABLE_TERM+" t on t."+Constants.ID+"=c."+Constants.Ids.TERM_ID+" ";


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(selection!=null){
            return database.rawQuery(QUERY_COURSES+selection,null);
        }else{
            return database.rawQuery(QUERY_COURSES,null);
        }

    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(Constants.Tables.TABLE_COURSE, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

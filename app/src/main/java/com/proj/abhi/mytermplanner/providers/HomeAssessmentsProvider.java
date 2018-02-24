package com.proj.abhi.mytermplanner.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DBOpenHelper;

public class HomeAssessmentsProvider extends ContentProvider{

    private static final String AUTHORITY = "com.proj.abhi.homeassessmentsprovider";
    private static final String BASE_PATH = "assessmentsWithCoursesWithTerm";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int ASSESSMENTS = 1;
    private static final int ID = 2;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, ASSESSMENTS);
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
    private static final String QUERY_ASSESSMENTS=
            "SELECT "
                +"a."+Constants.ID+", "
                +"a."+Constants.Assessment.ASSESSMENT_TITLE+" as assessmentTitle, "
                +"a."+Constants.Assessment.ASSESSMENT_END_DATE+","
                +"a."+Constants.Assessment.TYPE+","
                +"c."+Constants.Course.COURSE_TITLE+" as courseTitle, "
                +"t."+Constants.Term.TERM_TITLE+" as termTitle"
            +" FROM "+Constants.Tables.TABLE_ASSESSMENT+" a "
            +" JOIN "+Constants.Tables.TABLE_COURSE+" c on c."+Constants.ID+"=a."+Constants.Ids.COURSE_ID+" "
            +" JOIN "+Constants.Tables.TABLE_TERM+" t on t."+Constants.ID+"=c."+Constants.Ids.TERM_ID+" ";



    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(selection!=null){
            return database.rawQuery(QUERY_ASSESSMENTS+selection,null);
        }else{
            return database.rawQuery(QUERY_ASSESSMENTS,null);
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
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

package com.proj.abhi.mytermplanner.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;

    //SQL to create table
    private static final String CREATE_TASK=
            "CREATE TABLE " + Constants.Tables.TABLE_TASK + " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Task.TASK_TITLE + " TEXT, " +
                    Constants.Task.TASK_START_DATE + " DATETIME, " +
                    Constants.Task.TASK_END_DATE + " DATETIME, " +
                    Constants.Task.NOTES + " TEXT, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    //SQL to create table
    private static final String CREATE_TERM=
            "CREATE TABLE " + Constants.Tables.TABLE_TERM + " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Term.TERM_TITLE + " TEXT, " +
                    Constants.Term.TERM_START_DATE + " DATETIME, " +
                    Constants.Term.TERM_END_DATE + " DATETIME, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    //SQL to create table
    private static final String CREATE_COURSE=
            "CREATE TABLE " + Constants.Tables.TABLE_COURSE+ " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Ids.TERM_ID + " INTEGER, " +
                    Constants.Course.COURSE_TITLE + " TEXT, " +
                    Constants.Course.COURSE_START_DATE + " DATETIME, " +
                    Constants.Course.COURSE_END_DATE + " DATETIME, " +
                    Constants.Course.NOTES + " TEXT, " +
                    Constants.Course.STATUS + " TEXT, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY("+Constants.Ids.TERM_ID +") REFERENCES " +
                        Constants.Tables.TABLE_TERM+"("+Constants.ID+") ON DELETE CASCADE " +
                    ")";

    //SQL to create table
    private static final String CREATE_ASSESSMENT=
            "CREATE TABLE " + Constants.Tables.TABLE_ASSESSMENT+ " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Ids.COURSE_ID + " INTEGER, " +
                    Constants.Ids.TERM_ID + " INTEGER, " +
                    Constants.Assessment.ASSESSMENT_TITLE + " TEXT, " +
                    Constants.Assessment.ASSESSMENT_END_DATE + " DATETIME, " +
                    Constants.Assessment.NOTES + " TEXT, " +
                    Constants.Assessment.TYPE + " TEXT, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY("+Constants.Ids.COURSE_ID +") REFERENCES " +
                    Constants.Tables.TABLE_COURSE+"("+Constants.ID+") ON DELETE CASCADE, " +
                    "FOREIGN KEY("+Constants.Ids.TERM_ID +") REFERENCES " +
                    Constants.Tables.TABLE_TERM+"("+Constants.ID+") ON DELETE CASCADE " +
                    ")";

    //SQL to create table
    private static final String CREATE_PROFESSOR=
            "CREATE TABLE " + Constants.Tables.TABLE_PROFESSOR+ " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Professor.TITLE + " TEXT, " +
                    Constants.Professor.FIRST_NAME + " TEXT, " +
                    Constants.Professor.MIDDLE_NAME + " TEXT, " +
                    Constants.Professor.LAST_NAME + " TEXT, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP " +
                    ")";

    //SQL to create table
    private static final String CREATE_PHONE=
            "CREATE TABLE " + Constants.Tables.TABLE_PHONE+ " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Professor.PHONE + " TEXT, " +
                    Constants.Professor.PHONE_TYPE + " TEXT, " +
                    Constants.Ids.PROF_ID + " INTEGER, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY("+Constants.Ids.PROF_ID+") REFERENCES " +
                    Constants.Tables.TABLE_PROFESSOR+"("+Constants.ID+") ON DELETE CASCADE " +
                    ")";

    //SQL to create table
    private static final String CREATE_EMAIL=
            "CREATE TABLE " + Constants.Tables.TABLE_EMAIL+ " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Professor.EMAIL + " TEXT, " +
                    Constants.Professor.EMAIL_TYPE + " TEXT, " +
                    Constants.Ids.PROF_ID + " INTEGER, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY("+Constants.Ids.PROF_ID+") REFERENCES " +
                    Constants.Tables.TABLE_PROFESSOR+"("+Constants.ID+") ON DELETE CASCADE " +
                    ")";

    //SQL to create table
    private static final String CREATE_COURSE_PROF=
            "CREATE TABLE " + Constants.Tables.TABLE_COURSE_PROF+ " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.Ids.PROF_ID + " INTEGER, " +
                    Constants.Ids.COURSE_ID + " INTEGER, " +
                    Constants.CREATED + " TEXT default CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY("+Constants.Ids.PROF_ID+") REFERENCES " +
                    Constants.Tables.TABLE_PROFESSOR+"("+Constants.ID+") ON DELETE CASCADE, " +
                    "FOREIGN KEY("+Constants.Ids.COURSE_ID+") REFERENCES " +
                    Constants.Tables.TABLE_COURSE+"("+Constants.ID+") ON DELETE CASCADE " +
                    ")";

    //SQL to create table
    private static final String CREATE_PERSIST_ALARM=
            "CREATE TABLE " + Constants.Tables.TABLE_PERSIST_ALARM+ " (" +
                    Constants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.PersistAlarm.CONTENT_TEXT + " TEXT, " +
                    Constants.PersistAlarm.CONTENT_TITLE+ " TEXT, " +
                    Constants.PersistAlarm.CONTENT_INTENT+ " TEXT, " +
                    Constants.PersistAlarm.CONTENT_URI+ " TEXT, " +
                    Constants.PersistAlarm.USER_OBJECT+ " TEXT, " +
                    Constants.PersistAlarm.NOTIFY_DATETIME + " DATETIME NOT NULL, " +
                    Constants.SharedPreferenceKeys.NOTIFICATION_TYPE+ " INTEGER, " +
                    Constants.Ids.TERM_ID + " INTEGER, " +
                    Constants.Ids.COURSE_ID + " INTEGER, " +
                    Constants.Ids.ASSESSMENT_ID + " INTEGER, " +
                    Constants.Ids.TASK_ID + " INTEGER, " +
                    "FOREIGN KEY("+Constants.Ids.TERM_ID+") REFERENCES " +
                    Constants.Tables.TABLE_TERM+"("+Constants.ID+") ON DELETE CASCADE, " +
                    "FOREIGN KEY("+Constants.Ids.COURSE_ID+") REFERENCES " +
                    Constants.Tables.TABLE_COURSE+"("+Constants.ID+") ON DELETE CASCADE, " +
                    "FOREIGN KEY("+Constants.Ids.ASSESSMENT_ID+") REFERENCES " +
                    Constants.Tables.TABLE_ASSESSMENT+"("+Constants.ID+") ON DELETE CASCADE, " +
                    "FOREIGN KEY("+Constants.Ids.TASK_ID+") REFERENCES " +
                    Constants.Tables.TABLE_TASK+"("+Constants.ID+") ON DELETE CASCADE " +
                    ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TERM);
        db.execSQL(CREATE_COURSE);
        db.execSQL(CREATE_ASSESSMENT);
        db.execSQL(CREATE_PROFESSOR);
        db.execSQL(CREATE_PHONE);
        db.execSQL(CREATE_EMAIL);
        db.execSQL(CREATE_COURSE_PROF);
        db.execSQL(CREATE_TASK);
        db.execSQL(CREATE_PERSIST_ALARM);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=1;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_PHONE);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_EMAIL);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_COURSE_PROF);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_PROFESSOR);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_COURSE);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_ASSESSMENT);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_TERM);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.Tables.TABLE_PERSIST_ALARM);
        onCreate(db);
    }
}

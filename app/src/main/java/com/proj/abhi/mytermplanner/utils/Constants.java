package com.proj.abhi.mytermplanner.utils;

/**
 * Created by Abhi on 2/7/2018.
 */

public final class Constants {

    public static final String ID = "_id";
    public static final String CREATED = "created";
    public static final String APP_NAME = "My Term Planner";
    public static final String CURRENT_URI = "currentUri";
    public static final String CURRENT_INTENT = "currentIntent";

    public final class Tables{
        public static final String TABLE_TERM = "term";
        public static final String TABLE_COURSE= "course";
        public static final String TABLE_ASSESSMENT= "assessment";
        public static final String TABLE_PROFESSOR= "professor";
        public static final String TABLE_PHONE= "phone";
        public static final String TABLE_EMAIL= "email";
        public static final String TABLE_COURSE_PROF= "course_prof";
        public static final String TABLE_PERSIST_ALARM = "persist_alarm";
        public static final String TABLE_TASK = "task";
    }

    public final class Ids{
        public static final String TERM_ID= "termId";
        public static final String COURSE_ID= "courseId";
        public static final String ASSESSMENT_ID = "assessmentId";
        public static final String PROF_ID= "profId";
        public static final String ALARM_ID="alarmId";
        public static final String TASK_ID="taskId";
    }

    public final class Term{
        public static final String TERM_TITLE = "title";
        public static final String TERM_START_DATE = "startDate";
        public static final String TERM_END_DATE = "endDate";
    }

    public final class Task{
        public static final String TASK_TITLE = "title";
        public static final String TASK_START_DATE = "startDate";
        public static final String TASK_END_DATE = "endDate";
        public static final String NOTES = "notes";
    }

    public final class Course{
        public static final String COURSE_TITLE = "title";
        public static final String COURSE_START_DATE = "startDate";
        public static final String COURSE_END_DATE = "endDate";
        public static final String NOTES = "notes";
        public static final String STATUS = "status";
    }

    public final class Assessment{
        public static final String ASSESSMENT_TITLE = "title";
        public static final String ASSESSMENT_END_DATE = "endDate";
        public static final String NOTES = "notes";
        public static final String TYPE = "type";
    }

    public final class Professor{
        public static final String FIRST_NAME= "firstName";
        public static final String MIDDLE_NAME= "middleName";
        public static final String LAST_NAME= "lastName";
        public static final String TITLE= "title";
        public static final String PHONE_TYPE= "phoneType";
        public static final String PHONE= "phone";
        public static final String EMAIL_TYPE= "emailType";
        public static final String EMAIL= "email";
    }

    public final class MenuGroups{
        public static final int TERM_GROUP=-1;
        public static final int COURSE_GROUP=-2;
        public static final int ASSESSMENT_GROUP=-3;
        public static final int MANAGEMENT_GROUP=-4;
        public static final int PROF_GROUP=-5;
        public static final int TASK_GROUP =-6;
    }

    public final class ActionBarIds{
        public static final int ADD_REMINDER = -1;
        public static final int ADD_TERM = -2;
        public static final int ADD_PROF = -3;
        public static final int USER_PREFS = -4;
        public static final int ADD_EMAIL = -5;
        public static final int ADD_PHONE = -6;
        public static final int ADD_TASK = -7;
    }

    public final class CursorLoaderIds{
        public static final int TERM_ID=0;
        public static final int COURSE_ID=1;
        public static final int ASSESSMENT_ID=2;
        public static final int HOME_COURSE_ID=3;
        public static final int HOME_ASSESSMENT_ID=4;
        public static final int PROF_ID=5;
        public static final int PHONE_ID=6;
        public static final int EMAIL_ID =7;
        public static final int COURSE_PROF_ID_INCLUDE= -8;
        public static final int COURSE_PROF_ID_EXCLUDE= -9;
        public static final int TASK_ID = -10;
    }

    public final class PersistAlarm{
        public static final String CONTENT_TITLE="title";
        public static final String CONTENT_TEXT="text";
        public static final String CONTENT_URI="uri";
        public static final String USER_OBJECT="userObject";
        public static final String NOTIFY_DATETIME="notifyDatetime";
        public static final String CONTENT_INTENT = "intent";
        public static final String USER_BUNDLE="userBundle";
    }

    public final class SharedPreferenceKeys{
        public static final String USER_PREFS="MyPrefs";
        public static final String NUM_QUERY_DAYS="numQueryDays";
        public static final String DEFAULT_TAB="defaultTab";
        public static final String NOTIFICATION_RED="notification_red";
        public static final String NOTIFICATION_GREEN="notification_green";
        public static final String NOTIFICATION_BLUE="notification_blue";
        public static final String NOTIFICATION_VIBRATE_PATTERN="notification_vibrate_pattern";
    }

    public final class CoursesProfsSql{
        public static final String QUERY_RELATIONSHIP=
                "SELECT "
                        +"p."+Constants.ID+", "
                        +"p."+Constants.Professor.TITLE+", "
                        +"p."+Constants.Professor.FIRST_NAME+", "
                        +"p."+Constants.Professor.MIDDLE_NAME+", "
                        +"p."+Constants.Professor.LAST_NAME+" "
                        +" FROM "+Constants.Tables.TABLE_COURSE_PROF+" cp "
                        +" JOIN "+Constants.Tables.TABLE_PROFESSOR+" p on p."+Constants.ID+" = cp."+ Ids.PROF_ID+" ";

        public static final String QUERY_NON_RELATIONSHIP=
                "SELECT "
                        +"p."+Constants.ID+", "
                        +"p."+Constants.Professor.TITLE+" ||' '|| "
                        +"p."+Constants.Professor.FIRST_NAME+" ||' '|| "
                        +"p."+Constants.Professor.MIDDLE_NAME+" ||' '|| "
                        +"p."+Constants.Professor.LAST_NAME+" as fullName"
                        +" FROM "+ Tables.TABLE_PROFESSOR+" p "
                        +" LEFT JOIN "+ Tables.TABLE_COURSE_PROF+" cp on cp."+Ids.PROF_ID+" = p."+Constants.ID+" ";

    }
}

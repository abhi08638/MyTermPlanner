package com.proj.abhi.mytermplanner.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.proj.abhi.mytermplanner.services.NotifyService;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DBOpenHelper;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            DBOpenHelper helper = new DBOpenHelper(context);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor c = database.rawQuery("SELECT * FROM "+Constants.Tables.TABLE_PERSIST_ALARM,null);
            initAlarms(c,context);
            c.close();
            database.close();
        }
    }

    public void initAlarms(Cursor c,Context context){
        Bundle userBundle = new Bundle();
        Intent sendingIntent;
        Uri uri;
        if(c.getCount()>=1){
            while(c.moveToNext()){
                try {
                    sendingIntent = Intent.parseUri(c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_INTENT)),0);
                    if(c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_URI))!=null){
                        uri = Uri.parse(c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_URI)));
                        sendingIntent.putExtra(Constants.CURRENT_URI,uri);
                    }
                    userBundle.putParcelable(Constants.CURRENT_INTENT,sendingIntent);
                    userBundle.putInt(Constants.Ids.ALARM_ID,c.getInt(c.getColumnIndex(Constants.ID)));
                    userBundle.putString(Constants.PersistAlarm.CONTENT_TITLE,c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_TITLE)));
                    userBundle.putString(Constants.PersistAlarm.CONTENT_TEXT,c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_TEXT)));
                    userBundle.putString(Constants.PersistAlarm.USER_OBJECT,c.getString(c.getColumnIndex(Constants.PersistAlarm.USER_OBJECT)));
                    userBundle.putInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE,c.getInt(c.getColumnIndex(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE)));
                    userBundle.putInt(Constants.Ids.TERM_ID, c.getInt(c.getColumnIndex(Constants.Ids.TERM_ID)));
                    userBundle.putInt(Constants.Ids.COURSE_ID, c.getInt(c.getColumnIndex(Constants.Ids.COURSE_ID)));
                    userBundle.putInt(Constants.Ids.ASSESSMENT_ID, c.getInt(c.getColumnIndex(Constants.Ids.ASSESSMENT_ID)));
                    userBundle.putInt(Constants.Ids.TASK_ID, c.getInt(c.getColumnIndex(Constants.Ids.TASK_ID)));
                    String dateTime = c.getString(c.getColumnIndex(Constants.PersistAlarm.NOTIFY_DATETIME));

                    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(context, NotifyService.class);
                    intent.putExtra(NotifyService.INTENT_NOTIFY, true);
                    intent.putExtra(Constants.PersistAlarm.USER_BUNDLE, userBundle);

                    //this id is important to allow the message in the notification to update
                    PendingIntent pendingIntent = PendingIntent.getService(context, c.getInt(c.getColumnIndex(Constants.ID)), intent, 0);
                    am.set(AlarmManager.RTC, getCalendar(dateTime).getTimeInMillis(), pendingIntent);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Calendar getCalendar(String dateTime){
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = iso8601Format.parse(dateTime);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            return c;
        } catch (Exception e) {
            return Calendar.getInstance();
        }
    }


}
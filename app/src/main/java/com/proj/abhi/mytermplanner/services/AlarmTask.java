package com.proj.abhi.mytermplanner.services;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DBOpenHelper;
import com.proj.abhi.mytermplanner.utils.Utils;

public class AlarmTask implements Runnable{
    private final Date date;
    private final AlarmManager am;
    private final Context context;
    private Bundle userBundle;
    private SQLiteDatabase database;

    public AlarmTask(Context context, Date date,Bundle userBundle) {
        this.context = context;
        this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.date = date;
        this.userBundle=userBundle;
    }

    @Override
    public void run() {
            DBOpenHelper helper = new DBOpenHelper(context);
            database = helper.getWritableDatabase();
            int id = insertInDb();
            userBundle.putInt(Constants.Ids.ALARM_ID, id);
            Intent intent = new Intent(context, NotifyService.class);
            intent.putExtra(NotifyService.INTENT_NOTIFY, true);
            intent.putExtra(Constants.PersistAlarm.USER_BUNDLE, userBundle);

            //this id is important to allow the message in the notification to update
            PendingIntent pendingIntent = PendingIntent.getService(context, id, intent, 0);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            am.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
            database.close();
    }

    public void cancelAlarms(String where){
        DBOpenHelper helper = new DBOpenHelper(context);
        database = helper.getWritableDatabase();
        Cursor c = database.rawQuery("Select * from "+Constants.Tables.TABLE_PERSIST_ALARM+where,null);
        if(c.getCount()>=1){
            int id;
            while(c.moveToNext()){
                id=c.getInt(c.getColumnIndex(Constants.ID));
                Intent intent = new Intent(context, NotifyService.class);
                PendingIntent pendingIntent = PendingIntent.getService(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                am.cancel(pendingIntent);
            }
        }
        c.close();
        database.close();
    }
    private int insertInDb(){
        ContentValues values = new ContentValues();
        Intent sendingIntent = userBundle.getParcelable(Constants.CURRENT_INTENT);
        values.put(Constants.PersistAlarm.CONTENT_TEXT,userBundle.get(Constants.PersistAlarm.CONTENT_TEXT).toString());
        values.put(Constants.PersistAlarm.CONTENT_URI,sendingIntent.getParcelableExtra(Constants.CURRENT_URI).toString());
        values.put(Constants.PersistAlarm.CONTENT_INTENT,sendingIntent.toUri(0));
        values.put(Constants.PersistAlarm.CONTENT_TITLE,userBundle.get(Constants.PersistAlarm.CONTENT_TITLE).toString());
        values.put(Constants.PersistAlarm.USER_OBJECT,userBundle.get(Constants.PersistAlarm.USER_OBJECT).toString());
        values.put(Constants.PersistAlarm.NOTIFY_DATETIME, Utils.getDbDateTime(date));
        values=Utils.addTableId(values,userBundle);
        long id=database.insert(Constants.Tables.TABLE_PERSIST_ALARM,null,values);
        Long l = new Long(id);
        return l.intValue();
    }

}
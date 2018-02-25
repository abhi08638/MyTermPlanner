package com.proj.abhi.mytermplanner.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DBOpenHelper;
import com.proj.abhi.mytermplanner.utils.Utils;

public class NotifyService extends Service {

    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    public static final String INTENT_NOTIFY = "com.proj.abhi.mytermmanger.services.INTENT_NOTIFY";
    private NotificationManager mNM;
    private int r=0,g=0,b=255;
    private long[] pattern = {0, 500, 200,500 };
 
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initPrefs();
    }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getBooleanExtra(INTENT_NOTIFY, false))
            showNotification(intent);

        return START_NOT_STICKY;
    }

    private void initPrefs(){
        SharedPreferences sharedpreferences = getSharedPreferences(Constants.SharedPreferenceKeys.USER_PREFS, Context.MODE_PRIVATE);
        if(!sharedpreferences.contains(Constants.SharedPreferenceKeys.NOTIFICATION_RED)){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.NOTIFICATION_RED, Integer.toString(0));
            editor.apply();
        }else{
            r=Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.NOTIFICATION_RED,null));
        }
        if(!sharedpreferences.contains(Constants.SharedPreferenceKeys.NOTIFICATION_GREEN)){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.NOTIFICATION_GREEN, Integer.toString(0));
            editor.apply();
        }else{
            g=Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.NOTIFICATION_GREEN,null));
        }
        if(!sharedpreferences.contains(Constants.SharedPreferenceKeys.NOTIFICATION_BLUE)){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.NOTIFICATION_BLUE, Integer.toString(255));
            editor.apply();
        }else{
            b=Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.NOTIFICATION_BLUE,null));
        }
        String patternString="";
        if(!sharedpreferences.contains(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN)){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            for(long l:pattern) {
                patternString+=Long.toString(l)+",";
            }
            editor.putString(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN, patternString.substring(0,patternString.length()-1));
            editor.apply();
        }else{
            patternString=sharedpreferences.getString(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN,null);
            String[] longs=patternString.split(",");
            pattern=new long[longs.length];
            for(int i=0;i<longs.length-1;i++){
                pattern[i]=Long.parseLong(longs[i]);
            }
        }
    }
 
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new ServiceBinder();

    private void showNotification(Intent userIntent) {
        long time = System.currentTimeMillis();
        Bundle bundle=userIntent.getBundleExtra(Constants.PersistAlarm.USER_BUNDLE);
        String contextText = bundle.getString(Constants.PersistAlarm.CONTENT_TEXT);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent sendingIntent=bundle.getParcelable(Constants.CURRENT_INTENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),sendingIntent, 0);
        int id = bundle.getInt(Constants.Ids.ALARM_ID);
        String NOTIFICATION_CHANNEL_ID = "plannerChannel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);

                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                // Configure the notification channel.
                notificationChannel.setDescription("Planner Channel");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.rgb(r,g,b));
                notificationChannel.setVibrationPattern(pattern);
                notificationChannel.enableVibration(true);
                notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),att);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }

            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(Utils.getProperName(bundle.getString(Constants.PersistAlarm.USER_OBJECT))
                    +": "+bundle.get(Constants.PersistAlarm.CONTENT_TITLE))
                    .setContentInfo(Constants.APP_NAME)
                    .setSmallIcon(getNotificationIcon())
                    .setAutoCancel(true)
                    .setLights(Color.rgb(r,g,b), 500, 500)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);
            Notification notification = new Notification.BigTextStyle(builder).bigText(contextText).build();
            mNotificationManager.notify(id, notification);
        }else{
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
            mBuilder.setContentIntent(pendingIntent);

            mBuilder.setSmallIcon(getNotificationIcon());
            mBuilder.setContentTitle(Utils.getProperName(bundle.getString(Constants.PersistAlarm.USER_OBJECT))
                    +": "+bundle.get(Constants.PersistAlarm.CONTENT_TITLE));
            mBuilder.setContentText(contextText);
            mBuilder.setContentInfo(Constants.APP_NAME);
            mBuilder.setAutoCancel(true);
            mBuilder.setWhen(time);
            mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
            mBuilder.setVibrate(pattern);
            mBuilder.setLights(Color.rgb(r,g,b), 500, 500);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            mNotificationManager.notify(id, mBuilder.build());
        }

        deleteInDb(id);
        stopSelf();
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_stat_book : R.mipmap.ic_launcher;
    }

    private void deleteInDb(int id){
        DBOpenHelper helper = new DBOpenHelper(this);
        SQLiteDatabase database = helper.getWritableDatabase();
        String where=Constants.ID+"="+id;
        database.delete(Constants.Tables.TABLE_PERSIST_ALARM,where,null);
        database.close();
    }
}
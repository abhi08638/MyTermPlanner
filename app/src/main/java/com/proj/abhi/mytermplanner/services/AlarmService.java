package com.proj.abhi.mytermplanner.services;
 
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class AlarmService extends Service {

    private final IBinder mBinder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        AlarmService getService() {
            return AlarmService.this;
        }
    }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
 
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setAlarm(Date d, Bundle userBundle) {
        new AlarmTask(this, d, userBundle).run();
    }

    /*public void cancelAlarm(Date d, Bundle userBundle) {
        new AlarmTask(this, d, userBundle).run();
    }

    public void cancelAlarms(Bundle userBundle) {
        new AlarmTask(this,null, userBundle).cancelAlarms();
    }*/
}
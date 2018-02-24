package com.proj.abhi.mytermplanner.services;
 
import java.util.Calendar;
import java.util.Date;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class AlarmClient {

    private AlarmService mBoundService;
    private Context mContext;
    private boolean mIsBound;

    public AlarmClient(Context context) {
        mContext = context;
    }

    public void doBindService() {
        mContext.bindService(new Intent(mContext, AlarmService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            AlarmService.ServiceBinder binder=(AlarmService.ServiceBinder) service;
            mBoundService=binder.getService();
        }
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    public void setAlarmForNotification(Date d, Bundle userBundle){
        mBoundService.setAlarm(d, userBundle);
    }

    public void doUnbindService() {
        if (mIsBound) {
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }
}
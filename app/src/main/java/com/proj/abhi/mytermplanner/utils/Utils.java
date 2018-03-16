package com.proj.abhi.mytermplanner.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.activities.HomeActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Abhi on 2/8/2018.
 */

public class Utils {
    public static Context context;
    public static String userDatePattern = "MMM dd, yyyy";
    public static String userTimePattern = "HH:mm";
    public static String dbDateTimePattern = "yyyy-MM-dd HH:mm:ss";
    public static String userDateTimePattern = userDatePattern + " " + userTimePattern;
    public static SimpleDateFormat userTimeFormat = new SimpleDateFormat(userTimePattern);
    public static SimpleDateFormat userDateFormat = new SimpleDateFormat(userDatePattern);
    public static SimpleDateFormat dbDateTimeFormat = new SimpleDateFormat(dbDateTimePattern);
    public static SimpleDateFormat userDateTimeFormat = new SimpleDateFormat(userDateTimePattern);

    public static boolean isValidDate(String date) throws CustomException {

        return true;
    }

    public static String getDbDateTime(String date) {
        try {
            Date newDate = getDateFromUser(date);
            return getDbDateTime(newDate);
        } catch (Exception e) {
            return date;
        }
    }

    public static String getDbDateTime(Date date) {
        try {
            return dbDateTimeFormat.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserDate(String date) {
        try {
            Date newDate = dbDateTimeFormat.parse(date);
            return userDateFormat.format(newDate);
        } catch (Exception e) {
            return date;
        }
    }

    public static String getUserTime(Date newDate) {
        try {
            String ampm;
            if (newDate.getHours() >= 12) {
                if (newDate.getHours() != 12)
                    newDate.setHours(newDate.getHours() - 12);
                ampm = " PM";
            } else {
                if (newDate.getHours() == 0) {
                    newDate.setHours(12);
                }
                ampm = " AM";
            }
            return userTimeFormat.format(newDate) + ampm;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserTime(String date) {
        try {
            Date newDate = dbDateTimeFormat.parse(date);
            return getUserTime(newDate);
        } catch (Exception e) {
            return date;
        }
    }

    public static Date getDateFromUser(String date) {
        try {
            Date newDate = userDateFormat.parse(date);
            return newDate;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentDate() {
        try {
            Date newDate = new Date();
            return userDateFormat.format(newDate);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isBefore(String startDate, String endDate) throws CustomException {
        try {
            if (userDateFormat.parse(startDate).after(userDateFormat.parse(endDate))) {
                throw new CustomException("Start Date must be before End Date");
            }
        } catch (Exception e) {
            if (e instanceof CustomException) {
                throw new CustomException(e.getMessage());
            } else {
                throw new CustomException("Invalid Date");
            }
        }
        return true;
    }

    public static Date getDateTimeFromUser(String date, String time, boolean eod) throws CustomException {
        try {
            Date newDate;
            if (hasValue(time)) {
                int hourOffset = 0;
                if (time.contains("AM")) {
                    if (time.startsWith("12"))
                        hourOffset = -12;
                    else
                        hourOffset = 0;
                } else {
                    if (time.startsWith("12"))
                        hourOffset = 0;
                    else
                        hourOffset = 12;
                }
                time = time.substring(0, time.indexOf(":") + 2);
                newDate = userDateTimeFormat.parse(date + " " + time);
                newDate.setHours(newDate.getHours() + hourOffset);
            } else {
                if (eod)
                    newDate = userDateTimeFormat.parse(date + " 23:59");
                else
                    newDate = userDateTimeFormat.parse(date + " 00:00");
            }
            return newDate;
        } catch (Exception e) {
            throw new CustomException("Invalid Date Time Format");
        }
    }

    public static ContentValues addTableId(ContentValues values, Bundle b) {
        String userObj = b.getString(Constants.PersistAlarm.USER_OBJECT)!=null?b.getString(Constants.PersistAlarm.USER_OBJECT):"";
        if (userObj.equals(Constants.Tables.TABLE_TERM)) {
            values.put(Constants.Ids.TERM_ID, b.getInt(Constants.Ids.TERM_ID));
        } else if (userObj.equals(Constants.Tables.TABLE_COURSE)) {
            values.put(Constants.Ids.TERM_ID, b.getInt(Constants.Ids.TERM_ID));
            values.put(Constants.Ids.COURSE_ID, b.getInt(Constants.Ids.COURSE_ID));
        } else if (userObj.equals(Constants.Tables.TABLE_ASSESSMENT)) {
            values.put(Constants.Ids.TERM_ID, b.getInt(Constants.Ids.TERM_ID));
            values.put(Constants.Ids.COURSE_ID, b.getInt(Constants.Ids.COURSE_ID));
            values.put(Constants.Ids.ASSESSMENT_ID, b.getInt(Constants.Ids.ASSESSMENT_ID));
        } else if (userObj.equals(Constants.Tables.TABLE_TASK)) {
            values.put(Constants.Ids.TASK_ID, b.getInt(Constants.Ids.TASK_ID));
        }
        return values;
    }

    public static String getSqlDateNow() {
        long offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        offset = offset / 1000;
        String format = "'%Y-%m-%d','now','" + offset + " seconds'";
        return format;
    }

    public static boolean hasValue(String val) {
        if (val != null && !val.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static String getProperName(String word) {
        if (hasValue(word)) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }
        return Constants.APP_NAME;
    }

    public static void sendToActivity(int id, Class toActivity, Uri contentUri, LinkedHashMap<String,Integer> params) {
        try {
            Intent intent = new Intent(context, toActivity);
            Uri uri = Uri.parse(contentUri + "/" + id);
            intent.putExtra(Constants.CURRENT_URI, uri);
            if(params!=null){
                for(Map.Entry<String,Integer> entry: params.entrySet()){
                    intent.putExtra(entry.getKey(),entry.getValue());
                }
            }
            if(toActivity.equals(HomeActivity.class)){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                ((Activity) context).startActivity(intent);
            }
            ((Activity) context).startActivityForResult(intent, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToActivity(int id, Class toActivity, Uri contentUri) {
        sendToActivity(id, toActivity, contentUri,null);
    }

    public static void closeKeyboard() {
        try {
            Activity act = (Activity) context;
            InputMethodManager inputManager = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == act.getCurrentFocus()) ? null : act.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CompoundButton.OnCheckedChangeListener getCbListener(){
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    buttonView.setText(R.string.on);
                else
                    buttonView.setText(R.string.off);
            }
        };
    }

}

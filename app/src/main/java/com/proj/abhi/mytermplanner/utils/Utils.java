package com.proj.abhi.mytermplanner.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Abhi on 2/8/2018.
 */

public class Utils {

    public static String userDatePattern = "MMM dd, yyyy";
    public static String dbDateTimePattern = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat userDateFormat = new SimpleDateFormat(userDatePattern);
    public static SimpleDateFormat dbDateTimeFormat = new SimpleDateFormat(dbDateTimePattern);

    public static boolean isValidDate(String date) throws CustomException{

        return true;
    }

    public static String getDbDateTime(String date){
        try{
            Date newDate=getDateFromUser(date);
            return getDbDateTime(newDate);
        }catch (Exception e){
            return date;
        }
    }

    public static String getDbDateTime(Date date){
        try{
            return dbDateTimeFormat.format(date);
        }catch (Exception e){
            return null;
        }
    }

    public static String getUserDate(String date){
        try{
            Date newDate=dbDateTimeFormat.parse(date);
            return userDateFormat.format(newDate);
        }catch (Exception e){
            return date;
        }
    }

    public static String getUserTime(String date){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat userFormat = new SimpleDateFormat("HH:mm");
            Date newDate=sdf.parse(date);
            String ampm;
            if(newDate.getHours()>=12){
                if(newDate.getHours()!=12)
                    newDate.setHours(newDate.getHours()-12);
                ampm=" PM";
            }else{
                ampm=" AM";
            }
            return userFormat.format(newDate)+ampm;
        }catch (Exception e){
            return date;
        }
    }

    public static Date getDateFromUser(String date){
        try{
            Date newDate=userDateFormat.parse(date);
            return newDate;
        }catch (Exception e){
            return null;
        }
    }

    public static String getCurrentDate(){
        try{
            Date newDate=new Date();
            return userDateFormat.format(newDate);
        }catch (Exception e){
            return null;
        }
    }


    public static boolean isBefore(String startDate,String endDate) throws CustomException{
        try{
            if(userDateFormat.parse(startDate).after(userDateFormat.parse(endDate))){
                throw new CustomException("Start Date must be before End Date");
            }
        }catch(Exception e){
            if(e instanceof CustomException){
                throw new CustomException(e.getMessage());
            }
            else{
                throw new CustomException("Invalid Date");
            }
        }
        return true;
    }

    public static ContentValues addTableId(ContentValues values,Bundle b){
        if(b.get(Constants.PersistAlarm.USER_OBJECT).equals(Constants.Tables.TABLE_TERM)){
            values.put(Constants.Ids.TERM_ID,b.getInt(Constants.Ids.TERM_ID));
        }else if(b.get(Constants.PersistAlarm.USER_OBJECT).equals(Constants.Tables.TABLE_COURSE)){
            values.put(Constants.Ids.TERM_ID,b.getInt(Constants.Ids.TERM_ID));
            values.put(Constants.Ids.COURSE_ID,b.getInt(Constants.Ids.COURSE_ID));
        }else if(b.get(Constants.PersistAlarm.USER_OBJECT).equals(Constants.Tables.TABLE_ASSESSMENT)){
            values.put(Constants.Ids.TERM_ID,b.getInt(Constants.Ids.TERM_ID));
            values.put(Constants.Ids.COURSE_ID,b.getInt(Constants.Ids.COURSE_ID));
            values.put(Constants.Ids.ASSESSMENT_ID,b.getInt(Constants.Ids.ASSESSMENT_ID));
        }else if(b.get(Constants.PersistAlarm.USER_OBJECT).equals(Constants.Tables.TABLE_TASK)){
            values.put(Constants.Ids.TASK_ID,b.getInt(Constants.Ids.TASK_ID));
        }
        return values;
    }

    public static String getSqlDateNow(){
        long offset= TimeZone.getDefault().getOffset(System.currentTimeMillis());
        offset=offset/1000;
        String format = "'%Y-%m-%d','now','"+offset+" seconds'";
        return format;
    }

    public static boolean hasValue(String val){
        if(val!=null && !val.trim().equals("")){
            return true;
        }
        return false;
    }

    public static String getProperName(String word){
        if(hasValue(word)){
            return word.substring(0,1).toUpperCase()+word.substring(1);
        }
        return word;
    }

    public static void sendToActivity(int id, Context context,Class toActivity,Uri contentUri){
        try{
            Intent intent = new Intent(context, toActivity);
            Uri uri = Uri.parse(contentUri + "/" + id);
            intent.putExtra(Constants.CURRENT_URI, uri);
            ((Activity)context).startActivityForResult(intent,0);
        }catch (Exception e){
            new CustomException("No caller Activity provided");
        }
    }

}

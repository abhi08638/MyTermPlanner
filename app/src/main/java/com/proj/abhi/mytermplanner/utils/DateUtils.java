package com.proj.abhi.mytermplanner.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Abhi on 2/8/2018.
 */

public class DateUtils {

    public static String userDatePattern = "MMM dd, yyyy";
    public static String userTimePattern = "HH:mm";
    public static String dbDateTimePattern = "yyyy-MM-dd HH:mm:ss";
    public static String userDateTimePattern = userDatePattern+" "+userTimePattern;
    public static SimpleDateFormat userTimeFormat = new SimpleDateFormat(userTimePattern);
    public static SimpleDateFormat userDateFormat = new SimpleDateFormat(userDatePattern);
    public static SimpleDateFormat dbDateTimeFormat = new SimpleDateFormat(dbDateTimePattern);
    public static SimpleDateFormat userDateTimeFormat = new SimpleDateFormat(userDateTimePattern);

    public static Date getDateTimeFromDb(String date){
        try{
            Date newDate=dbDateTimeFormat.parse(date);
            return newDate;
        }catch (Exception e){
            return null;
        }
    }

    public static String getUserDateTime(Date date){
        try{
            return getUserDate(date)+" "+getUserTime(date);
        }catch (Exception e){
            return null;
        }
    }

    public static String getUserDate(Date date){
        try{
            return userDateFormat.format(date);
        }catch (Exception e){
            return null;
        }
    }

    public static String getUserTime(Date date){
        try{
            Date newDate=new Date();
            newDate.setTime(date.getTime());
            String ampm;
            if(newDate.getHours()>=12){
                if(newDate.getHours()!=12)
                    newDate.setHours(newDate.getHours()-12);
                ampm=" PM";
            }else{
                if(newDate.getHours()==0){
                    newDate.setHours(12);
                }
                ampm=" AM";
            }
            return userTimeFormat.format(newDate)+ampm;
        }catch (Exception e){
            return null;
        }
    }

    public static String getDbDate(Date date){
        try{
            return dbDateTimeFormat.format(date);
        }catch (Exception e){
            return null;
        }
    }

    public static Date getDateTimeFromUser(String date, String time,boolean eod){
        try{
            Date newDate;
            if(Utils.hasValue(time)){
                int hourOffset=0;
                if(time.contains("AM")){
                    if(time.startsWith("12"))
                        hourOffset=-12;
                    else
                        hourOffset=0;
                }else{
                    if(time.startsWith("12"))
                        hourOffset=0;
                    else
                        hourOffset=12;
                }
                time=time.substring(0,time.indexOf(":")+3);
                newDate = userDateTimeFormat.parse(date+" "+time);
                newDate.setHours(newDate.getHours()+hourOffset);
            }else{
                if(eod)
                    newDate = userDateTimeFormat.parse(date+" 23:59");
                else
                    newDate = userDateTimeFormat.parse(date+" 00:00");
            }
            return newDate;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isBefore(Date startDate,Date endDate) throws CustomException{
        try{
            if(!startDate.before(endDate)){
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

    public static String getCurrentDate(){
        try{
            Date newDate=new Date();
            return userDateFormat.format(newDate);
        }catch (Exception e){
            return null;
        }
    }

    //////////////////////////////////////////////////////////////////////

    public static String getSqlDateNowStart(){
        long offset= TimeZone.getDefault().getOffset(System.currentTimeMillis());
        offset=offset/1000;
        String format = "'%Y-%m-%d %H:%M:%S','now','"+offset+" seconds','start of day'";
        return format;
    }

    public static String getSqlDateNowEnd(){
        long offset= TimeZone.getDefault().getOffset(System.currentTimeMillis());
        offset=offset/1000;
        String format = "'%Y-%m-%d 23:59:59','now','"+offset+" seconds'";
        return format;
    }
}

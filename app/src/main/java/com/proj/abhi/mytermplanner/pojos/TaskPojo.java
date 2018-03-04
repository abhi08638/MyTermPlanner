package com.proj.abhi.mytermplanner.pojos;

import android.database.Cursor;

import com.proj.abhi.mytermplanner.generics.GenericDetailPojo;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;

import java.util.Date;

/**
 * Created by Abhi on 2/19/2018.
 */

public class TaskPojo extends GenericDetailPojo{
    private String title,notes;
    private Date startDate;
    private Date endDate;

    public TaskPojo() {
        className="task";
    }

    public void reset() {
        title=null;
        notes=null;
        startDate=null;
        endDate=null;
    }

    public void initPojo(Cursor c){
        title=c.getString(c.getColumnIndex(Constants.Task.TASK_TITLE));
        notes=c.getString(c.getColumnIndex(Constants.Task.NOTES));
        startDate= DateUtils.getDateTimeFromDb(c.getString(c.getColumnIndex(Constants.Task.TASK_START_DATE)));
        endDate= DateUtils.getDateTimeFromDb(c.getString(c.getColumnIndex(Constants.Task.TASK_END_DATE)));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}

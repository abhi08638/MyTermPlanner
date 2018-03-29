package com.proj.abhi.mytermplanner.pojos;

import android.database.Cursor;
import android.util.Log;

import com.proj.abhi.mytermplanner.generics.GenericDetailPojo;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;

import java.util.Date;

/**
 * Created by Abhi on 2/19/2018.
 */

public class CoursePojo extends GenericDetailPojo{
    private String title,notes,status;
    private Date startDate;
    private Date endDate;
    private int termId;
    private String[] projection = { Constants.ID, Constants.Ids.TERM_ID,Constants.Course.COURSE_TITLE,Constants.Course.COURSE_START_DATE,
                                    Constants.Course.COURSE_END_DATE,Constants.Course.NOTES,Constants.Course.STATUS};

    public CoursePojo() {
        className="course";
    }

    public void reset() {
        title=null;
        notes=null;
        startDate=null;
        endDate=null;
        status=null;
    }

    public void initPojo(Cursor c){
        try{
            title=c.getString(c.getColumnIndex(Constants.Course.COURSE_TITLE));
            notes=c.getString(c.getColumnIndex(Constants.Course.NOTES));
            status=c.getString(c.getColumnIndex(Constants.Course.STATUS));
        }catch (Exception e){
            title=c.getString(c.getColumnIndex("courseTitle"));
        }
        startDate= DateUtils.getDateTimeFromDb(c.getString(c.getColumnIndex(Constants.Course.COURSE_START_DATE)));
        endDate= DateUtils.getDateTimeFromDb(c.getString(c.getColumnIndex(Constants.Course.COURSE_END_DATE)));
        termId=c.getInt(c.getColumnIndex(Constants.Ids.TERM_ID));
    }

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
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

    public String[] getProjection() {
        return projection;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

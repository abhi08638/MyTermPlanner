package com.proj.abhi.mytermplanner.pojos;

import android.database.Cursor;
import android.widget.Spinner;

import com.proj.abhi.mytermplanner.generics.GenericDetailPojo;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;

import java.util.Date;

/**
 * Created by Abhi on 2/19/2018.
 */

public class ProfessorPojo extends GenericDetailPojo{
    private String firstName,middleName,lastName;
    private int titleIndex;

    public ProfessorPojo() {
        className="professor";
    }

    public void reset() {
        titleIndex=0;
        firstName=null;
        middleName=null;
        lastName=null;
    }

    @Override
    public void initPojo(Cursor c) {
        firstName=c.getString(c.getColumnIndex(Constants.Professor.FIRST_NAME));
        middleName=c.getString(c.getColumnIndex(Constants.Professor.MIDDLE_NAME));
        lastName=c.getString(c.getColumnIndex(Constants.Professor.LAST_NAME));
    }

    public void initPojo(Cursor c,Spinner title){
        String titleText=c.getString(c.getColumnIndex(Constants.Professor.TITLE));
        for(int i=0; i<title.getCount();i++){
            if(title.getItemAtPosition(i).equals(titleText)){
                titleIndex=i;
                break;
            }
        }
        initPojo(c);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getTitleIndex() {
        return titleIndex;
    }

    public void setTitleIndex(int titleIndex) {
        this.titleIndex = titleIndex;
    }
}

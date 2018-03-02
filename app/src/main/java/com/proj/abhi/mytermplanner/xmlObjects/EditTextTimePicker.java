package com.proj.abhi.mytermplanner.xmlObjects;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TimePicker;

import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EditTextTimePicker implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    EditText _editText;
    private Calendar myCalendar;
    private Context _context;

    public EditTextTimePicker(Context context, int editTextViewID)
    {
        Activity act = (Activity)context;
        this._editText = (EditText)act.findViewById(editTextViewID);
        this._editText.setOnClickListener(this);
        this._context = context;
        myCalendar = Calendar.getInstance(TimeZone.getDefault());
    }

    public EditTextTimePicker(Context context, EditText editText)
    {
        Activity act = (Activity)context;
        this._editText = editText;
        this._editText.setOnClickListener(this);
        this._context = context;
        myCalendar = Calendar.getInstance();
    }

    @Override
    public void onClick(View v) {
        if(!Utils.hasValue(getText())){
            myCalendar=Calendar.getInstance(TimeZone.getDefault());
        }
        Utils.closeKeyboard();
        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int min = myCalendar.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(_context, this,hour, min, false);
        dialog.show();

    }

    public void setText(Date date){
        _editText.setText(DateUtils.getUserTime(date));
        if (date!=null)
            myCalendar.setTime(date);
        else
            myCalendar=Calendar.getInstance(TimeZone.getDefault());
    }

    public String getText(){
        return  _editText.getText().toString();
    }

    public void setVisibility(int id){
        _editText.setVisibility(id);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int min) {
        myCalendar.set(Calendar.HOUR_OF_DAY, hour);
        myCalendar.set(Calendar.MINUTE, min);

        setText(myCalendar.getTime());
    }

    public int getHour(){
        return myCalendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute(){
        return myCalendar.get(Calendar.MINUTE);
    }
}
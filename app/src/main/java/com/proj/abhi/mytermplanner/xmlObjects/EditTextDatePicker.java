package com.proj.abhi.mytermplanner.xmlObjects;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.Calendar;
import java.util.TimeZone;

public class EditTextDatePicker implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    EditText _editText;
    private Calendar myCalendar;
    private Context _context;

    public EditTextDatePicker(Context context, int editTextViewID)
    {
        Activity act = (Activity)context;
        this._editText = (EditText)act.findViewById(editTextViewID);
        this._editText.setOnClickListener(this);
        this._context = context;
        myCalendar = Calendar.getInstance(TimeZone.getDefault());
    }

    public EditTextDatePicker(Context context, EditText editText)
    {
        Activity act = (Activity)context;
        this._editText = editText;
        this._editText.setOnClickListener(this);
        this._context = context;
        myCalendar = Calendar.getInstance(TimeZone.getDefault());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        _editText.setText(Utils.userDateFormat.format(myCalendar.getTime()));
    }

    @Override
    public void onClick(View v) {
        Utils.closeKeyboard(_context);
        DatePickerDialog dialog = new DatePickerDialog(_context, this,
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();

    }

    public void setText(String text){
        _editText.setText(text);
        if (Utils.hasValue(text))
            myCalendar.setTime(Utils.getDateFromUser(text));
    }

    public String getText(){
        return  _editText.getText().toString();
    }

    public void setVisibility(int id){
        _editText.setVisibility(id);
    }
}
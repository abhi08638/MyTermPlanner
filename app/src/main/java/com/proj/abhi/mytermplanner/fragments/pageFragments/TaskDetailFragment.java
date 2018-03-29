package com.proj.abhi.mytermplanner.fragments.pageFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.generics.GenericDetailFragment;
import com.proj.abhi.mytermplanner.pojos.TaskPojo;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextDatePicker;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextTimePicker;

import java.util.Date;

public class TaskDetailFragment extends GenericDetailFragment {
    private EditText title, notes;
    private EditTextDatePicker startDate, endDate;
    private EditTextTimePicker startTime, endTime;
    private TaskPojo task = new TaskPojo();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.task_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initReminderFields();
        //init screen fields
        title = (EditText) getActivity().findViewById(R.id.title);
        notes = (EditText) getActivity().findViewById(R.id.notes);
        startDate = new EditTextDatePicker(getContext(), R.id.startDate);
        endDate = new EditTextDatePicker(getContext(), R.id.endDate);
        startTime = new EditTextTimePicker(getContext(), R.id.startTime);
        endTime = new EditTextTimePicker(getContext(), R.id.endTime);

        if(savedInstanceState==null) {
            refreshPage(getCurrentUriId());
        }else{
            task=(TaskPojo) task.initJson(savedInstanceState.getString(task.className));
            startTime.setText(task.getStartDate());
            endTime.setText(task.getEndDate());
            startDate.setText(DateUtils.getUserDate(task.getStartDate()));
            endDate.setText(DateUtils.getUserDate(task.getEndDate()));
        }
        pojo=task;
    }

    protected void initReminderFields() {
        //always create size plus 1 to allow for creation of custom date
        reminderFields = new String[2 + 1];
        reminderFieldIds = new int[reminderFields.length];
        reminderFields[0] = getString(R.string.start_date);
        reminderFieldIds[0] = R.id.startDate;
        reminderFields[1] = getString(R.string.end_date);
        reminderFieldIds[1] = R.id.endDate;
    }

    public Uri refreshPage(int i) {
        final int id = i;
        currentUri = Uri.parse(TasksProvider.CONTENT_URI + "/" + id);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (id > 0) {
                    final Cursor c = getActivity().getContentResolver().query(currentUri, null,
                            Constants.ID + "=" + getCurrentUriId(), null, null);
                    c.moveToFirst();
                    task.initPojo(c);
                    c.close();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(task.getTitle());
                            notes.setText(task.getNotes());
                            startDate.setText(DateUtils.getUserDate(task.getStartDate()));
                            endDate.setText(DateUtils.getUserDate(task.getEndDate()));
                            startTime.setText(task.getStartDate());
                            endTime.setText(task.getEndDate());
                            getActivity().setTitle(title.getText().toString());
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            emptyPage();
                            getActivity().setTitle(R.string.task_editor);
                        }
                    });
                }
            }
        });
        return currentUri;
    }

    protected void emptyPage() {
        currentUri = Uri.parse(TasksProvider.CONTENT_URI + "/" + 0);
        title.setText(null);
        startDate.setText(DateUtils.getUserDate(new Date()));
        startTime.setText(new Date());
        Date endTimeVal=new Date();
        endTimeVal.setHours(23);
        endTimeVal.setMinutes(59);
        endTime.setText(endTimeVal);
        endDate.setText(DateUtils.getUserDate(new Date()));
        notes.setText(null);
        task.reset();
    }

    private void mapObject(TaskPojo task){
        task.setTitle(title.getText().toString().trim());
        task.setNotes(notes.getText().toString().trim());
        task.setStartDate(DateUtils.getDateTimeFromUser(startDate.getText(), startTime.getText(), false));
        task.setEndDate(DateUtils.getDateTimeFromUser(endDate.getText(), endTime.getText(), true));
    }

    public Uri save() throws Exception {
        ContentValues values = new ContentValues();
        TaskPojo tempPojo = new TaskPojo();
        //all validations throw exceptions on failure to prevent saving
        try {
            mapObject(tempPojo);
            //title cant be empty
            if (Utils.hasValue(tempPojo.getTitle())) {
                values.put(Constants.Task.TASK_TITLE, tempPojo.getTitle());
            } else {
                throw new CustomException(getString(R.string.error_empty_title));
            }

            if (DateUtils.isBefore(tempPojo.getStartDate(), tempPojo.getEndDate())) {
                values.put(Constants.Task.TASK_START_DATE, DateUtils.getDbDate(tempPojo.getStartDate()));
                values.put(Constants.Task.TASK_END_DATE, DateUtils.getDbDate(tempPojo.getEndDate()));
            }

            //save notes
            values.put(Constants.Task.NOTES, tempPojo.getNotes());
        } catch (CustomException e) {
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if (getCurrentUriId() > 0) {
            getActivity().getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        } else {
            currentUri = getActivity().getContentResolver().insert(currentUri, values);
        }
        task=tempPojo;
        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
        return currentUri;
    }

    public void doReminder(Context context, Class clazz) {
        Bundle b = prepareReminder(context, clazz);
        b.putInt(Constants.Ids.TASK_ID, getCurrentUriId());
        b.putString(Constants.PersistAlarm.CONTENT_TITLE, task.getTitle());
        b.putString(Constants.PersistAlarm.CONTENT_TEXT, task.getNotes());
        b.putString(Constants.PersistAlarm.USER_OBJECT, Constants.Tables.TABLE_TASK);
        createReminder(b);
    }

    public void setIntentMsg() {
        intentMsg = ("Task Title: " + task.getTitle());
        intentMsg += ("\n");
        intentMsg += ("Start Date: " + DateUtils.getUserDateTime(task.getStartDate()));
        intentMsg += ("\n");
        intentMsg += ("End Date: " + DateUtils.getUserDateTime(task.getEndDate()));
        intentMsg += ("\n");
        intentMsg += ("Notes: " + task.getNotes());
        intentMsg += ("\n");
    }
}

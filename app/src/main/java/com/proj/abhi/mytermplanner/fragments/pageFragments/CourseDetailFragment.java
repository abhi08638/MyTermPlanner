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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericDetailFragment;
import com.proj.abhi.mytermplanner.pojos.CoursePojo;
import com.proj.abhi.mytermplanner.providers.CoursesProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextDatePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseDetailFragment extends GenericDetailFragment {
    private EditText title,notes;
    private EditTextDatePicker startDate, endDate;
    private Spinner status;
    private CoursePojo course = new CoursePojo();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.course_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initReminderFields();
        //init screen fields
        course.setTermId(initializer.getInt(Constants.Ids.TERM_ID));
        title = (EditText) getActivity().findViewById(R.id.title);
        notes = (EditText) getActivity().findViewById(R.id.notes);
        startDate = new EditTextDatePicker(getContext(), R.id.startDate);
        endDate = new EditTextDatePicker(getContext(), R.id.endDate);
        initSpinner();
        if(savedInstanceState==null) {
            refreshPage(getCurrentUriId());
        }else{
            course=(CoursePojo) course.initJson(savedInstanceState.getString(course.className));
            startDate.setText(DateUtils.getUserDate(course.getStartDate()));
            endDate.setText(DateUtils.getUserDate(course.getEndDate()));
        }
        pojo=course;
    }

    private void initSpinner(){
        status=(Spinner) getActivity().findViewById(R.id.status);
        List<String> list = new ArrayList<String>();
        list.add("Not Started");
        list.add("In Progress");
        list.add("Complete");
        list.add("Dropped");
        list.add("Failed");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(dataAdapter);
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
        currentUri = Uri.parse(CoursesProvider.CONTENT_URI + "/" + id);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (id > 0) {
                    final Cursor c = getActivity().getContentResolver().query(currentUri, course.getProjection(),
                            Constants.ID + "=" + getCurrentUriId(), null, null);
                    c.moveToFirst();
                    course.initPojo(c);
                    c.close();
                    final Bundle b = new Bundle();
                    b.putInt(Constants.Ids.TERM_ID,course.getTermId());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(course.getTitle());
                            notes.setText(course.getNotes());
                            startDate.setText(DateUtils.getUserDate(course.getStartDate()));
                            endDate.setText(DateUtils.getUserDate(course.getEndDate()));
                            status.setSelection(0);
                            for(int i=0; i<status.getCount();i++){
                                if(status.getItemAtPosition(i).equals(course.getStatus())){
                                    status.setSelection(i);
                                    break;
                                }
                            }
                            getActivity().setTitle(title.getText().toString());
                            ((GenericActivity)getActivity()).initActivityFromFragment(b);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            emptyPage();
                            getActivity().setTitle(R.string.course_editor);
                        }
                    });
                }
            }
        });
        return currentUri;
    }

    protected void emptyPage() {
        currentUri = Uri.parse(CoursesProvider.CONTENT_URI + "/" + 0);
        title.setText(null);
        startDate.setText(DateUtils.getUserDate(new Date()));
        endDate.setText(null);
        notes.setText(null);
        course.reset();
        status.setSelection(0);
    }

    private void mapObject(CoursePojo course){
        course.setTitle(title.getText().toString().trim());
        course.setNotes(notes.getText().toString().trim());
        course.setStatus(status.getSelectedItem().toString());
        course.setStartDate(DateUtils.getDateTimeFromUser(startDate.getText(), null, false));
        course.setEndDate(DateUtils.getDateTimeFromUser(endDate.getText(), null, true));
    }

    public Uri save() throws Exception {
        ContentValues values = new ContentValues();
        CoursePojo tempPojo = new CoursePojo();
        //all validations throw exceptions on failure to prevent saving
        try {
            mapObject(tempPojo);
            //title cant be empty
            if (Utils.hasValue(tempPojo.getTitle())) {
                values.put(Constants.Course.COURSE_TITLE, tempPojo.getTitle());
            } else {
                throw new CustomException(getString(R.string.error_empty_title));
            }

            if (DateUtils.isBefore(tempPojo.getStartDate(), tempPojo.getEndDate())) {
                values.put(Constants.Course.COURSE_START_DATE, DateUtils.getDbDate(tempPojo.getStartDate()));
                values.put(Constants.Course.COURSE_END_DATE, DateUtils.getDbDate(tempPojo.getEndDate()));
            }

            //save misc
            values.put(Constants.Course.STATUS, tempPojo.getStatus());
            values.put(Constants.Course.NOTES, tempPojo.getNotes());
        } catch (CustomException e) {
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if (getCurrentUriId() > 0) {
            getActivity().getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        } else {
            values.put(Constants.Ids.TERM_ID,course.getTermId());
            currentUri = getActivity().getContentResolver().insert(currentUri, values);
        }
        course=tempPojo;
        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
        return currentUri;
    }

    public void doReminder(Context context, Class clazz) {
        Bundle b = prepareReminder(context, clazz);
        b.putInt(Constants.Ids.COURSE_ID, getCurrentUriId());
        b.putString(Constants.PersistAlarm.CONTENT_TITLE, course.getTitle());
        b.putString(Constants.PersistAlarm.CONTENT_TEXT, course.getNotes());
        b.putString(Constants.PersistAlarm.USER_OBJECT, Constants.Tables.TABLE_COURSE);
        b.putInt(Constants.Ids.TERM_ID,course.getTermId());
        createReminder(b);
    }

    public void setIntentMsg() {
        intentMsg = ("Course Title: " + course.getTitle());
        intentMsg += ("\n");
        intentMsg += ("Start Date: " + DateUtils.getUserDateTime(course.getStartDate()));
        intentMsg += ("\n");
        intentMsg += ("End Date: " + DateUtils.getUserDateTime(course.getEndDate()));
        intentMsg += ("\n");
    }
}

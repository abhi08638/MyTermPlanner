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
import com.proj.abhi.mytermplanner.pojos.TermPojo;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextDatePicker;

public class TermDetailFragment extends GenericDetailFragment {
    private EditText title;
    private EditTextDatePicker startDate, endDate;
    private TermPojo term = new TermPojo();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.term_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initReminderFields();
        //init screen fields
        title = (EditText) getActivity().findViewById(R.id.title);
        startDate = new EditTextDatePicker(getContext(), R.id.startDate);
        endDate = new EditTextDatePicker(getContext(), R.id.endDate);

        if(savedInstanceState==null) {
            refreshPage(getCurrentUriId());
        }else{
            term=(TermPojo) term.initJson(savedInstanceState.getString(term.className));
            startDate.setText(DateUtils.getUserDate(term.getStartDate()));
            endDate.setText(DateUtils.getUserDate(term.getEndDate()));
        }
        pojo=term;
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
        currentUri = Uri.parse(TermsProvider.CONTENT_URI + "/" + id);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (id > 0) {
                    final Cursor c = getActivity().getContentResolver().query(currentUri, null,
                            Constants.ID + "=" + getCurrentUriId(), null, null);
                    c.moveToFirst();
                    term.initPojo(c);
                    c.close();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(term.getTitle());
                            startDate.setText(DateUtils.getUserDate(term.getStartDate()));
                            endDate.setText(DateUtils.getUserDate(term.getEndDate()));
                            getActivity().setTitle(title.getText().toString());
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            emptyPage();
                            getActivity().setTitle(R.string.term_editor);
                        }
                    });
                }
            }
        });
        return currentUri;
    }

    protected void emptyPage() {
        currentUri = Uri.parse(TermsProvider.CONTENT_URI + "/" + 0);
        title.setText(null);
        startDate.setText(null);
        endDate.setText(null);
        term.reset();
    }

    private void mapObject(TermPojo term){
        term.setTitle(title.getText().toString().trim());
        term.setStartDate(DateUtils.getDateTimeFromUser(startDate.getText(), null, false));
        term.setEndDate(DateUtils.getDateTimeFromUser(endDate.getText(), null, true));
    }

    public Uri save() throws Exception {
        ContentValues values = new ContentValues();
        TermPojo tempPojo = new TermPojo();
        //all validations throw exceptions on failure to prevent saving
        try {
            mapObject(tempPojo);
            //title cant be empty
            if (Utils.hasValue(tempPojo.getTitle())) {
                values.put(Constants.Term.TERM_TITLE, tempPojo.getTitle());
            } else {
                throw new CustomException(getString(R.string.error_empty_title));
            }

            if (DateUtils.isBefore(tempPojo.getStartDate(), tempPojo.getEndDate())) {
                values.put(Constants.Term.TERM_START_DATE, DateUtils.getDbDate(tempPojo.getStartDate()));
                values.put(Constants.Term.TERM_END_DATE, DateUtils.getDbDate(tempPojo.getEndDate()));
            }

        } catch (CustomException e) {
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if (getCurrentUriId() > 0) {
            getActivity().getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        } else {
            currentUri = getActivity().getContentResolver().insert(currentUri, values);
        }
        term=tempPojo;
        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
        return currentUri;
    }

    public void doReminder(Context context, Class clazz) {
        Bundle b = prepareReminder(context, clazz);
        b.putInt(Constants.Ids.TERM_ID, getCurrentUriId());
        b.putString(Constants.PersistAlarm.CONTENT_TITLE, term.getTitle());
        b.putString(Constants.PersistAlarm.USER_OBJECT, Constants.Tables.TABLE_TERM);
        createReminder(b);
    }

    public void setIntentMsg() {
        intentMsg = ("Term Title: " + term.getTitle());
        intentMsg += ("\n");
        intentMsg += ("Start Date: " + DateUtils.getUserDateTime(term.getStartDate()));
        intentMsg += ("\n");
        intentMsg += ("End Date: " + DateUtils.getUserDateTime(term.getEndDate()));
        intentMsg += ("\n");;
    }
}

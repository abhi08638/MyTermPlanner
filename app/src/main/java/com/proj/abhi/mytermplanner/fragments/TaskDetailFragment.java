package com.proj.abhi.mytermplanner.fragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.activities.GenericActivity;
import com.proj.abhi.mytermplanner.activities.TaskActivity;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.MaskWatcher;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TaskDetailFragment extends Fragment {
    private Bundle initializer=null;
    private Uri currentUri;
    private EditText title,endDate,startDate,notes;
    protected CoordinatorLayout mCoordinatorLayout;
    private String[] reminderFields;
    private int[] reminderFieldIds;
    private String intentMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        return inflater.inflate(R.layout.task_header_fragment,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);
        initializer=getArguments();
        if(initializer!=null) {
            currentUri=initializer.getParcelable(Constants.CURRENT_URI);
        }

        initReminderFields();
        //init screen fields
        title=(EditText) getActivity().findViewById(R.id.taskTitle);
        notes=(EditText) getActivity().findViewById(R.id.notes);
        startDate=(EditText) getActivity().findViewById(R.id.startDate);
        endDate=(EditText) getActivity().findViewById(R.id.endDate);
        startDate.addTextChangedListener(new MaskWatcher("##/##/####"));
        endDate.addTextChangedListener(new MaskWatcher("##/##/####"));
        refreshPage(getCurrentUriId());
    }

    private void initReminderFields(){
        //always create size plus 1 to allow for creation of custom date
        reminderFields=new String[2+1];
        reminderFieldIds=new int[reminderFields.length];
        reminderFields[0]=getString(R.string.start_date);
        reminderFieldIds[0]=R.id.startDate;
        reminderFields[1]=getString(R.string.end_date);
        reminderFieldIds[1]=R.id.endDate;
    }

    protected int getCurrentUriId(){
        try{
            return Integer.parseInt(currentUri.getLastPathSegment());
        }catch (Exception e){
            return 0;
        }
    }

    public Uri refreshPage(int i){
        final int id=i;
        currentUri = Uri.parse(TasksProvider.CONTENT_URI+"/"+id);
        AsyncTask.execute(new Runnable(){
            @Override
            public void run(){
                if(id>0){
                    final Cursor c = getActivity().getContentResolver().query(currentUri,null,
                            Constants.ID+"="+getCurrentUriId(),null,null);
                    c.moveToFirst();
                    getActivity().runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            title.setText(c.getString(c.getColumnIndex(Constants.Task.TASK_TITLE)));
                            notes.setText(c.getString(c.getColumnIndex(Constants.Task.NOTES)));
                            startDate.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Task.TASK_START_DATE))));
                            endDate.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Task.TASK_END_DATE))));
                            getActivity().setTitle(title.getText().toString());
                            c.close();
                        }
                    });
                }else{
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

    protected void emptyPage(){
        currentUri= Uri.parse(TasksProvider.CONTENT_URI+"/"+0);
        title.setText(null);
        startDate.setText(null);
        endDate.setText(null);
        notes.setText(null);
    }

    public Uri save() throws Exception{
        ContentValues values = new ContentValues();
        //all validations throw exceptions on failure to prevent saving
        try{
            //title cant be empty
            if(title.getText()!=null && !title.getText().toString().trim().equals("")){
                values.put(Constants.Task.TASK_TITLE,title.getText().toString());
            }else{throw new CustomException(getString(R.string.error_empty_title));}

            //start date must be valid
            if(Utils.isValidDate(startDate.getText().toString())) {
                values.put(Constants.Task.TASK_START_DATE, Utils.getDbDate(startDate.getText().toString()));
            }
            //end date must be valid
            if(Utils.isValidDate(endDate.getText().toString())) {
                values.put(Constants.Task.TASK_END_DATE, Utils.getDbDate(endDate.getText().toString()));
            }

            Utils.isBefore(startDate.getText().toString(),endDate.getText().toString());

            //save notes
            values.put(Constants.Assessment.NOTES,notes.getText().toString());
        }catch (CustomException e){
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if(getCurrentUriId()>0){
            getActivity().getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        }else{
            currentUri=getActivity().getContentResolver().insert(currentUri, values);
        }

        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
        return currentUri;
    }

    public void doReminder(Context context,Class clazz){
        refreshPage(getCurrentUriId());
        Intent intent = new Intent(context, clazz);
        intent.putExtra(Constants.CURRENT_URI, currentUri);
        Bundle b = new Bundle();
        b.putInt(Constants.Ids.TASK_ID,getCurrentUriId());
        b.putString(Constants.PersistAlarm.CONTENT_TITLE,title.getText().toString());
        b.putString(Constants.PersistAlarm.USER_OBJECT,Constants.Tables.TABLE_TASK);
        b.putParcelable(Constants.CURRENT_INTENT,intent);
        createReminder(reminderFields,reminderFieldIds,b);
    }

    public void setIntentMsg(){
        intentMsg=("Task Title: "+title.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Task Start Date: "+startDate.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Task End Date: "+endDate.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Task Notes: "+notes.getText().toString());
        intentMsg+=("\n");
    }

    public void doShare() {
        refreshPage(getCurrentUriId());
        setIntentMsg();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,intentMsg);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "From "+Constants.APP_NAME);

        startActivity(Intent.createChooser(shareIntent, "From "+Constants.APP_NAME));
    }

    public void createReminder(String[] list, int[] listIds, Bundle userBundle){
        try{
            final int[] ids = listIds;
            final Bundle b=userBundle;
            LayoutInflater li = LayoutInflater.from(getActivity());
            final View promptsView = li.inflate(R.layout.reminder_alert, null);
            final Spinner mSpinner= (Spinner) promptsView.findViewById(R.id.reminderDropdown);
            final TextView customDate = (TextView) promptsView.findViewById(R.id.reminderDate);
            final TextView reminderMsg = (TextView) promptsView.findViewById(R.id.reminderMsg);
            final TimePicker timePicker =promptsView.findViewById(R.id.timePicker);

            list[list.length-1]=getString(R.string.custom_date);
            listIds[listIds.length-1]=R.id.reminderDate;
            timePicker.setCurrentMinute(timePicker.getCurrentMinute()+1);

            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if(parentView.getItemAtPosition(position).toString().equals(getString(R.string.custom_date))){
                        customDate.setVisibility(View.VISIBLE);
                        customDate.addTextChangedListener(new MaskWatcher("##/##/####"));
                        customDate.setText(Utils.getCurrentDate());
                    }else{
                        customDate.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                try{
                                    TextView date = getActivity().findViewById(ids[mSpinner.getSelectedItemPosition()]);
                                    b.putString(Constants.PersistAlarm.CONTENT_TEXT,reminderMsg.getText().toString());
                                    if(date==null){
                                        date = promptsView.findViewById(ids[mSpinner.getSelectedItemPosition()]);
                                        if(!Utils.hasValue(reminderMsg.getText().toString())){
                                            b.putString(Constants.PersistAlarm.CONTENT_TEXT,getString(R.string.notification_date));
                                        }
                                    }else{
                                        if(!Utils.hasValue(reminderMsg.getText().toString())){
                                            b.putString(Constants.PersistAlarm.CONTENT_TEXT,mSpinner.getSelectedItem().toString());
                                        }
                                    }
                                    if(Utils.isValidDate(date.getText().toString())) {
                                        Date alarmDate = Utils.getDate(date.getText().toString());
                                        alarmDate.setHours(timePicker.getCurrentHour());
                                        alarmDate.setMinutes(timePicker.getCurrentMinute());
                                        ((GenericActivity)getActivity()).setAlarmForDate(alarmDate,b);
                                        for(android.support.v4.app.Fragment f:getActivity().getSupportFragmentManager().getFragments()){
                                            if(f instanceof AlarmListFragment){
                                                ((AlarmListFragment)f).restartLoader(null);
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    if(e instanceof CustomException){
                                        Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }else{
                                        Snackbar.make(mCoordinatorLayout, R.string.error_alarm_failed, Snackbar.LENGTH_LONG).show();
                                    }
                                    e.printStackTrace();
                                }
                            }
                        }
                    };

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(promptsView);
            alertDialogBuilder.setTitle(R.string.reminder_header)
                    .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(android.R.string.no), dialogClickListener);

            final ArrayAdapter<String> adp = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, list);
            adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            mSpinner.setAdapter(adp);
            mSpinner.setSelection(list.length-1);
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);
        }catch(Exception e){
            Snackbar.make(mCoordinatorLayout, R.string.error_alarm_failed, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}

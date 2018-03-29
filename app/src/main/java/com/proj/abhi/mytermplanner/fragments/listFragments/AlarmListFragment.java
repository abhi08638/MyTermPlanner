package com.proj.abhi.mytermplanner.fragments.listFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.cursorAdapters.AlarmsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeCoursesCursorAdapter;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.pojos.SpinnerPojo;
import com.proj.abhi.mytermplanner.providers.AlarmsProvider;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.PreferenceSingleton;
import com.proj.abhi.mytermplanner.utils.Utils;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextDatePicker;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextTimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

public class AlarmListFragment extends GenericListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializer.putString(Constants.CONTENT_URI, AlarmsProvider.CONTENT_URI.toString());
        initializer.putString(Constants.ID, Constants.ID);

        cursorAdapter = new AlarmsCursorAdapter(getActivity(), null, 0);
        setEmptyText("No " + getString(R.string.reminders));

        setListAdapter(cursorAdapter);
        initLoader();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if (bundle != null) {
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
            if (uri.equals(AlarmsProvider.CONTENT_URI)) {
                where = initializer.getString(Constants.Sql.WHERE);
            }
            return new CursorLoader(getActivity(), uri,
                    cols, where, null, order);
        }
        return null;
    }

    @Override
    public void onResume(){
        super.onResume();
        restartLoader();
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        if (id > 0) {
            try{
                Long l = new Long(id);
                Cursor c = ((AlarmsCursorAdapter)parent.getAdapter()).getCursor();
                c.moveToPosition(position);
                Bundle b = new Bundle();
                Intent sendingIntent = Intent.parseUri(c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_INTENT)),0);
                if(c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_URI))!=null){
                    Uri uri = Uri.parse(c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_URI)));
                    sendingIntent.putExtra(Constants.CURRENT_URI,uri);
                }
                b.putParcelable(Constants.CURRENT_INTENT, sendingIntent);
                b.putInt(Constants.Ids.ALARM_ID,c.getInt(c.getColumnIndex(Constants.ID)));
                b.putString(Constants.PersistAlarm.CONTENT_TITLE,c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_TITLE)));
                b.putString(Constants.PersistAlarm.CONTENT_TEXT,c.getString(c.getColumnIndex(Constants.PersistAlarm.CONTENT_TEXT)));
                b.putString(Constants.PersistAlarm.USER_OBJECT,c.getString(c.getColumnIndex(Constants.PersistAlarm.USER_OBJECT)));
                b.putInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE,c.getInt(c.getColumnIndex(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE)));
                b.putInt(Constants.Ids.TERM_ID, c.getInt(c.getColumnIndex(Constants.Ids.TERM_ID)));
                b.putInt(Constants.Ids.COURSE_ID, c.getInt(c.getColumnIndex(Constants.Ids.COURSE_ID)));
                b.putInt(Constants.Ids.ASSESSMENT_ID, c.getInt(c.getColumnIndex(Constants.Ids.ASSESSMENT_ID)));
                b.putInt(Constants.Ids.TASK_ID, c.getInt(c.getColumnIndex(Constants.Ids.TASK_ID)));
                b.putInt(Constants.Ids.ALARM_ID, l.intValue());
                b.putString(Constants.PersistAlarm.NOTIFY_DATETIME,c.getString(c.getColumnIndex(Constants.PersistAlarm.NOTIFY_DATETIME)));
                createReminder(b);
            }catch (Exception e){

            }
        }
    }

    private Calendar getCalendar(String dateTime){
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = iso8601Format.parse(dateTime);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            return c;
        } catch (Exception e) {
            return Calendar.getInstance();
        }
    }

    public void createReminder(Bundle userBundle) {
        try {
            final Bundle b = userBundle;
            LayoutInflater li = LayoutInflater.from(getActivity());
            final View promptsView = li.inflate(R.layout.reminder_alert, null);
            final TextInputLayout lbl = promptsView.findViewById(R.id.reminderDateLbl);
            final Spinner mSpinner = (Spinner) promptsView.findViewById(R.id.reminderDropdown);
            final Spinner mSpinnerType = (Spinner) promptsView.findViewById(R.id.reminderType);
            final EditTextDatePicker customDate = new EditTextDatePicker(getActivity(), (EditText) promptsView.findViewById(R.id.reminderDate));
            final TextView reminderMsg = (TextView) promptsView.findViewById(R.id.reminderMsg);
            final EditTextTimePicker timePicker = new EditTextTimePicker(getActivity(), (EditText) promptsView.findViewById(R.id.reminderTime));
            Date savedDate=getCalendar(b.getString(Constants.PersistAlarm.NOTIFY_DATETIME)).getTime();
            timePicker.setText(savedDate);
            reminderMsg.setText(b.getString(Constants.PersistAlarm.CONTENT_TEXT));
            mSpinner.setVisibility(View.GONE);
            lbl.setVisibility(View.VISIBLE);
            customDate.setVisibility(View.VISIBLE);
            customDate.setText(DateUtils.getUserDate(savedDate));

            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                try {
                                    TextView date = promptsView.findViewById(R.id.reminderDate);
                                    if (Utils.hasValue(reminderMsg.getText().toString())) {
                                        b.putString(Constants.PersistAlarm.CONTENT_TEXT, reminderMsg.getText().toString());
                                    }
                                    Date alarmDate = DateUtils.userDateFormat.parse(date.getText().toString());
                                    alarmDate.setHours(timePicker.getHour());
                                    alarmDate.setMinutes(timePicker.getMinute());
                                    b.putInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE, mSpinnerType.getSelectedItemPosition());
                                    setAlarmForDate(alarmDate, b);
                                    restartLoader();
                                } catch (Exception e) {
                                    if (e instanceof CustomException) {
                                        Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    } else {
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

            final ArrayList<SpinnerPojo> typeList = new ArrayList();
            typeList.add(new SpinnerPojo(Constants.NotifyTypes.NORMAL, getString(R.string.normal)));
            typeList.add(new SpinnerPojo(Constants.NotifyTypes.ALARM, getString(R.string.alarm)));
            final ArrayAdapter<SpinnerPojo> typeAdp = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, typeList);
            typeAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            mSpinnerType.setAdapter(typeAdp);
            mSpinnerType.setSelection(b.getInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE));
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);
        } catch (Exception e) {
            Snackbar.make(mCoordinatorLayout, R.string.error_alarm_failed, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void setAlarmForDate(Date date, Bundle userBundle) {
        if (date.before(new Date())) {
            Snackbar.make(mCoordinatorLayout, R.string.error_alarm_before, Snackbar.LENGTH_LONG).show();
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            String cancelAlarmsWhere = " WHERE " + Constants.ID + "=" + userBundle.getInt(Constants.Ids.ALARM_ID);
            new AlarmTask(getActivity(), null, null).cancelAlarms(cancelAlarmsWhere);
            ((GenericActivity) getActivity()).setAlarmForNotification(date, userBundle);
            Snackbar.make(mCoordinatorLayout, "Notification set for " + DateUtils.getUserDateTime(date), Snackbar.LENGTH_LONG).show();
        }
    }
}

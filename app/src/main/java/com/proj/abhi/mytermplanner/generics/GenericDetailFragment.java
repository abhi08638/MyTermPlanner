package com.proj.abhi.mytermplanner.generics;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.listFragments.AlarmListFragment;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.Utils;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextDatePicker;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextTimePicker;

import java.util.Calendar;
import java.util.Date;

public abstract class GenericDetailFragment extends Fragment {
    protected Bundle initializer = null;
    protected Uri currentUri;
    protected CoordinatorLayout mCoordinatorLayout;
    protected String[] reminderFields;
    protected int[] reminderFieldIds;
    protected String intentMsg;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);
        initializer = getArguments();
        if (initializer != null) {
            currentUri = initializer.getParcelable(Constants.CURRENT_URI);
        }
    }

    protected abstract void initReminderFields();

    protected int getCurrentUriId() {
        try {
            return Integer.parseInt(currentUri.getLastPathSegment());
        } catch (Exception e) {
            return 0;
        }
    }

    public abstract Uri refreshPage(int i);

    protected abstract void emptyPage();

    public abstract Uri save() throws Exception;

    public abstract void doReminder(Context context, Class clazz);

    protected Bundle prepareReminder(Context context, Class clazz) {
        refreshPage(getCurrentUriId());
        Intent intent = new Intent(context, clazz);
        intent.putExtra(Constants.CURRENT_URI, currentUri);
        Bundle b = new Bundle();
        b.putParcelable(Constants.CURRENT_INTENT, intent);
        return b;
    }

    public abstract void setIntentMsg();

    public void doShare() {
        refreshPage(getCurrentUriId());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                setIntentMsg();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, intentMsg);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "From " + Constants.APP_NAME);
                startActivity(Intent.createChooser(shareIntent, "From " + Constants.APP_NAME));
            }
        });
    }

    public void setAlarmForDate(Date date, Bundle userBundle) {
        if (date.before(new Date())) {
            Snackbar.make(mCoordinatorLayout, R.string.error_alarm_before, Snackbar.LENGTH_LONG).show();
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            ((GenericActivity) getActivity()).setAlarmForNotification(date, userBundle);
            Snackbar.make(mCoordinatorLayout, "Notification set for " + DateUtils.getUserDateTime(date), Snackbar.LENGTH_LONG).show();
        }
    }

    public void createReminder(Bundle userBundle) {
        try {
            final int[] ids = reminderFieldIds;
            final String[] fields = reminderFields;
            final Bundle b = userBundle;
            LayoutInflater li = LayoutInflater.from(getActivity());
            final View promptsView = li.inflate(R.layout.reminder_alert, null);
            final TextInputLayout lbl = promptsView.findViewById(R.id.reminderDateLbl);
            final Spinner mSpinner = (Spinner) promptsView.findViewById(R.id.reminderDropdown);
            final EditTextDatePicker customDate = new EditTextDatePicker(getActivity(), (EditText) promptsView.findViewById(R.id.reminderDate));
            final TextView reminderMsg = (TextView) promptsView.findViewById(R.id.reminderMsg);
            final EditTextTimePicker timePicker = new EditTextTimePicker(getActivity(), (EditText) promptsView.findViewById(R.id.reminderTime));
            fields[fields.length - 1] = getString(R.string.custom_date);
            ids[ids.length - 1] = R.id.reminderDate;
            Date now = new Date();
            now.setMinutes(now.getMinutes() + 1);
            timePicker.setText(now);
            reminderMsg.setText(b.getString(Constants.PersistAlarm.CONTENT_TEXT));
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (parentView.getItemAtPosition(position).toString().equals(getString(R.string.custom_date))) {
                        lbl.setVisibility(View.VISIBLE);
                        customDate.setVisibility(View.VISIBLE);
                        customDate.setText(DateUtils.getCurrentDate());
                    } else {
                        lbl.setVisibility(View.GONE);
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
                                try {
                                    TextView date = getActivity().findViewById(ids[mSpinner.getSelectedItemPosition()]);
                                    b.putString(Constants.PersistAlarm.CONTENT_TEXT, reminderMsg.getText().toString());
                                    if (date == null) {
                                        date = promptsView.findViewById(ids[mSpinner.getSelectedItemPosition()]);
                                        if (!Utils.hasValue(reminderMsg.getText().toString())) {
                                            b.putString(Constants.PersistAlarm.CONTENT_TEXT, getString(R.string.notification_date));
                                        }
                                    } else {
                                        if (!Utils.hasValue(reminderMsg.getText().toString())) {
                                            b.putString(Constants.PersistAlarm.CONTENT_TEXT, mSpinner.getSelectedItem().toString());
                                        }
                                    }
                                    if (Utils.isValidDate(date.getText().toString())) {
                                        Date alarmDate = Utils.getDateFromUser(date.getText().toString());
                                        alarmDate.setHours(timePicker.getHour());
                                        alarmDate.setMinutes(timePicker.getMinute());
                                        setAlarmForDate(alarmDate, b);
                                        for (Fragment f : getActivity().getSupportFragmentManager().getFragments()) {
                                            if (f instanceof AlarmListFragment) {
                                                ((AlarmListFragment) f).restartLoader();
                                            }
                                        }
                                    }
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

            final ArrayAdapter<String> adp = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, fields);
            adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            mSpinner.setAdapter(adp);
            mSpinner.setSelection(fields.length - 1);
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);
        } catch (Exception e) {
            Snackbar.make(mCoordinatorLayout, R.string.error_alarm_failed, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}

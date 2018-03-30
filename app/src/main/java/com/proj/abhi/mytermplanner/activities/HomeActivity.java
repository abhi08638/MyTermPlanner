package com.proj.abhi.mytermplanner.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.listFragments.AlarmListFragment;
import com.proj.abhi.mytermplanner.fragments.listFragments.HomeListFragments;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.pojos.SpinnerPojo;
import com.proj.abhi.mytermplanner.providers.ContactsProvider;
import com.proj.abhi.mytermplanner.providers.CoursesProvider;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.PreferenceSingleton;
import com.proj.abhi.mytermplanner.utils.Utils;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextDatePicker;
import com.proj.abhi.mytermplanner.xmlObjects.EditTextTimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends GenericActivity implements DatePickerDialog.OnDateSetListener{
    private int numQueryDays = 7;
    private Calendar calendar;

    @Override
    protected Class getChildClass() {
        return HomeActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init user prefs
        numQueryDays=PreferenceSingleton.getNumQueryDays();
        defaultTabIndex=PreferenceSingleton.getHomeDefTabIndex();
        calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,numQueryDays);
        setTitle();

        //init tabs
        initViewPager();

        addItemsInNavMenuDrawer();
    }

    @Override
    protected void save() throws Exception {
    }

    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            Bundle b = new Bundle();
            if (PreferenceSingleton.isSchoolMode()) {
                b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
                b.putString(Constants.CONTENT_URI, TermsProvider.CONTENT_URI.toString());
                b.putString(Constants.ID, Constants.Ids.TERM_ID);
                b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.TERM_ID);
                HomeListFragments termFragment = new HomeListFragments();
                termFragment.setArguments(b);

                b = new Bundle();
                b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
                b.putString(Constants.CONTENT_URI, CoursesProvider.CONTENT_URI.toString());
                b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.HOME_COURSE_ID);
                b.putString(Constants.ID, Constants.Ids.COURSE_ID);
                HomeListFragments courseFragment = new HomeListFragments();
                courseFragment.setArguments(b);

                b = new Bundle();
                b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
                b.putString(Constants.CONTENT_URI, HomeAssessmentsProvider.CONTENT_URI.toString());
                b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.HOME_ASSESSMENT_ID);
                b.putString(Constants.ID, Constants.Ids.ASSESSMENT_ID);
                HomeListFragments assessmentFragment = new HomeListFragments();
                assessmentFragment.setArguments(b);

                adapter.addFragment(termFragment, getString(R.string.terms));
                adapter.addFragment(courseFragment, getString(R.string.courses));
                adapter.addFragment(assessmentFragment, getString(R.string.assessments));
            }

            b = new Bundle();
            b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
            b.putString(Constants.CONTENT_URI, TasksProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.TASK_ID);
            b.putString(Constants.ID, Constants.Ids.TASK_ID);
            HomeListFragments taskFragment = new HomeListFragments();
            taskFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.Sql.WHERE, Constants.SqlSelect.QUERY_ALARMS + "ORDER BY " + Constants.PersistAlarm.NOTIFY_DATETIME);
            AlarmListFragment reminderFragment = new AlarmListFragment();
            reminderFragment.setArguments(b);

            adapter.addFragment(taskFragment, getString(R.string.tasks));
            adapter.addFragment(reminderFragment, getString(R.string.reminders));
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(adapter.getCount());
            initTabs(viewPager);
            selectDefaultTab();
        }
    }

    protected void setTitle() {
        if (numQueryDays > 0) {
            this.setTitle(getString(R.string.title_prefix)+" "+DateUtils.getUserDate(calendar.getTime()));
        }else if (numQueryDays < 0) {
            this.setTitle(DateUtils.getUserDate(calendar.getTime())+" "+getString(R.string.title_suffix));
        } else {
            this.setTitle(getString(R.string.todays_events));
        }
    }

    protected void addItemsInNavMenuDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        //get rid of share item
        menu.getItem(0).getSubMenu().getItem(0).setVisible(false);
        menu.getItem(0).getSubMenu().getItem(3).setVisible(false);

        //add others
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.MANAGEMENT_GROUP, Constants.MenuGroups.MANAGEMENT_GROUP, 0, R.string.manage);
        submenu.setGroupCheckable(Constants.MenuGroups.MANAGEMENT_GROUP, false, true);
        submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP, Constants.MenuGroups.TASK_GROUP, 0, R.string.tasks);
        if (PreferenceSingleton.isSchoolMode()) {
            submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP, Constants.MenuGroups.TERM_GROUP, 0, R.string.terms);
        }
        submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP, Constants.MenuGroups.PROF_GROUP, 0, R.string.profs);
        navView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.add(0, Constants.ActionBarIds.ADD_TASK, 0, getString(R.string.create_task));
        if (PreferenceSingleton.isSchoolMode()) {
            menu.add(0, Constants.ActionBarIds.ADD_TERM, 0, getString(R.string.create_term));
        }
        menu.add(0, Constants.ActionBarIds.ADD_PROF, 0, getString(R.string.create_prof));
        menu.add(0, Constants.ActionBarIds.ADD_REMINDER, 0, R.string.quick_reminder);

        MenuItem calendar = menu.findItem(R.id.home_calendar);
        calendar.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == Constants.ActionBarIds.ADD_TERM) {
            Utils.sendToActivity(0, TermActivity.class, TermsProvider.CONTENT_URI);
            return true;
        } else if (id == Constants.ActionBarIds.ADD_PROF) {
            Utils.sendToActivity(0, ContactActivity.class, ContactsProvider.CONTENT_URI);
            return true;
        } else if (id == Constants.ActionBarIds.ADD_TASK) {
            Utils.sendToActivity(0, TaskActivity.class, TasksProvider.CONTENT_URI);
            return true;
        } else if (id == Constants.ActionBarIds.ADD_REMINDER) {
            createReminder(prepareReminder());
        } else if (id == R.id.home_calendar){
            DatePickerDialog dialog = new DatePickerDialog(this, this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public Bundle prepareReminder() {
        Intent intent = new Intent(this, this.getClass());
        intent.putExtra(Constants.CURRENT_URI, currentUri);
        Bundle b = new Bundle();
        b.putParcelable(Constants.CURRENT_INTENT, intent);
        b.putInt(Constants.Ids.TASK_ID, getCurrentUriId());
        b.putString(Constants.PersistAlarm.USER_OBJECT,getString(R.string.quick_reminder));
        return b;
    }

    public void createReminder(Bundle userBundle) {
        try {
            final Bundle b = userBundle;
            LayoutInflater li = LayoutInflater.from(this);
            final View promptsView = li.inflate(R.layout.reminder_alert, null);
            final TextInputLayout lbl = promptsView.findViewById(R.id.reminderDateLbl);
            final Spinner mSpinner = (Spinner) promptsView.findViewById(R.id.reminderDropdown);
            final Spinner mSpinnerType = (Spinner) promptsView.findViewById(R.id.reminderType);
            final EditTextDatePicker customDate = new EditTextDatePicker(this, (EditText) promptsView.findViewById(R.id.reminderDate));
            final TextView reminderMsg = (TextView) promptsView.findViewById(R.id.reminderMsg);
            final EditTextTimePicker timePicker = new EditTextTimePicker(this, (EditText) promptsView.findViewById(R.id.reminderTime));
            mSpinner.setVisibility(View.GONE);
            Date now = new Date();
            now.setMinutes(now.getMinutes() + 1);
            timePicker.setText(now);
            lbl.setVisibility(View.VISIBLE);
            customDate.setVisibility(View.VISIBLE);
            customDate.setText(DateUtils.getCurrentDate());

            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                try {
                                    if (!Utils.hasValue(reminderMsg.getText().toString())) {
                                        throw new CustomException(getString(R.string.msg_required));
                                    }
                                    b.putString(Constants.PersistAlarm.CONTENT_TITLE, reminderMsg.getText().toString());
                                    b.putString(Constants.PersistAlarm.CONTENT_TEXT, reminderMsg.getText().toString());
                                    Date alarmDate = DateUtils.userDateFormat.parse(customDate.getText());
                                    alarmDate.setHours(timePicker.getHour());
                                    alarmDate.setMinutes(timePicker.getMinute());
                                    b.putInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE, mSpinnerType.getSelectedItemPosition());
                                    setAlarmForDate(alarmDate, b);
                                    AlarmListFragment alarmListFragment = (AlarmListFragment) getFragmentByTitle(R.string.reminders);
                                    alarmListFragment.restartLoader();
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

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(promptsView);
            alertDialogBuilder.setTitle(R.string.reminder_header)
                    .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(android.R.string.no), dialogClickListener);

            final ArrayList<SpinnerPojo> typeList = new ArrayList();
            typeList.add(new SpinnerPojo(Constants.NotifyTypes.NORMAL, getString(R.string.normal)));
            typeList.add(new SpinnerPojo(Constants.NotifyTypes.ALARM, getString(R.string.alarm)));
            final ArrayAdapter<SpinnerPojo> typeAdp = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, typeList);
            typeAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            mSpinnerType.setAdapter(typeAdp);
            mSpinnerType.setSelection(PreferenceSingleton.getDefaultNotifyType());
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
            setAlarmForNotification(date, userBundle);
            Snackbar.make(mCoordinatorLayout, "Notification set for " + DateUtils.getUserDateTime(date), Snackbar.LENGTH_LONG).show();
        }
    }

    protected void refreshPage(int id) {
        setTitle();
        Bundle b = new Bundle();
        b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
        for (android.support.v4.app.Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof HomeListFragments) {
                ((HomeListFragments) f).restartLoader(b);
            } else if (f instanceof AlarmListFragment) {
                ((AlarmListFragment) f).restartLoader();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        super.onNavigationItemSelected(item);
        // Handle navigation view item clicks here.
        final int id = item.getItemId();
        final int groupId = item.getGroupId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (groupId == Constants.MenuGroups.MANAGEMENT_GROUP) {
                            if (id == Constants.MenuGroups.TERM_GROUP) {
                                Utils.sendToActivity(0, TermActivity.class, TermsProvider.CONTENT_URI);
                            } else if (id == Constants.MenuGroups.PROF_GROUP) {
                                Utils.sendToActivity(0, ContactActivity.class, ContactsProvider.CONTENT_URI);
                            } else if (id == Constants.MenuGroups.TASK_GROUP) {
                                Utils.sendToActivity(0, TaskActivity.class, TasksProvider.CONTENT_URI);
                            }
                        }
                    }
                },
                200
        );

        return true;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        Long minutes =TimeUnit.MINUTES.convert(calendar.getTime().getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
        numQueryDays=(int) Math.ceil(minutes/60.0/24.0);
        refreshPage(0);
    }
}

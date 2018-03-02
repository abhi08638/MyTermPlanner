package com.proj.abhi.mytermplanner.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.listFragments.AlarmListFragment;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.pageAdapters.CustomPageAdapter;
import com.proj.abhi.mytermplanner.fragments.listFragments.HomeListFragment;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.ProfProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends GenericActivity{
    private int numQueryDays = 7;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init user prefs
        initPreferences();

        //init tabs
        initViewPager();

        //hide fab from generic view
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addItemsInNavMenuDrawer();

        //restore values after rotation
        handleRotation(savedInstanceState);

    }

    @Override
    protected void save() throws Exception {}

    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            CustomPageAdapter adapter = new CustomPageAdapter(getSupportFragmentManager());
            Bundle b = new Bundle();
            b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
            b.putString(Constants.CONTENT_URI, TermsProvider.CONTENT_URI.toString());
            b.putString(Constants.ID, Constants.Ids.TERM_ID);
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.TERM_ID);
            HomeListFragment termFragment = new HomeListFragment();
            termFragment.setArguments(b);

            b = new Bundle();
            b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
            b.putString(Constants.CONTENT_URI, HomeCoursesProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.HOME_COURSE_ID);
            b.putString(Constants.ID, Constants.Ids.COURSE_ID);
            HomeListFragment courseFragment = new HomeListFragment();
            courseFragment.setArguments(b);

            b = new Bundle();
            b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
            b.putString(Constants.CONTENT_URI, HomeAssessmentsProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.HOME_ASSESSMENT_ID);
            b.putString(Constants.ID, Constants.Ids.ASSESSMENT_ID);
            HomeListFragment assessmentFragment = new HomeListFragment();
            assessmentFragment.setArguments(b);

            b = new Bundle();
            b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
            b.putString(Constants.CONTENT_URI, TasksProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.TASK_ID);
            b.putString(Constants.ID, Constants.Ids.TASK_ID);
            HomeListFragment taskFragment = new HomeListFragment();
            taskFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.Sql.WHERE, Constants.SqlSelect.QUERY_ALARMS + "ORDER BY " + Constants.PersistAlarm.NOTIFY_DATETIME);
            AlarmListFragment reminderFragment = new AlarmListFragment();
            reminderFragment.setArguments(b);

            adapter.addFragment(termFragment, getString(R.string.terms));
            adapter.addFragment(courseFragment, getString(R.string.courses));
            adapter.addFragment(assessmentFragment, getString(R.string.assessments));
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
            this.setTitle(getString(R.string.active_future_events) + ": " + numQueryDays + " Day(s)");
        } else if (numQueryDays < 0) {
            this.setTitle(getString(R.string.past_events) + ": " + numQueryDays + " Day(s)");
        } else {
            this.setTitle(getString(R.string.todays_events));
        }
    }

    private void initPreferences() {
        //init query params
        sharedpreferences = getSharedPreferences(Constants.SharedPreferenceKeys.USER_PREFS, Context.MODE_PRIVATE);
        if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, Integer.toString(numQueryDays));
            editor.apply();
        } else {
            numQueryDays = Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, null));
        }
        setTitle();

        //init default tab
        if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.DEFAULT_TAB)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.DEFAULT_TAB, Integer.toString(defaultTabIndex));
            editor.apply();
        } else {
            setDefaultTabIndex(Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.DEFAULT_TAB, null)));
        }

    }

    protected void addItemsInNavMenuDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        //get rid of share item
        menu.getItem(0).getSubMenu().getItem(0).setVisible(false);

        //add others
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.MANAGEMENT_GROUP, Constants.MenuGroups.MANAGEMENT_GROUP, 0, R.string.manage);
        submenu.setGroupCheckable(Constants.MenuGroups.MANAGEMENT_GROUP, false, true);
        submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP, Constants.MenuGroups.TASK_GROUP, 0, R.string.tasks);
        submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP, Constants.MenuGroups.TERM_GROUP, 0, R.string.terms);
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
        menu.add(0, Constants.ActionBarIds.ADD_TERM, 0, getString(R.string.create_term));
        menu.add(0, Constants.ActionBarIds.ADD_PROF, 0, getString(R.string.create_prof));
        menu.add(0, Constants.ActionBarIds.USER_PREFS, 0, getString(R.string.user_prefs));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == Constants.ActionBarIds.ADD_TERM) {
            Utils.sendToActivity(0, TermActivity.class, TermsProvider.CONTENT_URI);
            return true;
        } else if (id == Constants.ActionBarIds.ADD_PROF) {
            Utils.sendToActivity(0, ProfessorActivity.class, ProfProvider.CONTENT_URI);
            return true;
        } else if (id == Constants.ActionBarIds.ADD_TASK) {
            Utils.sendToActivity(0, TaskActivity.class, TasksProvider.CONTENT_URI);
            return true;
        } else if (id == Constants.ActionBarIds.USER_PREFS) {
            String[] tabList = {getString(R.string.terms), getString(R.string.courses), getString(R.string.assessments), getString(R.string.tasks), getString(R.string.reminders)};
            LayoutInflater li = LayoutInflater.from(this);
            final View prefsView = li.inflate(R.layout.prefs_dialog, null);
            final TextView daysInput = prefsView.findViewById(R.id.numDays);
            daysInput.setText(Integer.toString(numQueryDays));

            final Spinner mSpinner = (Spinner) prefsView.findViewById(R.id.tabDropDown);
            final ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, tabList);
            adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adp);
            mSpinner.setSelection(defaultTabIndex);

            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                try {
                                    int numDays = Integer.parseInt(daysInput.getText().toString());
                                    if (numDays > 365 || numDays < -365) {
                                        throw new CustomException("Invalid number of days");
                                    } else {
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, Integer.toString(numDays));
                                        editor.putString(Constants.SharedPreferenceKeys.DEFAULT_TAB, Integer.toString(mSpinner.getSelectedItemPosition()));
                                        editor.apply();
                                        numQueryDays = numDays;
                                        setDefaultTabIndex(mSpinner.getSelectedItemPosition());
                                        refreshPage(0);
                                    }
                                } catch (Exception e) {
                                    if (e instanceof CustomException) {
                                        Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    } else {
                                        Snackbar.make(mCoordinatorLayout, "Failed to save preferences", Snackbar.LENGTH_LONG).show();
                                    }
                                    e.printStackTrace();
                                }
                            }
                        }
                    };

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(prefsView);
            alertDialogBuilder.setTitle("User Preferences")
                    .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(android.R.string.no), dialogClickListener);

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);

        }

        return super.onOptionsItemSelected(item);
    }

    protected void refreshPage(int id) {
        setTitle();
        Bundle b = new Bundle();
        b.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numQueryDays);
        for (android.support.v4.app.Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof HomeListFragment) {
                ((HomeListFragment) f).restartLoader(b);
            } else if (f instanceof AlarmListFragment) {
                ((AlarmListFragment) f).restartLoader();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
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
                                Utils.sendToActivity(0, ProfessorActivity.class, ProfProvider.CONTENT_URI);
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
}

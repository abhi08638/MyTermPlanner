package com.proj.abhi.mytermplanner.activities;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeAssessmentsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeCoursesCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTasksCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeTermsCursorAdapter;
import com.proj.abhi.mytermplanner.providers.AssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.CoursesProvider;
import com.proj.abhi.mytermplanner.providers.HomeAssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.HomeCoursesProvider;
import com.proj.abhi.mytermplanner.providers.ProfProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.Utils;

public class HomeActivitybackup extends GenericActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{

    private CursorAdapter termCursorAdapter = new HomeTermsCursorAdapter(this,null,0);
    private CursorAdapter courseCursorAdapter = new HomeCoursesCursorAdapter(this,null,0);
    private CursorAdapter assessmentCursorAdapter = new HomeAssessmentsCursorAdapter(this,null,0);
    private CursorAdapter taskCursorAdapter = new HomeTasksCursorAdapter(this,null,0);
    private ListView termList,courseList,assessmentList,taskList;
    private int numQueryDays=7;
    private SharedPreferences sharedpreferences;
    private String sortOrder="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayout(R.layout.home_header);
        initTabs();

        //init user prefs
        initPreferences();

        //init cursor loaders
        Bundle b = new Bundle();
        b.putString("contentUri", TermsProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.TERM_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", HomeCoursesProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.HOME_COURSE_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", HomeAssessmentsProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.HOME_ASSESSMENT_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", TasksProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.TASK_ID,b,this);


        //hide fab from generic view
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        //init screen fields
        termList = (ListView) findViewById(R.id.termList);
        termList.setAdapter(termCursorAdapter);
        termList.setClickable(true);
        courseList = (ListView) findViewById(R.id.courseList);
        courseList.setAdapter(courseCursorAdapter);
        courseList.setClickable(true);
        assessmentList = (ListView) findViewById(R.id.assessmentList);
        assessmentList.setAdapter(assessmentCursorAdapter);
        assessmentList.setClickable(true);
        taskList = (ListView) findViewById(R.id.taskList);
        taskList.setAdapter(taskCursorAdapter);
        taskList.setClickable(true);

        termList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(id>0){
                    Long l = new Long(id);
                    sendToTerm(l.intValue());
                }
            }
        });

        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(id>0){
                    Intent intent = new Intent(HomeActivitybackup.this, CourseActivity.class);
                    Uri uri = Uri.parse(CoursesProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(Constants.CURRENT_URI, uri);
                    startActivityForResult(intent,0);
                }
            }
        });

        assessmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(id>0){
                    Intent intent = new Intent(HomeActivitybackup.this, AssessmentActivity.class);
                    Uri uri = Uri.parse(AssessmentsProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(Constants.CURRENT_URI, uri);
                    startActivityForResult(intent,0);
                }
            }
        });

        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(id>0){
                    Long l = new Long(id);
                    sendToTask(l.intValue());
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addItemsInNavMenuDrawer();

        //restore values after rotation
        handleRotation(savedInstanceState,false);

    }

    protected void setTitle(){
        if(numQueryDays>0){
            this.setTitle(getString(R.string.active_future_events)+": "+numQueryDays+" Day(s)");
        }else if(numQueryDays<0){
            this.setTitle(getString(R.string.past_events)+": "+numQueryDays+" Day(s)");
        }else{
            this.setTitle(getString(R.string.todays_events));
        }
    }

    private void initPreferences(){
        //init query params
        sharedpreferences = getSharedPreferences(Constants.SharedPreferenceKeys.USER_PREFS, Context.MODE_PRIVATE);
        if(!sharedpreferences.contains(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS)){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, Integer.toString(numQueryDays));
            editor.apply();
        }else{
            numQueryDays=Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS,null));
        }
        setTitle();

        //init default tab
        if(!sharedpreferences.contains(Constants.SharedPreferenceKeys.DEFAULT_TAB)){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.DEFAULT_TAB, Integer.toString(defaultTabIndex));
            editor.apply();
        }else{
            setDefaultTabIndex(Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.DEFAULT_TAB,null)));
            selectDefaultTab();
        }

    }

    protected void addItemsInNavMenuDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        //get rid of share item
        menu.getItem(0).getSubMenu().getItem(0).setVisible(false);

        //add others
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.MANAGEMENT_GROUP,Constants.MenuGroups.MANAGEMENT_GROUP,0, R.string.manage);
        submenu.setGroupCheckable(Constants.MenuGroups.MANAGEMENT_GROUP,false,true);
        submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP,Constants.MenuGroups.TASK_GROUP,0,R.string.tasks);
        submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP,Constants.MenuGroups.TERM_GROUP,0,R.string.terms);
        submenu.add(Constants.MenuGroups.MANAGEMENT_GROUP,Constants.MenuGroups.PROF_GROUP,0,R.string.profs);
        navView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.add(0,Constants.ActionBarIds.ADD_TASK,0,getString(R.string.create_task));
        menu.add(0,Constants.ActionBarIds.ADD_TERM,0,getString(R.string.create_term));
        menu.add(0,Constants.ActionBarIds.ADD_PROF,0,getString(R.string.create_prof));
        menu.add(0,Constants.ActionBarIds.USER_PREFS,0,getString(R.string.user_prefs));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == Constants.ActionBarIds.ADD_TERM ) {
            sendToTerm(0);
            return true;
        }else if (id == Constants.ActionBarIds.ADD_PROF ) {
            sendToProf(0);
            return true;
        }else if (id == Constants.ActionBarIds.ADD_TASK ) {
            sendToTask(0);
            return true;
        }else if(id == Constants.ActionBarIds.USER_PREFS){
            String[] tabList = {getString(R.string.terms),getString(R.string.courses),getString(R.string.assessments),getString(R.string.tasks)};
            LayoutInflater li = LayoutInflater.from(this);
            final View prefsView = li.inflate(R.layout.prefs_dialog, null);
            final TextView daysInput = prefsView.findViewById(R.id.numDays);
            daysInput.setText(Integer.toString(numQueryDays));

            final Spinner mSpinner= (Spinner) prefsView.findViewById(R.id.tabDropDown);
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
                                    } else{
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

    private void sendToTerm(int id){
        Intent intent = new Intent(HomeActivitybackup.this, TermActivity.class);
        Uri uri = Uri.parse(TermsProvider.CONTENT_URI + "/" + id);
        intent.putExtra(Constants.CURRENT_URI, uri);
        startActivityForResult(intent,0);
    }

    private void sendToProf(int id){
        Intent intent = new Intent(HomeActivitybackup.this, ProfessorActivity.class);
        Uri uri = Uri.parse(ProfProvider.CONTENT_URI + "/" + id);
        intent.putExtra(Constants.CURRENT_URI, uri);
        startActivityForResult(intent,0);
    }

    private void sendToTask(int id){
        Intent intent = new Intent(HomeActivitybackup.this, TaskActivity.class);
        Uri uri = Uri.parse(TasksProvider.CONTENT_URI + "/" + id);
        intent.putExtra(Constants.CURRENT_URI, uri);
        startActivityForResult(intent,0);
    }

    protected void refreshPage(int id){
        setTitle();
        Bundle b = new Bundle();
        b.putString("contentUri", TermsProvider.CONTENT_URI.toString());
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.TERM_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", HomeCoursesProvider.CONTENT_URI.toString());
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.HOME_COURSE_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", HomeAssessmentsProvider.CONTENT_URI.toString());
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.HOME_ASSESSMENT_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", TasksProvider.CONTENT_URI.toString());
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.TASK_ID,b,this);
        selectDefaultTab();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int groupId = item.getGroupId();

        if (groupId == Constants.MenuGroups.MANAGEMENT_GROUP) {
            if(id == Constants.MenuGroups.TERM_GROUP ){
                sendToTerm(0);
            }else if(id == Constants.MenuGroups.PROF_GROUP){
                sendToProf(0);
            }else if(id == Constants.MenuGroups.TASK_GROUP){
                sendToTask(0);
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getWhereClause(){
        String num=Integer.toString(numQueryDays);

        if(numQueryDays>0){
            sortOrder="";
            return " between strftime("+Utils.getSqlDateNow()+") and strftime("+Utils.getSqlDateNow()+",'"+num+" days')";
        }else if(numQueryDays<0){
            sortOrder="desc";
            return " between strftime("+Utils.getSqlDateNow()+",'"+num+" days') and strftime("+Utils.getSqlDateNow()+",'-1 day')";
        }else{
            sortOrder="";
            return " = strftime("+Utils.getSqlDateNow()+")";
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if(bundle!=null){
            Uri uri = Uri.parse((String) bundle.get("contentUri"));
            if(uri.equals(TermsProvider.CONTENT_URI)){
                if(numQueryDays>=0){
                    where=Constants.Term.TERM_START_DATE+getWhereClause()+
                        " OR ("+Constants.Term.TERM_START_DATE+" <= strftime("+Utils.getSqlDateNow()+") " +
                                "AND "+Constants.Term.TERM_END_DATE+" >= strftime("+Utils.getSqlDateNow()+"))";
                }else{
                    where=Constants.Term.TERM_END_DATE+getWhereClause();
                }

                order=Constants.Term.TERM_START_DATE+" "+sortOrder+","+Constants.Term.TERM_END_DATE+" "+sortOrder;
            }else if(uri.equals(HomeCoursesProvider.CONTENT_URI)){
                //raw query
                if(numQueryDays>=0){
                    where="WHERE c."+Constants.Course.COURSE_START_DATE+getWhereClause()+
                            " OR (c."+Constants.Course.COURSE_START_DATE+" <= strftime("+Utils.getSqlDateNow()+") " +
                            "AND c."+Constants.Course.COURSE_END_DATE+" >= strftime("+Utils.getSqlDateNow()+"))";
                }else{
                    where="WHERE c."+Constants.Course.COURSE_END_DATE+getWhereClause();
                }
                where+=" ORDER BY c."+Constants.Course.COURSE_START_DATE +" "+sortOrder+", c."+Constants.Course.COURSE_END_DATE+" "+sortOrder;
            }else if(uri.equals(HomeAssessmentsProvider.CONTENT_URI)){
                //raw query
                where="WHERE a."+Constants.Assessment.ASSESSMENT_END_DATE+getWhereClause();
                where+=" ORDER BY a."+Constants.Assessment.ASSESSMENT_END_DATE+" "+sortOrder;
            }else if(uri.equals(TasksProvider.CONTENT_URI)){
                if(numQueryDays>=0){
                    where=Constants.Task.TASK_START_DATE+getWhereClause()+
                            " OR ("+Constants.Task.TASK_START_DATE+" <= strftime("+Utils.getSqlDateNow()+") " +
                            "AND "+Constants.Task.TASK_END_DATE+" >= strftime("+Utils.getSqlDateNow()+"))";
                }else{
                    where=Constants.Task.TASK_END_DATE+getWhereClause();
                }

                order=Constants.Task.TASK_START_DATE+" "+sortOrder+","+Constants.Task.TASK_END_DATE+" "+sortOrder;
            }
            return new CursorLoader(this, uri,
                    cols,where,null,order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId()==Constants.CursorLoaderIds.TERM_ID){
            termCursorAdapter.swapCursor(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_COURSE_ID){
            courseCursorAdapter.swapCursor(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_ASSESSMENT_ID){
            assessmentCursorAdapter.swapCursor(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.TASK_ID){
            taskCursorAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId()==Constants.CursorLoaderIds.TERM_ID){
           termCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_COURSE_ID){
            courseCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.HOME_ASSESSMENT_ID){
            assessmentCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.TASK_ID){
            taskCursorAdapter.swapCursor(null);
        }
    }
}

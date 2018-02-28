package com.proj.abhi.mytermplanner.activities;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.proj.abhi.mytermplanner.cursorAdapters.AssessmentsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.ProfsCursorAdapter;
import com.proj.abhi.mytermplanner.pojos.ProfPojo;
import com.proj.abhi.mytermplanner.providers.AssessmentsProvider;
import com.proj.abhi.mytermplanner.providers.CoursesProfsProvider;
import com.proj.abhi.mytermplanner.providers.ProfProvider;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.providers.CoursesProvider;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.MaskWatcher;
import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CourseActivity extends GenericActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{

    private int termId;
    private EditText title,startDate,endDate,notes;
    private Spinner status;
    private CursorAdapter assessmentCursorAdapter = new AssessmentsCursorAdapter(this,null,0);
    private CursorAdapter profsCursorAdapter = new ProfsCursorAdapter(this,null,0);
    private CursorAdapter profsCursorExcludeAdapter = new ProfsCursorAdapter(this,null,0);
    private ListView assessmentList,profList;
    private String[] reminderFields;
    private int[] reminderFieldIds;
    private String assessmentMsg,profMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayout(R.layout.course_header);
        initTabs();
        initReminderFields();
        //init currentUri
        Intent intent = getIntent();
        termId= intent.getIntExtra(Constants.Ids.TERM_ID,0);
        if(intent.hasExtra(Constants.CURRENT_URI)){
            currentUri= intent.getParcelableExtra(Constants.CURRENT_URI);
        }else{
            currentUri = Uri.parse(CoursesProvider.CONTENT_URI+"/"+0);
        }

        //init cursor loaders
        getLoaderManager().initLoader(Constants.CursorLoaderIds.COURSE_ID,null,this);
        Bundle b = new Bundle();
        b.putString("contentUri", AssessmentsProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.ASSESSMENT_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", CoursesProfsProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE,b,this);


        //init screen fields
        assessmentList = (ListView) findViewById(R.id.assessmentList);
        assessmentList.setAdapter(assessmentCursorAdapter);
        profList = (ListView) findViewById(R.id.profList);
        profList.setAdapter(profsCursorAdapter);
        title=(EditText) findViewById(R.id.courseTitle);
        notes=(EditText) findViewById(R.id.notes);
        startDate=(EditText) findViewById(R.id.startDate);
        endDate=(EditText) findViewById(R.id.endDate);
        startDate.addTextChangedListener(new MaskWatcher("##/##/####"));
        endDate.addTextChangedListener(new MaskWatcher("##/##/####"));
        assessmentList.setClickable(true);
        profList.setClickable(true);
        assessmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(getCurrentUriId()>0){
                    Intent intent = new Intent(CourseActivity.this, AssessmentActivity.class);
                    Uri uri = Uri.parse(AssessmentsProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(Constants.CURRENT_URI, uri);
                    intent.putExtra(Constants.Ids.COURSE_ID,getCurrentUriId());
                    startActivityForResult(intent,0);
                }
            }
        });

        profList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(getCurrentUriId()>0){
                    Intent intent = new Intent(CourseActivity.this, ProfessorActivity.class);
                    Uri uri = Uri.parse(ProfProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(Constants.CURRENT_URI, uri);
                    startActivityForResult(intent,0);
                }
            }
        });

        profList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Long val=new Long(l);
                final int id=val.intValue();
                Log.d(null, "onClick: "+l);
                DialogInterface.OnClickListener dialogClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (button == DialogInterface.BUTTON_POSITIVE) {
                                    String where = Constants.Ids.COURSE_ID+"="+getCurrentUriId()
                                            +" AND "+Constants.Ids.PROF_ID+"="+id;
                                    getContentResolver().delete(CoursesProfsProvider.CONTENT_URI,where,null);
                                    Bundle b = new Bundle();
                                    b.putString("contentUri",CoursesProfsProvider.CONTENT_URI.toString());
                                    getLoaderManager().restartLoader(Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE,b,CourseActivity.this);
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted_prof, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        };

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CourseActivity.this);
                alertDialogBuilder.setTitle(R.string.do_delete)
                        .setPositiveButton(getString(R.string.delete), dialogClickListener)
                        .setNegativeButton(getString(android.R.string.no), dialogClickListener);

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
                return true;
            }
        });

        initSpinner();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        handleRotation(savedInstanceState,false);
        refreshPage(getCurrentUriId());
        refreshMenu();
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

    private void initSpinner(){
        status=(Spinner) findViewById(R.id.status);
        List<String> list = new ArrayList<String>();
        list.add("Not Started");
        list.add("In Progress");
        list.add("Complete");
        list.add("Dropped");
        list.add("Failed");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(dataAdapter);
    }

    protected void refreshMenu(){
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.COURSE_ID,null,this);
    }

    protected void save() throws Exception{
        ContentValues values = new ContentValues();
        //all validations throw exceptions on failure to prevent saving
        try{
            //title cant be empty
            if(title.getText()!=null && !title.getText().toString().trim().equals("")){
                values.put(Constants.Course.COURSE_TITLE,title.getText().toString());
            }else{throw new CustomException(getString(R.string.error_empty_title));}
            //start date must be valid
            if(Utils.isValidDate(startDate.getText().toString())) {
                values.put(Constants.Course.COURSE_START_DATE, Utils.getDbDateTime(startDate.getText().toString()));
            }
            //end date must be valid
            if(Utils.isValidDate(endDate.getText().toString())) {
                values.put(Constants.Course.COURSE_END_DATE, Utils.getDbDateTime(endDate.getText().toString()));
            }
            //start date must be before end date
            Utils.isBefore(startDate.getText().toString(),endDate.getText().toString());

            //save status and notes
            values.put(Constants.Course.STATUS,status.getSelectedItem().toString());
            values.put(Constants.Course.NOTES,notes.getText().toString());
        }catch (CustomException e){
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if(getCurrentUriId()>0){
            getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        }else{
            values.put(Constants.Ids.TERM_ID,termId);
            currentUri=getContentResolver().insert(currentUri, values);
        }

        refreshMenu();
        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
    }

    protected void addItemsInNavMenuDrawer(Cursor c) {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        menu.removeItem(Constants.MenuGroups.COURSE_GROUP);
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.COURSE_GROUP,Constants.MenuGroups.COURSE_GROUP,0,R.string.courses);
        submenu.setGroupCheckable(Constants.MenuGroups.COURSE_GROUP,false,true);
        submenu.add(Constants.MenuGroups.COURSE_GROUP,0,0, R.string.create_course);
        while (c.moveToNext()){
            submenu.add(Constants.MenuGroups.COURSE_GROUP,c.getInt(c.getColumnIndex(Constants.ID)),0,c.getString(c.getColumnIndex(Constants.Course.COURSE_TITLE)));
        }
        selectNavItem(submenu);
        c.close();
        navView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setTitle(R.string.delete_all_assessments);
        menu.findItem(R.id.action_delete).setTitle(R.string.delete_course);
        menu.findItem(R.id.action_add).setTitle(R.string.add_assessment);
        menu.add(0,Constants.ActionBarIds.ADD_REMINDER,0,R.string.add_reminder);
        menu.add(0,Constants.ActionBarIds.ADD_PROF,0, R.string.add_prof);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final int uriId =getCurrentUriId();

        if (id == R.id.action_delete && uriId>0) {
            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                if(assessmentList.getAdapter().getCount()==0){
                                    String cancelAlarmsWhere=" WHERE "+Constants.Ids.COURSE_ID+"="+getCurrentUriId();
                                    new AlarmTask(CourseActivity.this,null, null).cancelAlarms(cancelAlarmsWhere);
                                    getContentResolver().delete(currentUri,
                                            Constants.ID+"="+currentUri.getLastPathSegment(),null);
                                    refreshPage(0);
                                    selectDefaultTab();
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG).show();
                                    refreshMenu();
                                }else{
                                    Snackbar.make(mCoordinatorLayout, R.string.error_delete_course, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    };

            doAlert(dialogClickListener);

            return true;
        }else  if(id == R.id.action_delete_all && uriId>0){
            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                if(assessmentList.getAdapter().getCount()>0){
                                    String cancelAlarmsWhere=" WHERE "+Constants.Ids.COURSE_ID+"="+getCurrentUriId()+
                                            " AND "+ Constants.Ids.ASSESSMENT_ID+" IS NOT NULL";
                                    new AlarmTask(CourseActivity.this,null, null).cancelAlarms(cancelAlarmsWhere);

                                    getContentResolver().delete(AssessmentsProvider.CONTENT_URI,
                                            Constants.Ids.COURSE_ID+"="+currentUri.getLastPathSegment(),null);
                                    refreshPage(uriId);
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted_all_assessments, Snackbar.LENGTH_LONG).show();
                                }else{
                                    Snackbar.make(mCoordinatorLayout, R.string.error_no_assessments, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    };

            doAlert(dialogClickListener);

            return true;
        }else if (id == Constants.ActionBarIds.ADD_REMINDER && uriId>0) {
            refreshPage(getCurrentUriId());
            Intent intent = new Intent(this, this.getClass());
            intent.putExtra(Constants.CURRENT_URI, currentUri);
            Bundle b = new Bundle();
            b.putString(Constants.PersistAlarm.CONTENT_TITLE,title.getText().toString());
            b.putString(Constants.PersistAlarm.USER_OBJECT,Constants.Tables.TABLE_COURSE);
            b.putInt(Constants.Ids.COURSE_ID,getCurrentUriId());
            b.putInt(Constants.Ids.TERM_ID,termId);
            b.putParcelable(Constants.CURRENT_INTENT,intent);
            createReminder(reminderFields,reminderFieldIds,b);
        }else if (id == R.id.action_add && uriId>0) {
            Intent intent = new Intent(CourseActivity.this, AssessmentActivity.class);
            Uri uri = Uri.parse(AssessmentsProvider.CONTENT_URI + "/" + 0);
            intent.putExtra(Constants.CURRENT_URI, uri);
            intent.putExtra(Constants.Ids.COURSE_ID,getCurrentUriId());
            intent.putExtra(Constants.Ids.TERM_ID,termId);
            startActivityForResult(intent,0);
        }else if (id == Constants.ActionBarIds.ADD_PROF && uriId>0){
            if(uriId>0){
                Bundle b = new Bundle();
                b.putString("contentUri", CoursesProfsProvider.CONTENT_URI.toString());
                getLoaderManager().restartLoader(Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE,b,this);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void openProfView(Cursor c){
        LayoutInflater li = LayoutInflater.from(this);
        final View dialogView = li.inflate(R.layout.add_prof_dialog, null);
        final Spinner prof= (Spinner) dialogView.findViewById(R.id.profDropDown);
        SimpleCursorAdapter mAdapter;
        if(c.getCount()>0){
            ArrayList<ProfPojo> addProfList = new ArrayList<>();
            if(c.getCount()>=1){
                while(c.moveToNext()){
                    addProfList.add(new ProfPojo(c.getInt(c.getColumnIndex(Constants.ID)),c.getString(c.getColumnIndex("fullName"))));
                }
            }
            ArrayAdapter<ProfPojo> adapter = new ArrayAdapter<ProfPojo>(this,android.R.layout.simple_spinner_item,addProfList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            prof.setAdapter(adapter);
        }

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            ProfPojo pp = (ProfPojo) prof.getSelectedItem();
                            ContentValues values = new ContentValues();
                            values.put(Constants.Ids.PROF_ID,pp.getId());
                            values.put(Constants.Ids.COURSE_ID,getCurrentUriId());
                            getContentResolver().insert(CoursesProfsProvider.CONTENT_URI,values);
                            Bundle b = new Bundle();
                            b.putString("contentUri",CoursesProfsProvider.CONTENT_URI.toString());
                            getLoaderManager().restartLoader(Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE,b,CourseActivity.this);
                            Snackbar.make(mCoordinatorLayout, R.string.added_prof, Snackbar.LENGTH_LONG).show();
                        }else if(button == DialogInterface.BUTTON_NEUTRAL){
                            Intent intent = new Intent(CourseActivity.this, ProfessorActivity.class);
                            Uri uri = Uri.parse(ProfProvider.CONTENT_URI + "/" + 0);
                            intent.putExtra(Constants.CURRENT_URI, uri);
                            startActivityForResult(intent,0);
                        }
                    }
                };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.add_prof)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .setNeutralButton(getString(R.string.create_prof),dialogClickListener);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    protected void emptyPage(){
        currentUri= Uri.parse(CoursesProvider.CONTENT_URI+"/"+0);
        title.setText(null);
        startDate.setText(null);
        endDate.setText(null);
        notes.setText(null);
        status.setSelection(0);
        assessmentCursorAdapter.swapCursor(null);
        profsCursorAdapter.swapCursor(null);
    }

    protected void refreshPage(int id){
        currentUri = Uri.parse(CoursesProvider.CONTENT_URI+"/"+id);
        if(id>0){
            Cursor c = getContentResolver().query(currentUri,null,
                    Constants.ID+"="+currentUri.getLastPathSegment(),null,null);
            c.moveToFirst();
            title.setText(c.getString(c.getColumnIndex(Constants.Course.COURSE_TITLE)));
            notes.setText(c.getString(c.getColumnIndex(Constants.Course.NOTES)));
            startDate.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Course.COURSE_START_DATE))));
            endDate.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Course.COURSE_END_DATE))));
            String statusText=c.getString(c.getColumnIndex(Constants.Course.STATUS));
            termId=c.getInt(c.getColumnIndex(Constants.Ids.TERM_ID));
            status.setSelection(0);
            for(int i=0; i<status.getCount();i++){
                if(status.getItemAtPosition(i).equals(statusText)){
                    status.setSelection(i);
                    break;
                }
            }

            c.close();
            this.setTitle(title.getText().toString());
            Bundle b = new Bundle();
            b.putString("contentUri",AssessmentsProvider.CONTENT_URI.toString());
            getLoaderManager().restartLoader(Constants.CursorLoaderIds.ASSESSMENT_ID,b,this);
            b = new Bundle();
            b.putString("contentUri",CoursesProfsProvider.CONTENT_URI.toString());
            getLoaderManager().restartLoader(Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE,b,this);
        }else{
            emptyPage();
            this.setTitle(getString(R.string.course_editor));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int groupId = item.getGroupId();

        if(groupId == Constants.MenuGroups.COURSE_GROUP){
            selectDefaultTab();
            unSelectCurrNavItem(Constants.MenuGroups.COURSE_GROUP);
            item.setCheckable(true);
            item.setChecked(true);
            this.setTitle(item.getTitle());
            refreshPage(id);
        } else if (id == R.id.nav_share && getCurrentUriId()>0) {
            refreshPage(getCurrentUriId());
            setIntentMsg();
            doShare();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] cols = null;
        String where = null;
        if(bundle!=null){
            Uri uri = Uri.parse((String) bundle.get("contentUri"));
            if(uri.equals(AssessmentsProvider.CONTENT_URI)){
                cols = new String[2];
                cols[0]=Constants.Assessment.ASSESSMENT_TITLE;
                cols[1]=Constants.ID;
                where=Constants.Ids.COURSE_ID+"="+getCurrentUriId();
            }else if(uri.equals(CoursesProfsProvider.CONTENT_URI)){
                //raw query
                if(i==Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE){
                    where=Constants.CoursesProfsSql.QUERY_RELATIONSHIP
                            +"WHERE cp."+Constants.Ids.COURSE_ID+"="+getCurrentUriId();
                }else if(i==Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE){
                    where=Constants.CoursesProfsSql.QUERY_NON_RELATIONSHIP
                            +" AND cp."+Constants.Ids.COURSE_ID+"="+getCurrentUriId()
                            +" WHERE cp."+Constants.Ids.PROF_ID+" IS NULL";
                }

            }
            return new CursorLoader(this, uri,
                    cols,where,null,null);
        }else{
            cols = new String[2];
            cols[0]=Constants.Course.COURSE_TITLE;
            cols[1]=Constants.ID;
            where=Constants.Ids.TERM_ID+"="+termId;
            return new CursorLoader(this, CoursesProvider.CONTENT_URI,
                    cols,where,null,null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor!=null && loader.getId()==Constants.CursorLoaderIds.COURSE_ID){
            addItemsInNavMenuDrawer(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.ASSESSMENT_ID){
            assessmentCursorAdapter.swapCursor(cursor);
            assessmentMsg="";
            if(cursor.getCount()>=1){
                while(cursor.moveToNext()){
                    assessmentMsg+= cursor.getString(cursor.getColumnIndex(Constants.Assessment.TYPE))+" Assessment: "+
                            cursor.getString(cursor.getColumnIndex(Constants.Assessment.ASSESSMENT_TITLE))+"\n";
                }
            }
        }else if(loader.getId()==Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE){
            profsCursorAdapter.swapCursor(cursor);
            profMsg="";
            if(cursor.getCount()>=1){
                while(cursor.moveToNext()){
                    profMsg+= "Professor: "+ cursor.getString(cursor.getColumnIndex(Constants.Professor.TITLE))+
                            " "+cursor.getString(cursor.getColumnIndex(Constants.Professor.LAST_NAME))+"\n";
                }
            }
        }else if(loader.getId()==Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE){
            profsCursorExcludeAdapter.swapCursor(cursor);
            openProfView(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId()==Constants.CursorLoaderIds.ASSESSMENT_ID){
            assessmentCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE){
            profsCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE){
            profsCursorExcludeAdapter.swapCursor(null);
        }
    }

    public void setIntentMsg(){
        intentMsg=("Course Title: "+title.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Course Start Date: "+startDate.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Course End Date: "+endDate.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Course Status: "+status.getSelectedItem().toString());
        intentMsg+=("\n");
        intentMsg+=("Course Notes: "+notes.getText().toString());
        intentMsg+=("\n");
        intentMsg+=profMsg;
        intentMsg+=assessmentMsg;
    }
}

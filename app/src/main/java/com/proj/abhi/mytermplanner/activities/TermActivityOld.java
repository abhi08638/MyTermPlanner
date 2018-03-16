package com.proj.abhi.mytermplanner.activities;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.proj.abhi.mytermplanner.providers.AssessmentsProvider;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.cursorAdapters.CoursesCursorAdapter;
import com.proj.abhi.mytermplanner.providers.CoursesProviderOld;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Utils;

public class TermActivityOld extends GenericActivityOld
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{

    private EditText title,startDate,endDate;
    private CursorAdapter courseCursorAdapter = new CoursesCursorAdapter(this,null,0);
    private ListView courseList;
    private String[] reminderFields;
    private int[] reminderFieldIds;
    private String courseMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addLayout(R.layout.term_header);
        initTabs();
        initReminderFields();

        //init currentUri
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.CURRENT_URI)) {
            currentUri = intent.getParcelableExtra(Constants.CURRENT_URI);
        } else {
            currentUri = Uri.parse(TermsProvider.CONTENT_URI + "/" + 0);
        }

      /*  //init cursor loaders
        getLoaderManager().initLoader(Constants.CursorLoaderIds.TERM_ID, null, this);
        Bundle b = new Bundle();
        b.putString("contentUri", CoursesProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.COURSE_ID, b, this);

        //init screen fields
        courseList = (ListView) findViewById(R.id.courseList);
        courseList.setAdapter(courseCursorAdapter);
        title = (EditText) findViewById(R.id.termTitle);
        startDate = (EditText) findViewById(R.id.startDate);
        endDate = (EditText) findViewById(R.id.endDate);
        startDate.addTextChangedListener(new MaskWatcher("##/##/####"));
        endDate.addTextChangedListener(new MaskWatcher("##/##/####"));
        courseList.setClickable(true);
        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getCurrentUriId() > 0) {
                    Intent intent = new Intent(TermActivityOld.this, CourseActivityOld.class);
                    Uri uri = Uri.parse(CoursesProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(Constants.CURRENT_URI, uri);
                    intent.putExtra(Constants.Ids.TERM_ID, getCurrentUriId());
                    startActivityForResult(intent, 0);
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //restore values after rotation
        handleRotation(savedInstanceState, false);
        refreshMenu();
        refreshPage(getCurrentUriId());*/
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

    protected void refreshMenu(){
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.TERM_ID,null,this);
    }

    protected void save() throws Exception{
        ContentValues termValues = new ContentValues();
        //all validations throw exceptions on failure to prevent saving
        try{
            //title cant be empty
            if(title.getText()!=null && !title.getText().toString().trim().equals("")){
                termValues.put(Constants.Term.TERM_TITLE,title.getText().toString());
            }else{throw new CustomException(getString(R.string.error_empty_title));}
            //start date must be valid
            if(Utils.isValidDate(startDate.getText().toString())) {
                termValues.put(Constants.Term.TERM_START_DATE, Utils.getDbDateTime(startDate.getText().toString()));
            }
            //end date must be valid
            if(Utils.isValidDate(endDate.getText().toString())) {
                termValues.put(Constants.Term.TERM_END_DATE, Utils.getDbDateTime(endDate.getText().toString()));
            }
            //start date must be before end date
            Utils.isBefore(startDate.getText().toString(),endDate.getText().toString());
        }catch (CustomException e){
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if(getCurrentUriId()>0){
            getContentResolver().update(currentUri, termValues, Constants.ID + "=" + getCurrentUriId(), null);
        }else{
            currentUri=getContentResolver().insert(currentUri, termValues);
        }

        refreshMenu();
        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
    }

    protected void addItemsInNavMenuDrawer(Cursor c) {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        menu.removeItem(Constants.MenuGroups.TERM_GROUP);
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.TERM_GROUP,Constants.MenuGroups.TERM_GROUP,0,R.string.terms);
        submenu.setGroupCheckable(Constants.MenuGroups.TERM_GROUP,false,true);
        submenu.add(Constants.MenuGroups.TERM_GROUP,0,0,R.string.create_term);
        while (c.moveToNext()){
            submenu.add(Constants.MenuGroups.TERM_GROUP,c.getInt(c.getColumnIndex(Constants.ID)),0,c.getString(c.getColumnIndex(Constants.Term.TERM_TITLE)));
        }
        selectNavItem(submenu);
        c.close();
        navView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setTitle(R.string.delete_all_corses);
        menu.findItem(R.id.action_delete).setTitle(R.string.delete_term);
        menu.findItem(R.id.action_add).setTitle(R.string.add_course);
        menu.add(0,Constants.ActionBarIds.ADD_REMINDER,0,R.string.add_reminder);
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
                                if(courseList.getAdapter().getCount()==0){
                                    String cancelAlarmsWhere=" WHERE "+Constants.Ids.TERM_ID+"="+getCurrentUriId();
                                    new AlarmTask(TermActivityOld.this,null, null).cancelAlarms(cancelAlarmsWhere);
                                    getContentResolver().delete(currentUri,
                                            Constants.ID+"="+currentUri.getLastPathSegment(),null);
                                    refreshPage(0);
                                    selectDefaultTab();
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG).show();
                                    refreshMenu();
                                }else{
                                    Snackbar.make(mCoordinatorLayout, R.string.error_delete_term, Snackbar.LENGTH_LONG).show();
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
                                if(courseList.getAdapter().getCount()>0){
                                    String cancelAlarmsWhere=" WHERE "+Constants.Ids.TERM_ID+"="+getCurrentUriId()+
                                            " AND "+ Constants.Ids.COURSE_ID+" IS NOT NULL";
                                    new AlarmTask(TermActivityOld.this,null, null).cancelAlarms(cancelAlarmsWhere);
                                    getContentResolver().delete(AssessmentsProvider.CONTENT_URI,
                                            Constants.Ids.TERM_ID+"="+currentUri.getLastPathSegment(),null);
                                    getContentResolver().delete(CoursesProviderOld.CONTENT_URI,
                                            Constants.Ids.TERM_ID+"="+currentUri.getLastPathSegment(),null);
                                    refreshPage(uriId);
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted_all_courses, Snackbar.LENGTH_LONG).show();
                                }else{
                                    Snackbar.make(mCoordinatorLayout, R.string.error_no_courses, Snackbar.LENGTH_LONG).show();
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
            b.putInt(Constants.Ids.TERM_ID,getCurrentUriId());
            b.putString(Constants.PersistAlarm.USER_OBJECT,Constants.Tables.TABLE_TERM);
            b.putParcelable(Constants.CURRENT_INTENT,intent);
            createReminder(reminderFields,reminderFieldIds,b);
        } else if (id == R.id.action_add && uriId>0) {
            if(getCurrentUriId()>0){
                Intent intent = new Intent(TermActivityOld.this, CourseActivityOld.class);
                Uri uri = Uri.parse(CoursesProviderOld.CONTENT_URI + "/" + 0);
                intent.putExtra(Constants.CURRENT_URI, uri);
                intent.putExtra(Constants.Ids.TERM_ID,getCurrentUriId());
                startActivityForResult(intent,0);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void emptyPage(){
        currentUri= Uri.parse(TermsProvider.CONTENT_URI+"/"+0);
        title.setText(null);
        startDate.setText(null);
        endDate.setText(null);
        courseCursorAdapter.swapCursor(null);
    }

    protected void refreshPage(int id){
        currentUri = Uri.parse(TermsProvider.CONTENT_URI+"/"+id);
        if(id>0){
            Cursor c = getContentResolver().query(currentUri,null,
                    Constants.ID+"="+currentUri.getLastPathSegment(),null,null);
            c.moveToFirst();
            title.setText(c.getString(c.getColumnIndex(Constants.Term.TERM_TITLE)));
            startDate.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Term.TERM_START_DATE))));
            endDate.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Term.TERM_END_DATE))));
            c.close();
            this.setTitle(title.getText().toString());
            Bundle b = new Bundle();
            b.putString("contentUri", CoursesProviderOld.CONTENT_URI.toString());
            getLoaderManager().restartLoader(Constants.CursorLoaderIds.COURSE_ID,b,this);
        }else{
            emptyPage();
            this.setTitle(getString(R.string.term_editor));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int groupId = item.getGroupId();

        if(groupId == Constants.MenuGroups.TERM_GROUP){
            selectDefaultTab();
            unSelectCurrNavItem(groupId);
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
            if(uri.equals(CoursesProviderOld.CONTENT_URI)){
                cols = new String[2];
                cols[0]=Constants.Course.COURSE_TITLE;
                cols[1]=Constants.ID;
                where=Constants.Ids.TERM_ID+"="+getCurrentUriId();
            }
            return new CursorLoader(this, uri,
                    cols,where,null,null);
        }else{
            cols = new String[2];
            cols[0]=Constants.Term.TERM_TITLE;
            cols[1]=Constants.ID;
            return new CursorLoader(this, TermsProvider.CONTENT_URI,
                    cols,null,null,null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor!=null && loader.getId()==Constants.CursorLoaderIds.TERM_ID){
            addItemsInNavMenuDrawer(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.COURSE_ID){
            courseCursorAdapter.swapCursor(cursor);
            courseMsg="";
            if(cursor.getCount()>=1){
                while(cursor.moveToNext()){
                    courseMsg+= "Course: "+ cursor.getString(cursor.getColumnIndex(Constants.Course.COURSE_TITLE))+"\n";
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId()==Constants.CursorLoaderIds.COURSE_ID){
            courseCursorAdapter.swapCursor(null);
        }
    }

    public void setIntentMsg(){
        intentMsg=("Term Title: "+title.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Term Start Date: "+startDate.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Term End Date: "+endDate.getText().toString());
        intentMsg+=("\n");
        intentMsg+=courseMsg;
    }
}

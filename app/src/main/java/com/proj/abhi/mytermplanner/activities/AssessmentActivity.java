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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.providers.AssessmentsProvider;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.MaskWatcher;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AssessmentActivity extends GenericActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{

    private int courseId,termId;
    private EditText title,endDate,notes;
    private Spinner type;
    private String[] reminderFields;
    private int[] reminderFieldIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayout(R.layout.assessment_header);
        initTabs();
        initReminderFields();

        //init currentUri
        Intent intent = getIntent();
        courseId= intent.getIntExtra(Constants.Ids.COURSE_ID,0);
        termId= intent.getIntExtra(Constants.Ids.TERM_ID,0);
        if(intent.hasExtra(Constants.CURRENT_URI)){
            currentUri= intent.getParcelableExtra(Constants.CURRENT_URI);
        }else{
            currentUri = Uri.parse(AssessmentsProvider.CONTENT_URI+"/"+0);
        }

        //init cursor loaders
        getLoaderManager().initLoader(Constants.CursorLoaderIds.ASSESSMENT_ID,null,this);

        //init screen fields
        title=(EditText) findViewById(R.id.assessmentTitle);
        notes=(EditText) findViewById(R.id.notes);
        endDate=(EditText) findViewById(R.id.endDate);
        endDate.addTextChangedListener(new MaskWatcher("##/##/####"));
        initSpinner();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        handleRotation(savedInstanceState,false);
        refreshPage(getCurrentUriId());
        refreshMenu();
    }

    private void initReminderFields(){
        //always create size plus 1 to allow for creation of custom date
        reminderFields=new String[1+1];
        reminderFieldIds=new int[reminderFields.length];
        reminderFields[0]=getString(R.string.end_date);
        reminderFieldIds[0]=R.id.endDate;
    }

    private void initSpinner(){
        type=(Spinner) findViewById(R.id.type);
        List<String> list = new ArrayList<String>();
        list.add("Test");
        list.add("Essay");
        list.add("Homework");
        list.add("Performance");
        list.add("Objective");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(dataAdapter);
    }

    protected void refreshMenu(){
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.ASSESSMENT_ID,null,this);
    }

    protected void save() throws Exception{
        ContentValues values = new ContentValues();
        //all validations throw exceptions on failure to prevent saving
        try{
            //title cant be empty
            if(title.getText()!=null && !title.getText().toString().trim().equals("")){
                values.put(Constants.Assessment.ASSESSMENT_TITLE,title.getText().toString());
            }else{throw new CustomException(getString(R.string.error_empty_title));}

            //end date must be valid
            if(Utils.isValidDate(endDate.getText().toString())) {
                values.put(Constants.Assessment.ASSESSMENT_END_DATE, Utils.getDbDateTime(endDate.getText().toString()));
            }

            //save type and notes
            values.put(Constants.Assessment.TYPE,type.getSelectedItem().toString());
            values.put(Constants.Assessment.NOTES,notes.getText().toString());
        }catch (CustomException e){
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if(getCurrentUriId()>0){
            getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        }else{
            values.put(Constants.Ids.COURSE_ID,courseId);
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
        menu.removeItem(Constants.MenuGroups.ASSESSMENT_GROUP);
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.ASSESSMENT_GROUP,Constants.MenuGroups.ASSESSMENT_GROUP,0, R.string.assessments);
        submenu.setGroupCheckable(Constants.MenuGroups.ASSESSMENT_GROUP,false,true);
        submenu.add(Constants.MenuGroups.ASSESSMENT_GROUP,0,0, R.string.create_assessment);
        while (c.moveToNext()){
            submenu.add(Constants.MenuGroups.ASSESSMENT_GROUP,c.getInt(c.getColumnIndex(Constants.ID)),0,
                    c.getString(c.getColumnIndex(Constants.Assessment.ASSESSMENT_TITLE)));
        }
        selectNavItem(submenu);
        c.close();
        navView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setVisible(false);
        menu.findItem(R.id.action_delete).setTitle(R.string.delete_assessment);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.add(0,Constants.ActionBarIds.ADD_REMINDER,0, R.string.add_reminder);
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
                                String cancelAlarmsWhere=" WHERE "+Constants.Ids.ASSESSMENT_ID+"="+getCurrentUriId();
                                new AlarmTask(AssessmentActivity.this,null, null).cancelAlarms(cancelAlarmsWhere);
                                getContentResolver().delete(currentUri,
                                        Constants.ID+"="+currentUri.getLastPathSegment(),null);
                                refreshPage(0);
                                selectDefaultTab();
                                Snackbar.make(mCoordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG).show();
                                refreshMenu();
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
            b.putInt(Constants.Ids.COURSE_ID,courseId);
            b.putInt(Constants.Ids.TERM_ID,termId);
            b.putInt(Constants.Ids.ASSESSMENT_ID,getCurrentUriId());
            b.putString(Constants.PersistAlarm.CONTENT_TITLE,title.getText().toString());
            b.putString(Constants.PersistAlarm.USER_OBJECT,Constants.Tables.TABLE_ASSESSMENT);
            b.putParcelable(Constants.CURRENT_INTENT,intent);
            createReminder(reminderFields,reminderFieldIds,b);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void emptyPage(){
        currentUri= Uri.parse(AssessmentsProvider.CONTENT_URI+"/"+0);
        title.setText(null);
        endDate.setText(null);
        notes.setText(null);
        type.setSelection(0);
    }

    protected void refreshPage(int id){
        currentUri = Uri.parse(AssessmentsProvider.CONTENT_URI+"/"+id);
        if(id>0){
            Cursor c = getContentResolver().query(currentUri,null,
                    Constants.ID+"="+currentUri.getLastPathSegment(),null,null);
            c.moveToFirst();
            title.setText(c.getString(c.getColumnIndex(Constants.Assessment.ASSESSMENT_TITLE)));
            notes.setText(c.getString(c.getColumnIndex(Constants.Assessment.NOTES)));
            endDate.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Assessment.ASSESSMENT_END_DATE))));
            String typeText=c.getString(c.getColumnIndex(Constants.Assessment.TYPE));
            courseId=c.getInt(c.getColumnIndex(Constants.Ids.COURSE_ID));
            termId=c.getInt(c.getColumnIndex(Constants.Ids.TERM_ID));
            type.setSelection(0);
            for(int i=0; i<type.getCount();i++){
                if(type.getItemAtPosition(i).equals(typeText)){
                    type.setSelection(i);
                    break;
                }
            }

            c.close();
            this.setTitle(title.getText().toString());
        }else{
            emptyPage();
            this.setTitle(getString(R.string.assessment_editor));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int groupId = item.getGroupId();

        if(groupId == Constants.MenuGroups.ASSESSMENT_GROUP){
            selectDefaultTab();
            unSelectCurrNavItem(Constants.MenuGroups.ASSESSMENT_GROUP);
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
        cols = new String[2];
        cols[0]=Constants.Assessment.ASSESSMENT_TITLE;
        cols[1]=Constants.ID;
        where=Constants.Ids.COURSE_ID+"="+courseId+" and "+
                Constants.Ids.TERM_ID+"="+termId;
        return new CursorLoader(this, AssessmentsProvider.CONTENT_URI,
                cols,where,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor!=null && loader.getId()==Constants.CursorLoaderIds.ASSESSMENT_ID){
            addItemsInNavMenuDrawer(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setIntentMsg(){
        intentMsg=("Assessment Title: "+title.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Assessment Type: "+type.getSelectedItem().toString());
        intentMsg+=("\n");
        intentMsg+=("Assessment End Date: "+endDate.getText().toString());
        intentMsg+=("\n");
        intentMsg+=("Assessment Notes: "+notes.getText().toString());
        intentMsg+=("\n");
    }
}

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
import android.text.InputType;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.cursorAdapters.EmailsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.PhonesCursorAdapter;
import com.proj.abhi.mytermplanner.providers.EmailsProvider;
import com.proj.abhi.mytermplanner.providers.PhonesProvider;
import com.proj.abhi.mytermplanner.providers.ProfProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProfessorActivity extends GenericActivityOld
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{
    private EditText firstName,middleName,lastName;
    private Spinner title;
    private CursorAdapter phoneCursorAdapter = new PhonesCursorAdapter(this,null,0);
    private CursorAdapter emailCursorAdapter = new EmailsCursorAdapter(this,null,0);
    private ListView phoneList,emailList;
    private String phoneMsg,emailMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayout(R.layout.prof_header);
        initTabs();

        //init currentUri
        Intent intent = getIntent();
        if(intent.hasExtra(Constants.CURRENT_URI)){
            currentUri= intent.getParcelableExtra(Constants.CURRENT_URI);
        }else{
            currentUri = Uri.parse(ProfProvider.CONTENT_URI+"/"+0);
        }

        //init cursor loaders
        getLoaderManager().initLoader(Constants.CursorLoaderIds.PROF_ID,null,this);
        Bundle b = new Bundle();
        b.putString("contentUri", PhonesProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.PHONE_ID,b,this);
        b = new Bundle();
        b.putString("contentUri", EmailsProvider.CONTENT_URI.toString());
        getLoaderManager().initLoader(Constants.CursorLoaderIds.EMAIL_ID,b,this);

        //init screen fields
        phoneList = (ListView) findViewById(R.id.phoneList);
        phoneList.setAdapter(phoneCursorAdapter);
        emailList = (ListView) findViewById(R.id.emailList);
        emailList.setAdapter(emailCursorAdapter);
        title=(Spinner) findViewById(R.id.profTitle);
        firstName=(EditText) findViewById(R.id.firstName);
        middleName=(EditText) findViewById(R.id.middleName);
        lastName=(EditText) findViewById(R.id.lastName);
        initSpinner();
        phoneList.setClickable(true);
        emailList.setClickable(true);
        phoneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(getCurrentUriId()>0){
                    Long l = new Long(id);
                    openPhoneView(l.intValue());
                }
            }
        });

        emailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(getCurrentUriId()>0){
                    Long l = new Long(id);
                    openEmailView(l.intValue());
                }
            }
        });


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //restore values after rotation
        handleRotation(savedInstanceState,false);
        refreshMenu();
        refreshPage(getCurrentUriId());
    }

    private void initSpinner(){
        title=(Spinner) findViewById(R.id.profTitle);
        List<String> list = new ArrayList<String>();
        list.add("Mr.");
        list.add("Ms.");
        list.add("Mrs.");
        list.add("Dr.");
        list.add("Professor");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        title.setAdapter(dataAdapter);
    }

    protected void refreshMenu(){
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.PROF_ID,null,this);
    }

    private void openPhoneView(int id){
        final int phoneId=id;
        String[] list = {"Home","Work","Cell"};
        LayoutInflater li = LayoutInflater.from(this);
        final View dialogView = li.inflate(R.layout.prof_info_dialog, null);
        final TextView phoneLbl = dialogView.findViewById(R.id.inputText);
        final TextView typeLbl = dialogView.findViewById(R.id.spinnerText);
        final TextView phoneNum = dialogView.findViewById(R.id.input);
        phoneLbl.setText(R.string.phone_number);
        typeLbl.setText(R.string.phone_type);
        phoneNum.setInputType( InputType.TYPE_CLASS_PHONE);

        final Spinner type= (Spinner) dialogView .findViewById(R.id.spinner);
        final ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adp);

        if(phoneId>0){
            Cursor c = getContentResolver().query(PhonesProvider.CONTENT_URI,null,Constants.ID + "=" + phoneId,null,null);
            c.moveToFirst();
            phoneNum.setText(c.getString(c.getColumnIndex(Constants.Professor.PHONE)));
            String typeText=c.getString(c.getColumnIndex(Constants.Professor.PHONE_TYPE));
            type.setSelection(0);
            for(int i=0; i<type.getCount();i++){
                if(type.getItemAtPosition(i).equals(typeText)){
                    type.setSelection(i);
                    break;
                }
            }
            c.close();
        }

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            if(phoneNum.getText()!=null && !phoneNum.getText().toString().trim().equals("")) {
                                ContentValues values = new ContentValues();
                                values.put(Constants.Professor.PHONE, phoneNum.getText().toString());
                                values.put(Constants.Professor.PHONE_TYPE, type.getSelectedItem().toString());
                                if (phoneId > 0) {
                                    getContentResolver().update(PhonesProvider.CONTENT_URI, values, Constants.ID + "=" + phoneId, null);
                                } else {
                                    values.put(Constants.Ids.PROF_ID, getCurrentUriId());
                                    getContentResolver().insert(PhonesProvider.CONTENT_URI, values);
                                }
                                refreshPage(getCurrentUriId());
                                Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
                            }else{
                                Snackbar.make(mCoordinatorLayout, R.string.error_empty_phone_num, Snackbar.LENGTH_LONG).show();
                            }
                        }else if(button == DialogInterface.BUTTON_NEUTRAL){
                            if(phoneId>0){
                                getContentResolver().delete(PhonesProvider.CONTENT_URI,Constants.ID + "=" + phoneId,null);
                                refreshPage(getCurrentUriId());
                                Snackbar.make(mCoordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.phone_editor)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .setNeutralButton(getString(R.string.delete), dialogClickListener);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private void openEmailView(int id){
        final int emailId=id;
        String[] list = {"Personal","Work"};
        LayoutInflater li = LayoutInflater.from(this);
        final View dialogView = li.inflate(R.layout.prof_info_dialog, null);
        final TextView emailLbl = dialogView.findViewById(R.id.inputText);
        final TextView typeLbl = dialogView.findViewById(R.id.spinnerText);
        final TextView email = dialogView.findViewById(R.id.input);
        emailLbl.setText(R.string.email_address);
        typeLbl.setText(R.string.email_type);
        email.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );

        final Spinner type= (Spinner) dialogView .findViewById(R.id.spinner);
        final ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adp);

        if(emailId>0){
            Cursor c = getContentResolver().query(EmailsProvider.CONTENT_URI,null,Constants.ID + "=" + emailId,null,null);
            c.moveToFirst();
            email.setText(c.getString(c.getColumnIndex(Constants.Professor.EMAIL)));
            String typeText=c.getString(c.getColumnIndex(Constants.Professor.EMAIL_TYPE));
            type.setSelection(0);
            for(int i=0; i<type.getCount();i++){
                if(type.getItemAtPosition(i).equals(typeText)){
                    type.setSelection(i);
                    break;
                }
            }
            c.close();
        }

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            if(email.getText()!=null && !email.getText().toString().trim().equals("")) {
                                ContentValues values = new ContentValues();
                                values.put(Constants.Professor.EMAIL, email.getText().toString());
                                values.put(Constants.Professor.EMAIL_TYPE, type.getSelectedItem().toString());
                                if (emailId > 0) {
                                    getContentResolver().update(EmailsProvider.CONTENT_URI, values, Constants.ID + "=" + emailId, null);
                                } else {
                                    values.put(Constants.Ids.PROF_ID, getCurrentUriId());
                                    getContentResolver().insert(EmailsProvider.CONTENT_URI, values);
                                }
                                refreshPage(getCurrentUriId());
                                Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
                            }else{
                                Snackbar.make(mCoordinatorLayout, R.string.error_empty_email, Snackbar.LENGTH_LONG).show();
                            }
                        }else if(button == DialogInterface.BUTTON_NEUTRAL){
                            if(emailId>0){
                                getContentResolver().delete(EmailsProvider.CONTENT_URI,Constants.ID + "=" + emailId,null);
                                refreshPage(getCurrentUriId());
                                Snackbar.make(mCoordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.email_editor)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .setNeutralButton(getString(R.string.delete), dialogClickListener);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    protected void save() throws Exception{
        ContentValues values = new ContentValues();
        //all validations throw exceptions on failure to prevent saving
        try{
            //First and last name cant be empty
            if(firstName.getText()!=null && !firstName.getText().toString().trim().equals("")){
                values.put(Constants.Professor.FIRST_NAME,firstName.getText().toString());
            }else{throw new CustomException(getString(R.string.error_empty_first_name));}
            if(lastName.getText()!=null && !lastName.getText().toString().trim().equals("")){
                values.put(Constants.Professor.LAST_NAME,lastName.getText().toString());
            }else{throw new CustomException(getString(R.string.error_empty_last_name));}

            //other data
            values.put(Constants.Professor.MIDDLE_NAME,middleName.getText().toString());
            values.put(Constants.Professor.TITLE,title.getSelectedItem().toString());
        }catch (CustomException e){
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if(getCurrentUriId()>0){
            getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        }else{
            currentUri=getContentResolver().insert(currentUri, values);
        }

        refreshMenu();
        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
    }

    protected void addItemsInNavMenuDrawer(Cursor c) {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        menu.removeItem(Constants.MenuGroups.PROF_GROUP);
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.PROF_GROUP,Constants.MenuGroups.PROF_GROUP,0, R.string.profs);
        submenu.setGroupCheckable(Constants.MenuGroups.PROF_GROUP,false,true);
        submenu.add(Constants.MenuGroups.PROF_GROUP,0,0,R.string.create_prof);
        String name;
        while (c.moveToNext()){
            name=c.getString(c.getColumnIndex(Constants.Professor.TITLE))+" "+c.getString(c.getColumnIndex(Constants.Professor.LAST_NAME));
            submenu.add(Constants.MenuGroups.PROF_GROUP,c.getInt(c.getColumnIndex(Constants.ID)),0,name);
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
        menu.findItem(R.id.action_delete).setTitle(R.string.delete_prof);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.add(0,Constants.ActionBarIds.ADD_PHONE,0, R.string.add_phone);
        menu.add(0,Constants.ActionBarIds.ADD_EMAIL,0, R.string.add_email);
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
        }else if (id == Constants.ActionBarIds.ADD_PHONE && uriId>0) {
            openPhoneView(0);
            return true;
        }else if (id == Constants.ActionBarIds.ADD_EMAIL && uriId>0) {
            openEmailView(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void emptyPage(){
        currentUri= Uri.parse(ProfProvider.CONTENT_URI+"/"+0);
        title.setSelection(0);
        firstName.setText(null);
        lastName.setText(null);
        middleName.setText(null);
        phoneCursorAdapter.swapCursor(null);
        emailCursorAdapter.swapCursor(null);
    }

    protected void refreshPage(int id){
        currentUri = Uri.parse(ProfProvider.CONTENT_URI+"/"+id);
        if(id>0){
            Cursor c = getContentResolver().query(currentUri,null,
                    Constants.ID+"="+currentUri.getLastPathSegment(),null,null);
            c.moveToFirst();
            firstName.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Professor.FIRST_NAME))));
            middleName.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Professor.MIDDLE_NAME))));
            lastName.setText(Utils.getUserDate(c.getString(c.getColumnIndex(Constants.Professor.LAST_NAME))));

            String titleText=c.getString(c.getColumnIndex(Constants.Professor.TITLE));
            title.setSelection(0);
            for(int i=0; i<title.getCount();i++){
                if(title.getItemAtPosition(i).equals(titleText)){
                    title.setSelection(i);
                    break;
                }
            }
            c.close();
            this.setTitle(title.getSelectedItem()+" "+lastName.getText().toString());

            Bundle b = new Bundle();
            b.putString("contentUri",PhonesProvider.CONTENT_URI.toString());
            getLoaderManager().restartLoader(Constants.CursorLoaderIds.PHONE_ID,b,this);
            b = new Bundle();
            b.putString("contentUri",EmailsProvider.CONTENT_URI.toString());
            getLoaderManager().restartLoader(Constants.CursorLoaderIds.EMAIL_ID,b,this);
        }else{
            emptyPage();
            this.setTitle(getString(R.string.prof_editor));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int groupId = item.getGroupId();

        if(groupId == Constants.MenuGroups.PROF_GROUP){
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
            if(uri.equals(PhonesProvider.CONTENT_URI) || uri.equals(EmailsProvider.CONTENT_URI)){
                where=Constants.Ids.PROF_ID+"="+getCurrentUriId();
            }
            return new CursorLoader(this, uri,
                    cols,where,null,null);
        }else{
            return new CursorLoader(this, ProfProvider.CONTENT_URI,
                    cols,null,null,null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor!=null && loader.getId()==Constants.CursorLoaderIds.PROF_ID){
            addItemsInNavMenuDrawer(cursor);
        }else if(loader.getId()==Constants.CursorLoaderIds.PHONE_ID){
            phoneCursorAdapter.swapCursor(cursor);
            phoneMsg="";
            if(cursor.getCount()>=1){
                while(cursor.moveToNext()){
                    phoneMsg+= cursor.getString(cursor.getColumnIndex(Constants.Professor.PHONE_TYPE))+" Phone: "+
                            cursor.getString(cursor.getColumnIndex(Constants.Professor.PHONE))+"\n";
                }
            }
        }else if(loader.getId()==Constants.CursorLoaderIds.EMAIL_ID){
            emailCursorAdapter.swapCursor(cursor);
            emailMsg="";
            if(cursor.getCount()>=1){
                while(cursor.moveToNext()){
                    emailMsg+= cursor.getString(cursor.getColumnIndex(Constants.Professor.EMAIL_TYPE))+" Email: "+
                            cursor.getString(cursor.getColumnIndex(Constants.Professor.EMAIL))+"\n";
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId()==Constants.CursorLoaderIds.PHONE_ID){
            phoneCursorAdapter.swapCursor(null);
        }else if(loader.getId()==Constants.CursorLoaderIds.EMAIL_ID){
            emailCursorAdapter.swapCursor(null);
        }
    }

    public void setIntentMsg(){
        intentMsg=("Professor: "+title.getSelectedItem()+" "+firstName.getText().toString()
                +" "+middleName.getText().toString()+" "+lastName.getText().toString());
        intentMsg+=("\n");
        intentMsg+=phoneMsg;
        intentMsg+=emailMsg;
    }
}

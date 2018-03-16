package com.proj.abhi.mytermplanner.fragments.listFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.activities.ContactActivity;
import com.proj.abhi.mytermplanner.activities.CourseActivity;
import com.proj.abhi.mytermplanner.activities.CourseActivityOld;
import com.proj.abhi.mytermplanner.cursorAdapters.ContactsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.HomeCoursesCursorAdapter;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.pojos.ProfPojo;
import com.proj.abhi.mytermplanner.pojos.SpinnerPojo;
import com.proj.abhi.mytermplanner.providers.ContactsProvider;
import com.proj.abhi.mytermplanner.providers.CoursesContactsProvider;
import com.proj.abhi.mytermplanner.providers.CoursesProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ContactListFragment extends GenericListFragment implements LoaderCallbacks<Cursor> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cursorAdapter = new ContactsCursorAdapter(getActivity(), null, 0);
        setEmptyText("No " + getString(R.string.contacts));
        setListAdapter(cursorAdapter);
        if(savedInstanceState==null)
            initLoader();

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
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
                                    String where = Constants.Ids.COURSE_ID+"="+initializer.getInt(Constants.Ids.COURSE_ID)
                                            +" AND "+Constants.Ids.PROF_ID+"="+id;
                                    getActivity().getContentResolver().delete(CoursesContactsProvider.CONTENT_URI,where,null);
                                    Bundle b = new Bundle();
                                    b.putString("contentUri", CoursesContactsProvider.CONTENT_URI.toString());
                                    restartLoader();
                                    Snackbar.make(mCoordinatorLayout, R.string.deleted_prof, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        };

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle(R.string.do_delete)
                        .setPositiveButton(getString(R.string.delete), dialogClickListener)
                        .setNegativeButton(getString(android.R.string.no), dialogClickListener);

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
                return true;
            }
        });
    }

    public void restartLoader(Bundle b) {
        initializer.putInt(Constants.Ids.COURSE_ID,b.getInt(Constants.Ids.COURSE_ID));
        if(b.getInt(Constants.CURSOR_LOADER_ID)==Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE){
            getLoaderManager().restartLoader(Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE, initializer, this);
        }else{
            super.restartLoader();
        }
    }

    private void addContactView(Cursor c){
        LayoutInflater li = LayoutInflater.from(getActivity());
        final View dialogView = li.inflate(R.layout.add_contact_dialog, null);
        final Spinner contact= (Spinner) dialogView.findViewById(R.id.contactDropDown);
        if(c.getCount()>0){
            ArrayList<SpinnerPojo> addProfList = new ArrayList<>();
            if(c.getCount()>=1){
                while(c.moveToNext()){
                    addProfList.add(new SpinnerPojo(c.getInt(c.getColumnIndex(Constants.ID)),c.getString(c.getColumnIndex("fullName"))));
                }
            }
            ArrayAdapter<SpinnerPojo> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,addProfList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            contact.setAdapter(adapter);
        }

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            SpinnerPojo sp = (SpinnerPojo) contact.getSelectedItem();
                            ContentValues values = new ContentValues();
                            values.put(Constants.Ids.PROF_ID,sp.getId());
                            values.put(Constants.Ids.COURSE_ID,initializer.getInt(Constants.Ids.COURSE_ID));
                            getActivity().getContentResolver().insert(CoursesContactsProvider.CONTENT_URI,values);
                            restartLoader();
                            Snackbar.make(mCoordinatorLayout, R.string.added_prof, Snackbar.LENGTH_LONG).show();
                        }else if(button == DialogInterface.BUTTON_NEUTRAL){
                            Utils.sendToActivity(0,ContactActivity.class,ContactsProvider.CONTENT_URI);
                        }
                    }
                };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(R.string.add_prof)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .setNeutralButton(getString(R.string.create_prof),dialogClickListener);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] cols = null;
        String where = null;
        String order = null;
        if (bundle != null) {
            Uri uri = Uri.parse((String) bundle.get(Constants.CONTENT_URI));
           if (uri.equals(CoursesContactsProvider.CONTENT_URI)) {
               if(id==Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE){
                   where=Constants.CoursesProfsSql.QUERY_RELATIONSHIP
                           +"WHERE cp."+Constants.Ids.COURSE_ID+"="+bundle.getInt(Constants.Ids.COURSE_ID);
               }else if(id==Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE){
                   where=Constants.CoursesProfsSql.QUERY_NON_RELATIONSHIP
                           +" AND cp."+Constants.Ids.COURSE_ID+"="+bundle.getInt(Constants.Ids.COURSE_ID)
                           +" WHERE cp."+Constants.Ids.PROF_ID+" IS NULL";
               }
            }
            return new CursorLoader(getActivity(), uri,
                    cols, where, null, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId()==Constants.CursorLoaderIds.COURSE_PROF_ID_INCLUDE){
            cursorAdapter.swapCursor(cursor);
            /*profMsg="";
            if(cursor.getCount()>=1){
                while(cursor.moveToNext()){
                    profMsg+= "Professor: "+ cursor.getString(cursor.getColumnIndex(Constants.Professor.TITLE))+
                            " "+cursor.getString(cursor.getColumnIndex(Constants.Professor.LAST_NAME))+"\n";
                }
            }*/
        }else if(loader.getId()==Constants.CursorLoaderIds.COURSE_PROF_ID_EXCLUDE){
            addContactView(cursor);
        }
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        if (id > 0) {
            Long l = new Long(id);
            Utils.sendToActivity(l.intValue(), ContactActivity.class, ContactsProvider.CONTENT_URI);
        }
    }
}

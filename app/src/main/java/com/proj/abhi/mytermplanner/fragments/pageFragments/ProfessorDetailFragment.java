package com.proj.abhi.mytermplanner.fragments.pageFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.listFragments.ProfessorListFragments;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericDetailFragment;
import com.proj.abhi.mytermplanner.pojos.ProfessorPojo;
import com.proj.abhi.mytermplanner.providers.ProfProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProfessorDetailFragment extends GenericDetailFragment {
    private EditText firstName,middleName,lastName;
    private Spinner title;
    private ProfessorPojo profPojo = new ProfessorPojo();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.prof_header_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //init screen fields
        title=(Spinner) getActivity().findViewById(R.id.profTitle);
        title.setSelection(0);
        firstName=(EditText) getActivity().findViewById(R.id.firstName);
        middleName=(EditText) getActivity().findViewById(R.id.middleName);
        lastName=(EditText) getActivity().findViewById(R.id.lastName);
        initSpinner();
        if(savedInstanceState==null) {
            refreshPage(getCurrentUriId());
        }else{
            profPojo=(ProfessorPojo) profPojo.initJson(savedInstanceState.getString(profPojo.className));
        }
        pojo=profPojo;
    }

    @Override
    protected void initReminderFields() {
    }

    private void initSpinner(){
        title=(Spinner) getActivity().findViewById(R.id.profTitle);
        List<String> list = new ArrayList<String>();
        list.add("Mr.");
        list.add("Ms.");
        list.add("Mrs.");
        list.add("Dr.");
        list.add("Professor");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        title.setAdapter(dataAdapter);
    }

    public Uri refreshPage(int i) {
        final int id = i;
        currentUri = Uri.parse(ProfProvider.CONTENT_URI + "/" + id);
        Log.d(null, "handleRotation: "+currentUri);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (id > 0) {
                    final Cursor c = getActivity().getContentResolver().query(currentUri, null,
                            Constants.ID + "=" + getCurrentUriId(), null, null);
                    c.moveToFirst();
                    profPojo.initPojo(c,title);
                    c.close();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setSelection(profPojo.getTitleIndex());
                            firstName.setText(profPojo.getFirstName());
                            middleName.setText(profPojo.getMiddleName());
                            lastName.setText(profPojo.getLastName());
                            getActivity().setTitle(title.getSelectedItem()+" "+profPojo.getLastName());
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            emptyPage();
                            getActivity().setTitle(getString(R.string.prof_editor));
                        }
                    });
                }
            }
        });
        return currentUri;
    }

    protected void emptyPage() {
        currentUri = Uri.parse(ProfProvider.CONTENT_URI + "/" + 0);
        title.setSelection(0);
        firstName.setText(null);
        middleName.setText(null);
        lastName.setText(null);
        profPojo.reset();
    }

    private void mapObject(ProfessorPojo prof){
        prof.setTitleIndex(title.getSelectedItemPosition());
        prof.setFirstName(firstName.getText().toString());
        prof.setMiddleName(middleName.getText().toString());
        prof.setLastName(lastName.getText().toString());
    }

    public Uri save() throws Exception {
        ContentValues values = new ContentValues();
        ProfessorPojo tempPojo=new ProfessorPojo();
        //all validations throw exceptions on failure to prevent saving
        try {
            mapObject(tempPojo);
            //title cant be empty
            if(Utils.hasValue(tempPojo.getFirstName())){
                values.put(Constants.Professor.FIRST_NAME,tempPojo.getFirstName());
            }else{throw new CustomException(getString(R.string.error_empty_first_name));}

            if(Utils.hasValue(tempPojo.getLastName())){
                values.put(Constants.Professor.LAST_NAME,tempPojo.getLastName());
            }else{throw new CustomException(getString(R.string.error_empty_last_name));}

            values.put(Constants.Professor.MIDDLE_NAME,tempPojo.getMiddleName());
            values.put(Constants.Professor.TITLE,title.getItemAtPosition(tempPojo.getTitleIndex()).toString());
        } catch (CustomException e) {
            Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            throw e;
        }

        if (getCurrentUriId() > 0) {
            getActivity().getContentResolver().update(currentUri, values, Constants.ID + "=" + getCurrentUriId(), null);
        } else {
            currentUri = getActivity().getContentResolver().insert(currentUri, values);
        }
        profPojo=tempPojo;
        refreshPage(getCurrentUriId());
        Snackbar.make(mCoordinatorLayout, R.string.saved, Snackbar.LENGTH_LONG).show();
        return currentUri;
    }

    public void doReminder(Context context, Class clazz) {
    }

    public void setIntentMsg() {
        intentMsg=(title.getItemAtPosition(profPojo.getTitleIndex())+" "+profPojo.getFirstName()
                +" "+profPojo.getMiddleName()+" "+profPojo.getLastName()+"\n");
        ProfessorListFragments phoneFragment = (ProfessorListFragments) ((GenericActivity)getActivity()).getFragmentByTitle(getActivity().getString(R.string.phones));
        ProfessorListFragments emailFragment = (ProfessorListFragments) ((GenericActivity)getActivity()).getFragmentByTitle(getActivity().getString(R.string.emails));
        intentMsg+=phoneFragment.getMsg();
        intentMsg+=emailFragment.getMsg();

    }
}

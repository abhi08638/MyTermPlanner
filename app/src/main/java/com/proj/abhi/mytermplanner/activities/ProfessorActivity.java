package com.proj.abhi.mytermplanner.activities;

import android.app.AlertDialog;
import android.app.Fragment;
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
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.cursorAdapters.EmailsCursorAdapter;
import com.proj.abhi.mytermplanner.cursorAdapters.PhonesCursorAdapter;
import com.proj.abhi.mytermplanner.fragments.listFragments.AlarmListFragment;
import com.proj.abhi.mytermplanner.fragments.listFragments.ProfessorListFragments;
import com.proj.abhi.mytermplanner.fragments.pageFragments.ProfessorDetailFragment;
import com.proj.abhi.mytermplanner.fragments.pageFragments.TaskDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.pageAdapters.CustomPageAdapter;
import com.proj.abhi.mytermplanner.pojos.NavMenuPojo;
import com.proj.abhi.mytermplanner.providers.EmailsProvider;
import com.proj.abhi.mytermplanner.providers.PhonesProvider;
import com.proj.abhi.mytermplanner.providers.ProfProvider;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProfessorActivity extends GenericActivity
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
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.CURRENT_URI)) {
            currentUri = intent.getParcelableExtra(Constants.CURRENT_URI);
        } else {
            currentUri = Uri.parse(ProfProvider.CONTENT_URI + "/" + 0);
        }

        //init cursor loaders
        navBundle.putInt(Constants.CURSOR_LOADER_ID,Constants.CursorLoaderIds.PROF_ID);
        getLoaderManager().initLoader(Constants.CursorLoaderIds.PROF_ID, navBundle, this);
        handleRotation(savedInstanceState);
        //init tabs
        initViewPager();

        navMenuPojo=new NavMenuPojo(Constants.MenuGroups.PROF_GROUP,getString(R.string.profs),
                getString(R.string.create_prof),Constants.Professor.TITLE,Constants.Professor.LAST_NAME);
    }

    @Override
    protected void save() throws Exception {
        ProfessorDetailFragment professorDetailFragment = (ProfessorDetailFragment) getFragmentByTitle(R.string.details);
        currentUri=professorDetailFragment.save();
        refreshMenu();
    }

    @Override
    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            Bundle b = new Bundle();
            b.putParcelable(Constants.CURRENT_URI, currentUri);
            ProfessorDetailFragment profDetailFragment = new ProfessorDetailFragment();
            profDetailFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.CONTENT_URI, PhonesProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.PHONE_ID);
            ProfessorListFragments phoneFragment = new ProfessorListFragments();
            phoneFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.CONTENT_URI, EmailsProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.EMAIL_ID);
            ProfessorListFragments emailFragment = new ProfessorListFragments();
            emailFragment.setArguments(b);

            adapter.addFragment(profDetailFragment, getString(R.string.details));
            adapter.addFragment(phoneFragment, getString(R.string.phones));
            adapter.addFragment(emailFragment, getString(R.string.emails));
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(adapter.getCount());
            initTabs(viewPager);
        }
    }

    @Override
    protected void refreshPage(int id) {
        //handles switching tasks from nav bar
        for (android.support.v4.app.Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof GenericDetailFragment) {
                currentUri = ((GenericDetailFragment) f).refreshPage(id);
            }else if (f instanceof GenericListFragment) {
                ((GenericListFragment) f).restartLoader();
            }
        }
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
                                delete(null);
                            }
                        }
                    };

            doAlert(dialogClickListener);
            return true;
        }else if (id == Constants.ActionBarIds.ADD_PHONE && uriId>0) {
            ProfessorListFragments phoneFragment = (ProfessorListFragments) getFragmentByTitle(R.string.phones);
            phoneFragment.openPhoneView(0);
            return true;
        }else if (id == Constants.ActionBarIds.ADD_EMAIL && uriId>0) {
            ProfessorListFragments emailFragment = (ProfessorListFragments) getFragmentByTitle(R.string.emails);
            emailFragment.openEmailView(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

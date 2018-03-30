package com.proj.abhi.mytermplanner.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.listFragments.PhoneEmailListFragments;
import com.proj.abhi.mytermplanner.fragments.pageFragments.ContactDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.pojos.NavMenuPojo;
import com.proj.abhi.mytermplanner.providers.ContactsProvider;
import com.proj.abhi.mytermplanner.providers.EmailsProvider;
import com.proj.abhi.mytermplanner.providers.PhonesProvider;
import com.proj.abhi.mytermplanner.utils.Constants;

public class ContactActivity extends GenericActivity
{

    @Override
    protected Class getChildClass() {
        return ContactActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.CURRENT_URI)) {
            currentUri = intent.getParcelableExtra(Constants.CURRENT_URI);
        } else {
            currentUri = Uri.parse(ContactsProvider.CONTENT_URI + "/" + 0);
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
        ContactDetailFragment professorDetailFragment = (ContactDetailFragment) getFragmentByTitle(R.string.details);
        currentUri=professorDetailFragment.save();
        refreshMenu();
    }

    @Override
    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            Bundle b = new Bundle();
            b.putParcelable(Constants.CURRENT_URI, currentUri);
            ContactDetailFragment profDetailFragment = new ContactDetailFragment();
            profDetailFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.CONTENT_URI, PhonesProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.PHONE_ID);
            PhoneEmailListFragments phoneFragment = new PhoneEmailListFragments();
            phoneFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.CONTENT_URI, EmailsProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.EMAIL_ID);
            PhoneEmailListFragments emailFragment = new PhoneEmailListFragments();
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
            PhoneEmailListFragments phoneFragment = (PhoneEmailListFragments) getFragmentByTitle(R.string.phones);
            phoneFragment.openPhoneView(0);
            return true;
        }else if (id == Constants.ActionBarIds.ADD_EMAIL && uriId>0) {
            PhoneEmailListFragments emailFragment = (PhoneEmailListFragments) getFragmentByTitle(R.string.emails);
            emailFragment.openEmailView(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

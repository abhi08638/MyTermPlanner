package com.proj.abhi.mytermplanner.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.listFragments.AlarmListFragment;
import com.proj.abhi.mytermplanner.fragments.listFragments.CourseListFragment;
import com.proj.abhi.mytermplanner.fragments.pageFragments.TermDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.pojos.NavMenuPojo;
import com.proj.abhi.mytermplanner.providers.CoursesProviderOld;
import com.proj.abhi.mytermplanner.providers.CoursesProvider;
import com.proj.abhi.mytermplanner.providers.TermsProvider;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.LinkedHashMap;

public class TermActivity extends GenericActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.CURRENT_URI)) {
            currentUri = intent.getParcelableExtra(Constants.CURRENT_URI);
        } else {
            currentUri = Uri.parse(TermsProvider.CONTENT_URI + "/" + 0);
        }
        //init cursor loaders
        navBundle.putInt(Constants.CURSOR_LOADER_ID,Constants.CursorLoaderIds.TERM_ID);
        navBundle.putString(Constants.Sql.COL1,Constants.Term.TERM_TITLE);
        getLoaderManager().initLoader(0, navBundle, this);
        handleRotation(savedInstanceState);
        //init tabs
        initViewPager();

        navMenuPojo=new NavMenuPojo(Constants.MenuGroups.TERM_GROUP,getString(R.string.terms),
                getString(R.string.create_term),Constants.Term.TERM_TITLE);
    }

    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            Bundle b = new Bundle();
            b.putParcelable(Constants.CURRENT_URI, currentUri);
            TermDetailFragment termDetailFragment = new TermDetailFragment();
            termDetailFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.CONTENT_URI, CoursesProvider.CONTENT_URI.toString());
            b.putInt(Constants.CURSOR_LOADER_ID, Constants.CursorLoaderIds.HOME_COURSE_ID);
            b.putInt(Constants.Ids.TERM_ID, getCurrentUriId());
            CourseListFragment courseFragment = new CourseListFragment();
            courseFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.Sql.WHERE, Constants.SqlSelect.QUERY_ALARMS +
                    "WHERE " + Constants.Ids.TERM_ID + "=" + getCurrentUriId() +
                    " ORDER BY " + Constants.PersistAlarm.NOTIFY_DATETIME);
            AlarmListFragment reminderFragment = new AlarmListFragment();
            reminderFragment.setArguments(b);

            adapter.addFragment(termDetailFragment, getString(R.string.details));
            adapter.addFragment(courseFragment, getString(R.string.courses));
            adapter.addFragment(reminderFragment, getString(R.string.reminders));
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(adapter.getCount());
            initTabs(viewPager);
        }
    }

    protected void save() throws Exception {
        TermDetailFragment termDetailFragment = (TermDetailFragment) getFragmentByTitle(R.string.details);
        currentUri=termDetailFragment.save();
        refreshMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setVisible(false);
        menu.findItem(R.id.action_delete).setTitle(R.string.delete_term);
        menu.findItem(R.id.action_add).setTitle(R.string.add_course);
        menu.add(0,Constants.ActionBarIds.ADD_REMINDER,0,R.string.add_reminder);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final int uriId = getCurrentUriId();

        if (id == R.id.action_delete && uriId > 0) {
            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE) {
                                String cancelAlarmsWhere = " WHERE " + Constants.Ids.TERM_ID + "=" + getCurrentUriId();
                                delete(cancelAlarmsWhere);
                            }
                        }
                    };

            doAlert(dialogClickListener);

            return true;
        } else if (id == Constants.ActionBarIds.ADD_REMINDER && uriId > 0) {
            TermDetailFragment termDetailFragment = (TermDetailFragment) getFragmentByTitle(R.string.details);
            termDetailFragment.doReminder(this,TermActivity.class);
        } else if (id == R.id.action_add && uriId>0) {
            if(getCurrentUriId()>0){
                LinkedHashMap<String,Integer> params = new LinkedHashMap<>();
                params.put(Constants.Ids.TERM_ID,getCurrentUriId());
                Uri uri = Uri.parse(CoursesProvider.CONTENT_URI + "/" + 0);
                Utils.sendToActivity(0,CourseActivity.class,uri,params);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void refreshPage(int id) {
        Bundle b = new Bundle();
        b.putInt(Constants.Ids.TERM_ID, id);
        //handles switching tasks from nav bar
        for (android.support.v4.app.Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof GenericDetailFragment) {
                currentUri = ((GenericDetailFragment) f).refreshPage(id);
            }else if (f instanceof GenericListFragment) {
                ((GenericListFragment) f).restartLoader(b);
            }
        }
    }
}

package com.proj.abhi.mytermplanner.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.listFragments.AlarmListFragment;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.generics.GenericDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericListFragment;
import com.proj.abhi.mytermplanner.pageAdapters.CustomPageAdapter;
import com.proj.abhi.mytermplanner.fragments.pageFragments.TaskDetailFragment;
import com.proj.abhi.mytermplanner.pojos.NavMenuPojo;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.utils.Constants;

public class TaskActivity extends GenericActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.CURRENT_URI)) {
            currentUri = intent.getParcelableExtra(Constants.CURRENT_URI);
        } else {
            currentUri = Uri.parse(TasksProvider.CONTENT_URI + "/" + 0);
        }
        //init tabs
        initViewPager();

        //init cursor loaders
        navBundle.putInt(Constants.CURSOR_LOADER_ID,Constants.CursorLoaderIds.TASK_ID);
        navBundle.putString(Constants.Sql.COL1,Constants.Task.TASK_TITLE);
        getLoaderManager().initLoader(Constants.CursorLoaderIds.TASK_ID, navBundle, this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navMenuPojo=new NavMenuPojo(Constants.MenuGroups.TASK_GROUP,getString(R.string.tasks),
                getString(R.string.create_task),Constants.Task.TASK_TITLE);
    }

    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            CustomPageAdapter adapter = new CustomPageAdapter(getSupportFragmentManager());
            Bundle b = new Bundle();
            b.putParcelable(Constants.CURRENT_URI, currentUri);
            TaskDetailFragment taskDetailFragment = new TaskDetailFragment();
            taskDetailFragment.setArguments(b);

            b = new Bundle();
            b.putString(Constants.Sql.WHERE, Constants.SqlSelect.QUERY_ALARMS +
                    "WHERE " + Constants.Ids.TASK_ID + "=" + getCurrentUriId() +
                    " ORDER BY " + Constants.PersistAlarm.NOTIFY_DATETIME);
            AlarmListFragment reminderFragment = new AlarmListFragment();
            reminderFragment.setArguments(b);


            adapter.addFragment(taskDetailFragment, getString(R.string.details));
            adapter.addFragment(reminderFragment, getString(R.string.reminders));
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(adapter.getCount());
            initTabs(viewPager);
        }
    }

    protected void save() throws Exception {
        for (android.support.v4.app.Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof TaskDetailFragment) {
                currentUri = ((TaskDetailFragment) f).save();
                break;
            }
        }
        refreshMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setVisible(false);
        menu.findItem(R.id.action_delete).setTitle(R.string.delete_task);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.add(0, Constants.ActionBarIds.ADD_REMINDER, 0, R.string.add_reminder);
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
                                String cancelAlarmsWhere = " WHERE " + Constants.Ids.TASK_ID + "=" + getCurrentUriId();
                                delete(cancelAlarmsWhere);
                            }
                        }
                    };

            doAlert(dialogClickListener);

            return true;
        } else if (id == Constants.ActionBarIds.ADD_REMINDER && uriId > 0) {
            for (android.support.v4.app.Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof GenericDetailFragment) {
                    ((GenericDetailFragment) f).doReminder(this, TaskActivity.class);
                    break;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

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
}

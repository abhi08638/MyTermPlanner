package com.proj.abhi.mytermplanner.activities;

import android.app.LoaderManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.pageAdapters.CustomPageAdapter;
import com.proj.abhi.mytermplanner.fragments.TaskDetailFragment;
import com.proj.abhi.mytermplanner.providers.TasksProvider;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;

public class TaskActivity extends GenericActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent.hasExtra(Constants.CURRENT_URI)){
            currentUri= intent.getParcelableExtra(Constants.CURRENT_URI);
        }else{
            currentUri = Uri.parse(TasksProvider.CONTENT_URI+"/"+0);
        }
        //init tabs
        initViewPager();

        //init cursor loaders
        getLoaderManager().initLoader(Constants.CursorLoaderIds.TASK_ID,null,this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        handleRotation(savedInstanceState,false);
        refreshMenu();
    }

    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if(viewPager!=null){
            CustomPageAdapter adapter = new CustomPageAdapter(getSupportFragmentManager());
            Bundle b = new Bundle();
            b.putParcelable(Constants.CURRENT_URI, currentUri);
            TaskDetailFragment taskDetailFragment= new TaskDetailFragment();
            taskDetailFragment.setArguments(b);

            adapter.addFragment(taskDetailFragment, getString(R.string.details));
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(adapter.getCount());
            initTabs(viewPager);
        }
    }

    protected void refreshMenu(){
        getLoaderManager().restartLoader(Constants.CursorLoaderIds.TASK_ID,null,this);
    }

    protected void save() throws Exception{
        for(android.support.v4.app.Fragment f:getSupportFragmentManager().getFragments()){
            if(f instanceof TaskDetailFragment){
                currentUri=((TaskDetailFragment) f).save();
                break;
            }
        }
        refreshMenu();
    }

    protected void addItemsInNavMenuDrawer(Cursor c) {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        menu.removeItem(Constants.MenuGroups.TASK_GROUP);
        SubMenu submenu = menu.addSubMenu(Constants.MenuGroups.TASK_GROUP,Constants.MenuGroups.TASK_GROUP,0, R.string.tasks);
        submenu.setGroupCheckable(Constants.MenuGroups.TASK_GROUP,false,true);
        submenu.add(Constants.MenuGroups.TASK_GROUP,0,0, R.string.create_task);
        while (c.moveToNext()){
            submenu.add(Constants.MenuGroups.TASK_GROUP,c.getInt(c.getColumnIndex(Constants.ID)),0,
                    c.getString(c.getColumnIndex(Constants.Task.TASK_TITLE)));
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
        menu.findItem(R.id.action_delete).setTitle(R.string.delete_task);
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
                                String cancelAlarmsWhere=" WHERE "+Constants.Ids.TASK_ID+"="+getCurrentUriId();
                                new AlarmTask(TaskActivity.this,null, null).cancelAlarms(cancelAlarmsWhere);
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
            for(android.support.v4.app.Fragment f:getSupportFragmentManager().getFragments()){
                if(f instanceof TaskDetailFragment){
                    ((TaskDetailFragment) f).doReminder(this,TaskActivity.class);
                    break;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void refreshPage(int id){
        for(android.support.v4.app.Fragment f:getSupportFragmentManager().getFragments()){
            if(f instanceof TaskDetailFragment){
                currentUri=((TaskDetailFragment) f).refreshPage(id);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int groupId = item.getGroupId();

        if(groupId == Constants.MenuGroups.TASK_GROUP){
            selectDefaultTab();
            unSelectCurrNavItem(Constants.MenuGroups.TASK_GROUP);
            item.setCheckable(true);
            item.setChecked(true);
            //this.setTitle(item.getTitle());
            refreshPage(id);
        } else if (id == R.id.nav_share && getCurrentUriId()>0) {
            for(android.support.v4.app.Fragment f:getSupportFragmentManager().getFragments()){
                if(f instanceof TaskDetailFragment){
                    ((TaskDetailFragment) f).doShare();
                    break;
                }
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {;
        String where = null;
        String[] cols = new String[2];
        cols[0]=Constants.Task.TASK_TITLE;
        cols[1]=Constants.ID;
        return new CursorLoader(this, TasksProvider.CONTENT_URI,
                cols,where,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor!=null && loader.getId()==Constants.CursorLoaderIds.TASK_ID){
            addItemsInNavMenuDrawer(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

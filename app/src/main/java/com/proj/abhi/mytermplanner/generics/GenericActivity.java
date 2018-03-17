package com.proj.abhi.mytermplanner.generics;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.activities.HomeActivity;
import com.proj.abhi.mytermplanner.activities.SettingsActivity;
import com.proj.abhi.mytermplanner.pageAdapters.CustomPageAdapter;
import com.proj.abhi.mytermplanner.pojos.NavMenuPojo;
import com.proj.abhi.mytermplanner.services.AlarmClient;
import com.proj.abhi.mytermplanner.services.AlarmTask;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DateUtils;
import com.proj.abhi.mytermplanner.utils.PreferenceSingleton;
import com.proj.abhi.mytermplanner.utils.Utils;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public abstract class GenericActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>
{

    protected Uri currentUri;
    protected CoordinatorLayout mCoordinatorLayout;
    protected AlarmClient alarmClient;
    protected int defaultTabIndex=0;
    protected Bundle navBundle=new Bundle();
    protected ViewPager viewPager;
    protected NavMenuPojo navMenuPojo;
    protected TabLayout tabLayout = null;
    protected Toolbar toolbar = null;
    protected CustomPageAdapter adapter = new CustomPageAdapter(getSupportFragmentManager());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DateUtils.context=this;
        Utils.context=this;
        initSharedPreferences();
        setTheme(PreferenceSingleton.getThemeId());
        AppCompatDelegate.setDefaultNightMode(PreferenceSingleton.getNightModeId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        initContentView(drawer);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    save();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initNavHeader(navigationView);

        alarmClient = new AlarmClient(this);
        alarmClient.doBindService();
        navBundle.putInt(Constants.CURSOR_LOADER_ID,Constants.CursorLoaderIds.NONE);
        PreferenceSingleton.setPageIntent(getIntent());
    }

    @Override
    public void onResume(){
        super.onResume();
        if(PreferenceSingleton.wasNightModeChanged() && !getIntent().equals(PreferenceSingleton.getPageIntent())){
            getDelegate().setLocalNightMode(PreferenceSingleton.getNightModeId());
            PreferenceSingleton.setWasNightModeChanged(false);
            recreate();
        }
    }

    private void initNavHeader(NavigationView navigationView){
        View headerLayout = navigationView.getHeaderView(0);

        //workaround for setting background drawable in code for api<21
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {Utils.getThemeColor(R.attr.colorPrimary),Utils.getThemeColor(R.attr.colorPrimary),Utils.getThemeColor(R.attr.colorPrimaryDark)});
        gd.setCornerRadius(0f);
        gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gd.setShape(GradientDrawable.RECTANGLE);
        headerLayout.setBackgroundDrawable(gd);

        ImageButton nightBtn = headerLayout.findViewById(R.id.nightBtn);
        nightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    int currentNightMode = getResources().getConfiguration().uiMode
                            & Configuration.UI_MODE_NIGHT_MASK;
                    PreferenceSingleton.setWasNightModeChanged(true);
                    switch (currentNightMode) {
                        case Configuration.UI_MODE_NIGHT_NO:
                            PreferenceSingleton.setNightModeId(AppCompatDelegate.MODE_NIGHT_YES);
                            recreate();
                            break;
                        case Configuration.UI_MODE_NIGHT_YES:
                            PreferenceSingleton.setNightModeId(AppCompatDelegate.MODE_NIGHT_NO);
                            recreate();
                            break;
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            PreferenceSingleton.setNightModeId(AppCompatDelegate.MODE_NIGHT_YES);
                            recreate();
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void initContentView(DrawerLayout home){
        LayoutInflater inflater = LayoutInflater.from(this);
        View inflatedLayout;
        if(PreferenceSingleton.isHideTabBar() && !PreferenceSingleton.isHideToolbar()){
            inflatedLayout= inflater.inflate(R.layout.app_bar_home_no_tab, null, false);
        }
        else {
            inflatedLayout = inflater.inflate(R.layout.app_bar_home, null, false);
        }
        home.addView(inflatedLayout,0);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        setSupportActionBar(toolbar);
        if(PreferenceSingleton.isHideToolbar()){
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            if(PreferenceSingleton.isHideTabBar()){
                AppBarLayout.LayoutParams tabParams = (AppBarLayout.LayoutParams) tabLayout.getLayoutParams();
                tabParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            }
        }
    }

    private void initSharedPreferences() {
        //init query params
        if(!PreferenceSingleton.isInit()){
            SharedPreferences sharedpreferences = getSharedPreferences(Constants.SharedPreferenceKeys.USER_PREFS, Context.MODE_PRIVATE);
            //this.getSharedPreferences(Constants.SharedPreferenceKeys.USER_PREFS, 0).edit().clear().commit();
            //init home query days
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, 7);
                editor.apply();
            } else {
                PreferenceSingleton.setNumQueryDays(sharedpreferences.getInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, 7));
            }

            //init default tab
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.DEFAULT_TAB)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Constants.SharedPreferenceKeys.DEFAULT_TAB, defaultTabIndex);
                editor.apply();
            } else {
                PreferenceSingleton.setHomeDefTabIndex(sharedpreferences.getInt(Constants.SharedPreferenceKeys.DEFAULT_TAB, 0));
            }

            //init theme
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.THEME)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Constants.SharedPreferenceKeys.THEME, R.style.AppThemeBlue);
                editor.apply();
            }
            PreferenceSingleton.setThemeId(sharedpreferences.getInt(Constants.SharedPreferenceKeys.THEME, R.style.AppThemeBlue));

            //init night mode
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.NIGHT_MODE)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Constants.SharedPreferenceKeys.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_AUTO);
                editor.apply();
            }
            PreferenceSingleton.setNightModeId(sharedpreferences.getInt(Constants.SharedPreferenceKeys.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_AUTO));

            //init toolbar pref
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.HIDE_TOOLBAR)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.SharedPreferenceKeys.HIDE_TOOLBAR, true);
                editor.apply();
            }
            PreferenceSingleton.setHideToolbar(sharedpreferences.getBoolean(Constants.SharedPreferenceKeys.HIDE_TOOLBAR,true));

            //init tabbar pref
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.HIDE_TABBAR)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.SharedPreferenceKeys.HIDE_TABBAR, false);
                editor.apply();
            }
            PreferenceSingleton.setHideTabBar(sharedpreferences.getBoolean(Constants.SharedPreferenceKeys.HIDE_TABBAR,false));

            //init schoolMode pref
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.SCHOOL_MODE)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.SharedPreferenceKeys.SCHOOL_MODE, true);
                editor.apply();
            }
            PreferenceSingleton.setSchoolMode(sharedpreferences.getBoolean(Constants.SharedPreferenceKeys.SCHOOL_MODE,true));

            //init LED color
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.LED_COLOR)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Constants.SharedPreferenceKeys.LED_COLOR, Color.BLUE);
                editor.apply();
            }
            PreferenceSingleton.setLedColorId(sharedpreferences.getInt(Constants.SharedPreferenceKeys.LED_COLOR, Color.BLUE));

            //init default notification type
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE, 0);
                editor.apply();
            }
            PreferenceSingleton.setDefaultNotifyType(sharedpreferences.getInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE,0));

            //init default notification type
            Gson gson = new Gson();
            if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN)) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN, gson.toJson(PreferenceSingleton.getVibratePattern()));
                editor.apply();
            }
            PreferenceSingleton.setVibratePattern(gson.fromJson(sharedpreferences.getString(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN,null),PreferenceSingleton.getVibratePattern().getClass()));

            PreferenceSingleton.setWasNightModeChanged(false);
            PreferenceSingleton.setInit(true);
        }
    }

    public Fragment getFragmentByTitle(String title){
        return adapter.getFragmentByTitle(title);
    }

    public Fragment getFragmentByTitle(int id){
        return adapter.getFragmentByTitle(getString(id));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Persist selected bundle across orientation changes.
        if(currentUri!=null)
            outState.putString(Constants.CURRENT_URI, currentUri.toString());
    }

    protected void handleRotation(Bundle savedInstanceState){
        // Recreate state if applicable.
        if (savedInstanceState != null) {
            // Get selected uri from saved state.
            if(savedInstanceState.containsKey(Constants.CURRENT_URI))
                currentUri = Uri.parse((String) savedInstanceState.get(Constants.CURRENT_URI));
        }
        refreshMenu();
    }

    protected void initTabs(ViewPager viewPager){
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(5);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        if(tabLayout.getTabCount()>3) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }else{
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Utils.closeKeyboard();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public int getCurrentUriId(){
        try{
            return Integer.parseInt(currentUri.getLastPathSegment());
        }catch (Exception e){
            return 0;
        }
    }

    protected void selectNavItem(SubMenu subMenu) {
        MenuItem item = subMenu.findItem(getCurrentUriId());
        if(item!=null){
            this.setTitle(item.getTitle());
            item.setCheckable(true);
            item.setChecked(true);
        }
    }

    protected void unSelectCurrNavItem() {
        int prevId = getCurrentUriId();
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        SubMenu subMenu = menu.findItem(navMenuPojo.getMenuGroup()).getSubMenu();
        MenuItem prevItem = subMenu.findItem(prevId);
        prevItem.setChecked(false);
    }

    protected void refreshMenu() {
        if(navBundle.getInt(Constants.CURSOR_LOADER_ID)!=Constants.CursorLoaderIds.NONE)
            getLoaderManager().restartLoader(navBundle.getInt(Constants.CURSOR_LOADER_ID), navBundle, this);
    }

    protected abstract void save() throws Exception;

    protected abstract void initViewPager();

    protected void addItemsInNavMenuDrawer(Cursor c) {
        if(navMenuPojo!=null){
            try{
                //cant use async task because crash occurs when saving and pressing tab too quickly
                NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
                Menu menu = navView.getMenu();
                menu.removeItem(navMenuPojo.getMenuGroup());
                SubMenu submenu = menu.addSubMenu(navMenuPojo.getMenuGroup(), navMenuPojo.getMenuGroup(), 0, navMenuPojo.getGroupName());
                submenu.setGroupCheckable(navMenuPojo.getMenuGroup(), false, true);
                submenu.add(navMenuPojo.getMenuGroup(), 0, 0, navMenuPojo.getGroupHeaderName());
                String itemName;
                while (c.moveToNext()) {
                    itemName="";
                    for (String col:navMenuPojo.getItemNames()){
                        itemName+=c.getString(c.getColumnIndex(col))+" ";
                    }
                    submenu.add(navMenuPojo.getMenuGroup(), c.getInt(c.getColumnIndex(Constants.ID)), 0,
                            itemName.trim());
                }
                selectNavItem(submenu);
                c.close();
                navView.invalidate();
            }catch (Exception e){
                c.close();
                e.printStackTrace();
            }
        }
    }

    protected void doAbout(MenuItem item){
        int id = item.getItemId();

        if (id==R.id.nav_about) {
            LayoutInflater li = LayoutInflater.from(this);
            final View aboutView = li.inflate(R.layout.about_alert, null);
            DialogInterface.OnCancelListener dialogClickListener =
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {}
                    };

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(aboutView);
            alertDialogBuilder.setTitle(R.string.about)
                    .setOnCancelListener(dialogClickListener);
            alertDialogBuilder.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected abstract void refreshPage(int id);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    protected void selectDefaultTab(){
        if(tabLayout.getTabCount()<defaultTabIndex){
            defaultTabIndex=0;
        }
        tabLayout.getTabAt(defaultTabIndex).select();
    }

    protected void delete(String cancelAlarmsWhere){
        if(cancelAlarmsWhere!=null)
            new AlarmTask(this, null, null).cancelAlarms(cancelAlarmsWhere);
        getContentResolver().delete(currentUri,
                Constants.ID + "=" + currentUri.getLastPathSegment(), null);
        refreshPage(0);
        selectDefaultTab();
        Snackbar.make(mCoordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG).show();
        refreshMenu();
    }

    protected void setDefaultTabIndex(int index){
        defaultTabIndex=index;
    }

    @Override
    protected void onStop() {
        stopAlertClient();
        super.onStop();
    }

    protected void stopAlertClient(){
        if(alarmClient != null)
            alarmClient.doUnbindService();
    }

    public void setAlarmForNotification(Date date, Bundle userBundle){
        //used inside fragments to call service
        alarmClient.setAlarmForNotification(date,userBundle);
    }

    protected void doAlert(DialogInterface.OnClickListener dialogClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        doAbout(item);
        // Handle navigation view item clicks here.
        final int id = item.getItemId();
        int groupId = item.getGroupId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (navMenuPojo!=null && groupId == navMenuPojo.getMenuGroup()) {
            selectDefaultTab();
            unSelectCurrNavItem();
            item.setCheckable(true);
            item.setChecked(true);
            this.setTitle(item.getTitle());
            refreshPage(id);
        } else if (id == R.id.nav_share && getCurrentUriId() > 0) {
            for (android.support.v4.app.Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof GenericDetailFragment) {
                    ((GenericDetailFragment) f).doShare();
                    break;
                }
            }
        } else if (id == R.id.nav_settings || id==R.id.home) {
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            if (id==R.id.nav_settings)
                                Utils.sendToActivity(0, SettingsActivity.class,null);
                            else
                                Utils.sendToActivity(0, HomeActivity.class,null);
                        }
                    },
                    200
            );
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String where = bundle.getString(Constants.Sql.WHERE);
        String[] cols = new String[2];
        if(bundle.containsKey(Constants.Sql.COL1)){
            cols[0] = bundle.getString(Constants.Sql.COL1);
            cols[1] = Constants.ID;
        }else{
            cols=null;
        }
        Uri uri = Uri.parse(currentUri.toString().substring(0,currentUri.toString().lastIndexOf("/")));
        return new CursorLoader(this, uri,
                cols, where, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        addItemsInNavMenuDrawer(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void initActivityFromFragment(Bundle b){}
}

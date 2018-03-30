package com.proj.abhi.mytermplanner.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.pageFragments.SettingsDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericActivity;
import com.proj.abhi.mytermplanner.utils.Utils;

public class SettingsActivity extends GenericActivity implements ColorPickerDialogListener
{
    private boolean didSave=false;

    @Override
    protected Class getChildClass() {
        return SettingsActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewPager();
        addItemsInNavMenuDrawer();
    }

    @Override
    protected void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            SettingsDetailFragment settingsDetailFragment = new SettingsDetailFragment();
            adapter.addFragment(settingsDetailFragment, getString(R.string.details));
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(adapter.getCount());
            initTabs(viewPager);
            tabLayout.setVisibility(View.GONE);
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(0);
        }
    }

    protected void save() throws Exception{
        SettingsDetailFragment settingsDetailFragment = (SettingsDetailFragment) getFragmentByTitle(R.string.details);
        settingsDetailFragment.save();
        didSave=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_delete_all).setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);
        return true;
    }

    protected void addItemsInNavMenuDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        //get rid of share item
        menu.getItem(0).getSubMenu().getItem(0).setVisible(false);
        menu.getItem(0).getSubMenu().getItem(2).setVisible(false);

        navView.invalidate();
    }

    @Override
    public void onBackPressed() {
        if(didSave){
            Utils.sendToActivity(0,HomeActivity.class,null);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    protected void refreshPage(int id){
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        SettingsDetailFragment settingsDetailFragment = (SettingsDetailFragment) getFragmentByTitle(R.string.details);
        settingsDetailFragment.setColor(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}

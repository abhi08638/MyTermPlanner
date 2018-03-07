package com.proj.abhi.mytermplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.fragments.pageFragments.SettingsDetailFragment;
import com.proj.abhi.mytermplanner.generics.GenericActivity;

public class SettingsActivity extends GenericActivity implements ColorPickerDialogListener
{
    private boolean didSave=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewPager();
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

    @Override
    public void onBackPressed() {
        if(didSave){
            Intent clearBackStack=new Intent(this,HomeActivity.class);
            clearBackStack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            clearBackStack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            clearBackStack.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(clearBackStack);
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

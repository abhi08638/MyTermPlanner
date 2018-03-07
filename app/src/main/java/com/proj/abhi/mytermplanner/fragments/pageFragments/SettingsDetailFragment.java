package com.proj.abhi.mytermplanner.fragments.pageFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.pojos.SpinnerPojo;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.PreferenceSingleton;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsDetailFragment extends Fragment {
    private TextView daysInput;
    private Spinner mDefTabSpinner, mThemeSpinner, mNightModeSpinner;
    private CheckBox hideToolbar, hideTabBar;
    private SharedPreferences sharedpreferences;
    private CoordinatorLayout mCoordinatorLayout;
    private ArrayList<SpinnerPojo> themeList = new ArrayList();
    private ArrayList<SpinnerPojo> nightModeList = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);
        sharedpreferences = getActivity().getSharedPreferences(Constants.SharedPreferenceKeys.USER_PREFS, Context.MODE_PRIVATE);
        initHomeSettings();
        initUiSettings();
        if (savedInstanceState == null) {
            initPreferences();
        }
    }

    private void initHomeSettings() {
        daysInput = getActivity().findViewById(R.id.numDays);
        mDefTabSpinner = (Spinner) getActivity().findViewById(R.id.tabDropDown);
        String[] tabList = {getString(R.string.terms), getString(R.string.courses), getString(R.string.assessments), getString(R.string.tasks), getString(R.string.reminders)};
        final ArrayAdapter<String> adp = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, tabList);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDefTabSpinner.setAdapter(adp);
    }

    private void initUiSettings() {
        mThemeSpinner = (Spinner) getActivity().findViewById(R.id.themeDropDown);
        themeList.add(new SpinnerPojo(R.style.AppThemeBlue, getString(R.string.blue)));
        themeList.add(new SpinnerPojo(R.style.AppThemeRed, getString(R.string.red)));
        final ArrayAdapter<SpinnerPojo> adp = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, themeList);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mThemeSpinner.setAdapter(adp);

        mNightModeSpinner = (Spinner) getActivity().findViewById(R.id.nightModeDropDown);
        nightModeList.add(new SpinnerPojo(AppCompatDelegate.MODE_NIGHT_AUTO, getString(R.string.auto)));
        nightModeList.add(new SpinnerPojo(AppCompatDelegate.MODE_NIGHT_YES, getString(R.string.always)));
        nightModeList.add(new SpinnerPojo(AppCompatDelegate.MODE_NIGHT_NO, getString(R.string.never)));
        final ArrayAdapter<SpinnerPojo> adpNight = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, nightModeList);
        adpNight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNightModeSpinner.setAdapter(adpNight);

        hideToolbar = getActivity().findViewById(R.id.toolbarCheckbox);
        hideTabBar = getActivity().findViewById(R.id.tabBarCheckbox);

        hideToolbar.setOnCheckedChangeListener(Utils.getCbListener());
        hideTabBar.setOnCheckedChangeListener(Utils.getCbListener());
    }

    private void initPreferences() {
        //init query params
        if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, Integer.toString(7));
            editor.apply();
        } else {
            daysInput.setText(sharedpreferences.getString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, null));
        }

        //init default tab
        if (!sharedpreferences.contains(Constants.SharedPreferenceKeys.DEFAULT_TAB)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.SharedPreferenceKeys.DEFAULT_TAB, Integer.toString(0));
            editor.apply();
        } else {
            mDefTabSpinner.setSelection(Integer.parseInt(sharedpreferences.getString(Constants.SharedPreferenceKeys.DEFAULT_TAB, null)));
        }

        //init theme
        int themeId = sharedpreferences.getInt(Constants.SharedPreferenceKeys.THEME, R.style.AppThemeBlue);
        for (int i = 0; i < themeList.size(); i++) {
            if (themeList.get(i).getId() == themeId) {
                mThemeSpinner.setSelection(i);
                break;
            }
        }

        //init night mode
        int nightModeId = sharedpreferences.getInt(Constants.SharedPreferenceKeys.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_AUTO);
        for (int i = 0; i < nightModeList.size(); i++) {
            if (nightModeList.get(i).getId() == nightModeId) {
                mNightModeSpinner.setSelection(i);
                break;
            }
        }

        hideToolbar.setChecked(sharedpreferences.getBoolean(Constants.SharedPreferenceKeys.HIDE_TOOLBAR, true));
        hideTabBar.setChecked(sharedpreferences.getBoolean(Constants.SharedPreferenceKeys.HIDE_TABBAR, true));
    }

    public void save() throws Exception {
        try {
            int numDays = Integer.parseInt(daysInput.getText().toString());
            if (numDays > 365 || numDays < -365) {
                throw new CustomException(getString(R.string.invalidDays));
            } else {
                SpinnerPojo themeItem = (SpinnerPojo) mThemeSpinner.getSelectedItem();
                SpinnerPojo nightModeItem = (SpinnerPojo) mNightModeSpinner.getSelectedItem();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, Integer.toString(numDays));
                editor.putString(Constants.SharedPreferenceKeys.DEFAULT_TAB, Integer.toString(mDefTabSpinner.getSelectedItemPosition()));
                editor.putInt(Constants.SharedPreferenceKeys.THEME, themeItem.getId());
                editor.putInt(Constants.SharedPreferenceKeys.NIGHT_MODE, nightModeItem.getId());
                editor.putBoolean(Constants.SharedPreferenceKeys.HIDE_TOOLBAR,hideToolbar.isChecked());
                editor.putBoolean(Constants.SharedPreferenceKeys.HIDE_TABBAR,hideTabBar.isChecked());
                editor.apply();
                PreferenceSingleton.setThemeId(themeItem.getId());
                PreferenceSingleton.setNightModeId(nightModeItem.getId());
                PreferenceSingleton.setHideTabBar(hideTabBar.isChecked());
                PreferenceSingleton.setHideToolbar(hideToolbar.isChecked());
                Snackbar.make(mCoordinatorLayout, getString(R.string.saved), Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            if (e instanceof CustomException) {
                Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(mCoordinatorLayout, R.string.preferenceSaveFailed, Snackbar.LENGTH_LONG).show();
            }
            throw e;
        }
    }
}

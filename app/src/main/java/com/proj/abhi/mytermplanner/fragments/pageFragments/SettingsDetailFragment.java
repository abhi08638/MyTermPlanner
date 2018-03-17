package com.proj.abhi.mytermplanner.fragments.pageFragments;

/**
 * Created by Abhi on 2/25/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.pojos.SpinnerPojo;
import com.proj.abhi.mytermplanner.services.NotifyService;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.CustomException;
import com.proj.abhi.mytermplanner.utils.PreferenceSingleton;
import com.proj.abhi.mytermplanner.utils.Utils;

import java.util.ArrayList;

public class SettingsDetailFragment extends Fragment {
    private TextView daysInput, vibratePattern;
    private Spinner mDefTabSpinner, mThemeSpinner, mNightModeSpinner, mReminderTypeSpinner;
    private CheckBox hideToolbar, hideTabBar,schoolMode;
    private SharedPreferences sharedpreferences;
    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton notificationColor;
    private ArrayList<SpinnerPojo> themeList = new ArrayList();
    private ArrayList<SpinnerPojo> nightModeList = new ArrayList();
    private Button testBtn;
    private Gson gson = new Gson();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.settings_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);
        sharedpreferences = getActivity().getSharedPreferences(Constants.SharedPreferenceKeys.USER_PREFS, Context.MODE_PRIVATE);
        initHomeSettings();
        initUiSettings();
        initNotificationSettings();
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
        themeList.add(new SpinnerPojo(R.style.AppThemeGreen, getString(R.string.green)));
        themeList.add(new SpinnerPojo(R.style.AppThemeRed, getString(R.string.red)));
        final ArrayAdapter<SpinnerPojo> adp = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, themeList);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mThemeSpinner.setAdapter(adp);
        mThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SpinnerPojo sp = (SpinnerPojo) mThemeSpinner.getSelectedItem();
                setColor(Utils.getThemeColor(sp.getId(),R.attr.colorPrimaryDark));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

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
        schoolMode = getActivity().findViewById(R.id.schoolModeCheckbox);

        hideToolbar.setOnCheckedChangeListener(Utils.getCbListener());
        hideTabBar.setOnCheckedChangeListener(Utils.getCbListener());
        schoolMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final ArrayAdapter<String> tabAdp;
                if (isChecked){
                    buttonView.setText(R.string.on);
                    String[] tabList = {getString(R.string.terms), getString(R.string.courses), getString(R.string.assessments), getString(R.string.tasks), getString(R.string.reminders)};
                    tabAdp = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_item, tabList);
                }
                else{
                    buttonView.setText(R.string.off);
                    String[] tabList = {getString(R.string.tasks), getString(R.string.reminders)};
                    tabAdp = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_item, tabList);
                }

                tabAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mDefTabSpinner.setAdapter(tabAdp);
            }
        });
    }

    private void initNotificationSettings() {
        notificationColor = getActivity().findViewById(R.id.notificationColor);
        setColor(PreferenceSingleton.getLedColorId());
        notificationColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ColorPickerDialog.newBuilder().setColor(notificationColor.getBackgroundTintList().getDefaultColor()).show(getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mReminderTypeSpinner = (Spinner) getActivity().findViewById(R.id.reminderTypeDropDown);
        final ArrayList<SpinnerPojo> typeList = new ArrayList();
        typeList.add(new SpinnerPojo(Constants.NotifyTypes.NORMAL, getString(R.string.normal)));
        typeList.add(new SpinnerPojo(Constants.NotifyTypes.ALARM, getString(R.string.alarm)));
        final ArrayAdapter<SpinnerPojo> typeAdp = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, typeList);
        typeAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mReminderTypeSpinner.setAdapter(typeAdp);

        vibratePattern = getActivity().findViewById(R.id.vibratePattern);
        testBtn = getActivity().findViewById(R.id.notifBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bundle b = new Bundle();
                    b.putInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE,mReminderTypeSpinner.getSelectedItemPosition());
                    b.putInt(Constants.SharedPreferenceKeys.LED_COLOR, notificationColor.getBackgroundTintList().getDefaultColor());
                    b.putLongArray(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN,getLongArray());
                    NotifyService.testNotification(getActivity(),b);
                }catch (Exception e){
                    if (e instanceof CustomException) {
                        Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public void setColor(int color) {
        notificationColor.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void initPreferences() {
        //init query params
        daysInput.setText(Integer.toString(sharedpreferences.getInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, 7)));

        //init default tab
        mDefTabSpinner.setSelection(sharedpreferences.getInt(Constants.SharedPreferenceKeys.DEFAULT_TAB, 0));

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
        hideTabBar.setChecked(sharedpreferences.getBoolean(Constants.SharedPreferenceKeys.HIDE_TABBAR, false));
        schoolMode.setChecked(sharedpreferences.getBoolean(Constants.SharedPreferenceKeys.SCHOOL_MODE, true));
        setColor(sharedpreferences.getInt(Constants.SharedPreferenceKeys.LED_COLOR, Color.BLUE));
        mReminderTypeSpinner.setSelection(sharedpreferences.getInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE, 0));

        long[] pattern = gson.fromJson(sharedpreferences.getString(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN, null), PreferenceSingleton.getVibratePattern().getClass());
        String patternString = "";
        for (long ms : pattern) {
            patternString += Long.toString(ms) + ":";
        }
        vibratePattern.setText(patternString.substring(0, patternString.length() - 1));
    }

    public void save() throws Exception {
        try {
            int numDays = Integer.parseInt(daysInput.getText().toString());
            if (numDays > 365 || numDays < -365) {
                throw new CustomException(getString(R.string.invalidDays));
            }
            long[] pattern = getLongArray();
            SpinnerPojo themeItem = (SpinnerPojo) mThemeSpinner.getSelectedItem();
            SpinnerPojo nightModeItem = (SpinnerPojo) mNightModeSpinner.getSelectedItem();
            SpinnerPojo reminderTypeItem = (SpinnerPojo) mReminderTypeSpinner.getSelectedItem();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(Constants.SharedPreferenceKeys.NUM_QUERY_DAYS, numDays);
            editor.putInt(Constants.SharedPreferenceKeys.DEFAULT_TAB, mDefTabSpinner.getSelectedItemPosition());
            editor.putInt(Constants.SharedPreferenceKeys.THEME, themeItem.getId());
            editor.putInt(Constants.SharedPreferenceKeys.NIGHT_MODE, nightModeItem.getId());
            editor.putBoolean(Constants.SharedPreferenceKeys.HIDE_TOOLBAR, hideToolbar.isChecked());
            editor.putBoolean(Constants.SharedPreferenceKeys.HIDE_TABBAR, hideTabBar.isChecked());
            editor.putBoolean(Constants.SharedPreferenceKeys.SCHOOL_MODE, schoolMode.isChecked());
            editor.putInt(Constants.SharedPreferenceKeys.LED_COLOR, notificationColor.getBackgroundTintList().getDefaultColor());
            editor.putInt(Constants.SharedPreferenceKeys.NOTIFICATION_TYPE, reminderTypeItem.getId());

            PreferenceSingleton.setVibratePattern(pattern);
            editor.putString(Constants.SharedPreferenceKeys.NOTIFICATION_VIBRATE_PATTERN, gson.toJson(PreferenceSingleton.getVibratePattern()));
            editor.apply();
            PreferenceSingleton.setNumQueryDays(numDays);
            PreferenceSingleton.setHomeDefTabIndex(mDefTabSpinner.getSelectedItemPosition());
            PreferenceSingleton.setThemeId(themeItem.getId());
            PreferenceSingleton.setNightModeId(nightModeItem.getId());
            PreferenceSingleton.setHideTabBar(hideTabBar.isChecked());
            PreferenceSingleton.setHideToolbar(hideToolbar.isChecked());
            PreferenceSingleton.setSchoolMode(schoolMode.isChecked());
            PreferenceSingleton.setLedColorId(notificationColor.getBackgroundTintList().getDefaultColor());
            PreferenceSingleton.setDefaultNotifyType(reminderTypeItem.getId());
            Snackbar.make(mCoordinatorLayout, getString(R.string.saved), Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            if (e instanceof CustomException) {
                Snackbar.make(mCoordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(mCoordinatorLayout, R.string.preferenceSaveFailed, Snackbar.LENGTH_LONG).show();
            }
            throw e;
        }
    }

    private long[] getLongArray() throws Exception{
        String[] longs = vibratePattern.getText().toString().split(":");
        if (longs.length > 10) {
            throw new CustomException(getString(R.string.error_vibrate_pattern_length));
        }
        long[] pattern = new long[longs.length];
        for (int i = 0; i < longs.length; i++) {
            pattern[i] = Long.parseLong(longs[i]);
            if (pattern[i] > 3000) {
                throw new CustomException(getString(R.string.error_vibrate_pattern_long));
            }
        }
        return pattern;
    }
}

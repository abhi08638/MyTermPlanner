package com.proj.abhi.mytermplanner.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Abhi on 2/8/2018.
 */

public class PreferenceSingleton {
    private static boolean init=false;
    private static int themeId;
    private static int nightModeId;
    private static int ledColorId;
    private static boolean wasNightModeChanged;
    private static Intent pageIntent;
    private static boolean hideToolbar,hideTabBar;

    public static int getThemeId() {
        return themeId;
    }

    public static void setThemeId(int themeId) {
        PreferenceSingleton.themeId = themeId;
    }

    public static boolean isInit() {
        return init;
    }

    public static void setInit(boolean init) {
        PreferenceSingleton.init = init;
    }

    public static boolean isHideToolbar() {
        return hideToolbar;
    }

    public static void setHideToolbar(boolean hideToolbar) {
        PreferenceSingleton.hideToolbar = hideToolbar;
    }

    public static boolean isHideTabBar() {
        return hideTabBar;
    }

    public static void setHideTabBar(boolean hideTabBar) {
        PreferenceSingleton.hideTabBar = hideTabBar;
    }

    public static int getNightModeId() {
        return nightModeId;
    }

    public static void setNightModeId(int nightModeId) {
        PreferenceSingleton.nightModeId = nightModeId;
    }

    public static boolean wasNightModeChanged() {
        return wasNightModeChanged;
    }

    public static void setWasNightModeChanged(boolean wasNightModeChanged) {
        PreferenceSingleton.wasNightModeChanged = wasNightModeChanged;
    }

    public static Intent getPageIntent() {
        return pageIntent;
    }

    public static void setPageIntent(Intent pageIntent) {
        PreferenceSingleton.pageIntent = pageIntent;
    }

    public static int getLedColorId() {
        return ledColorId;
    }

    public static void setLedColorId(int ledColorId) {
        PreferenceSingleton.ledColorId = ledColorId;
    }
}

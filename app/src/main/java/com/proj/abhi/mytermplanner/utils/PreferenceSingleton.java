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
}

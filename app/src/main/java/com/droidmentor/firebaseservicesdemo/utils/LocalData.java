package com.droidmentor.firebaseservicesdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Jaison.
 */
public class LocalData {

    public static final String TEMP_EMAIL_ID = "TEMP_EMAIl_ID";
    public static final String USER_ID = "USER_ID";
    private static final String APP_SHARED_PREFS = "FireBaseDemoPref";
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    public LocalData(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public String getString(String keyName) {
        return appSharedPrefs.getString(keyName, "");
    }

    public void setString(String keyName, String value) {
        prefsEditor.putString(keyName, value);
        prefsEditor.commit();
    }

    public void resetAll() {
        prefsEditor.clear();
        prefsEditor.commit();
    }
}

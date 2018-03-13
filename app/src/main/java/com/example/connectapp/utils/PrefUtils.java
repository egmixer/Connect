package com.example.connectapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;


public class PrefUtils {
    private final static String TAG = PrefUtils.class.getSimpleName();
    private final static String PREFERENCES_NAME = "com.example.connectapp.utils.PREFERENCES";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static PrefUtils instance;

    private PrefUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public synchronized static PrefUtils with(Context context) {
        if (instance == null) {
            instance = new PrefUtils(context);
        }
        return instance;
    }

    public static boolean hasKey(String key) {
        return sharedPreferences.contains(key);
    }

    public static void saveObjectToPref(Object object, Class<?> targetClass, String key) {
        String json = new Gson().toJson(object, targetClass);
        editor.putString(key, json).apply();
    }

    public static Object getObjectFromSharedPreferences(Class<?> targetClass, String key) {
        String json = sharedPreferences.getString(key, "");
        if (!json.isEmpty()) {
            return new Gson().fromJson(json, targetClass);
        }
        return null;
    }

    public static void saveStringToPref(String string, String key) {
        editor.putString(key, string).apply();
    }

    public static String getStringFromPref(String key) {
        return sharedPreferences.getString(key, null);
    }

    public static void saveIntToPref(int i, String key) {
        editor.putInt(key, i).apply();
    }

    public static int getIntFromPref(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public static void saveBoolIntoPref(boolean bool, String key) {
        editor.putBoolean(key, bool).apply();
    }

    public static boolean getBoolFromPref(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public static void logOut() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "logOut: ");
    }

}

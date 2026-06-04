package com.example.vidygo.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Persiste et applique le mode d'apparence de l'application.
 */
public final class ThemePreferenceManager {

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private static final String UI_PREFS = "vidygo_ui_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    private ThemePreferenceManager() {
    }

    public static void saveThemeMode(Context context, String mode) {
        SharedPreferences prefs = context.getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME_MODE, normalizeMode(mode)).apply();
    }

    public static String loadThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE);
        return normalizeMode(prefs.getString(KEY_THEME_MODE, MODE_SYSTEM));
    }

    public static void applySavedTheme(Context context) {
        applyTheme(loadThemeMode(context));
    }

    public static void applyTheme(String mode) {
        switch (normalizeMode(mode)) {
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private static String normalizeMode(String mode) {
        if (MODE_LIGHT.equals(mode)) {
            return MODE_LIGHT;
        }
        if (MODE_DARK.equals(mode)) {
            return MODE_DARK;
        }
        return MODE_SYSTEM;
    }
}


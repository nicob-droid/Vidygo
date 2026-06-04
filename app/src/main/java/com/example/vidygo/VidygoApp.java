package com.example.vidygo;

import android.app.Application;

import com.example.vidygo.util.ThemePreferenceManager;

/**
 * Point d'entrée global pour appliquer la configuration UI persistée.
 */
public class VidygoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemePreferenceManager.applySavedTheme(this);
    }
}


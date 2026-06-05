package io.github.nicobdroid.vidygo;

import android.app.Application;

import io.github.nicobdroid.vidygo.util.ThemePreferenceManager;
import com.google.android.gms.ads.MobileAds;

/**
 * Point d'entrée global pour appliquer la configuration UI persistée.
 */
public class VidygoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemePreferenceManager.applySavedTheme(this);
        MobileAds.initialize(this, initializationStatus -> {
            // L'initialisation est asynchrone; le chargement des bannières peut suivre.
        });
    }
}


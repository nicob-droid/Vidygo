package io.github.nicobdroid.vidygo;

import android.app.Application;

import io.github.nicobdroid.vidygo.util.ThemePreferenceManager;
import io.github.nicobdroid.vidygo.util.Logger;
import com.google.android.gms.ads.MobileAds;

/**
 * Point d'entrée global pour appliquer la configuration UI persistée.
 */
public class VidygoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ThemePreferenceManager.applySavedTheme(this);
        } catch (Throwable t) {
            // Evite un crash de lancement si la preference de theme est invalide/corrompue.
            Logger.e("Application du theme impossible au demarrage", t);
        }
        try {
            MobileAds.initialize(this, initializationStatus -> {
                // L'initialisation est asynchrone; le chargement des bannières peut suivre.
            });
        } catch (Throwable t) {
            // Ne pas faire tomber l'application si le SDK pub n'est pas disponible côté appareil.
            Logger.e("Initialisation MobileAds impossible", t);
        }
    }
}


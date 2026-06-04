package com.example.vidygo.util;

import android.util.Log;

/**
 * Classe utilitaire pour la journalisation centralisée.
 * Facilite le debugging et le suivi des événements.
 */
public class Logger {

    private static final String TAG = "Vidygo";
    private static final boolean DEBUG = true;

    /**
     * Log un message de débogage
     */
    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    /**
     * Log un message de débogage avec une exception
     */
    public static void d(String message, Throwable throwable) {
        if (DEBUG) {
            Log.d(TAG, message, throwable);
        }
    }

    /**
     * Log un message d'information
     */
    public static void i(String message) {
        Log.i(TAG, message);
    }

    /**
     * Log un message d'avertissement
     */
    public static void w(String message) {
        Log.w(TAG, message);
    }

    /**
     * Log un message d'avertissement avec une exception
     */
    public static void w(String message, Throwable throwable) {
        Log.w(TAG, message, throwable);
    }

    /**
     * Log un message d'erreur
     */
    public static void e(String message) {
        Log.e(TAG, message);
    }

    /**
     * Log un message d'erreur avec une exception
     */
    public static void e(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
    }
}


package com.example.vidygo.config;

/**
 * Classe de configuration centralisant toutes les constantes de l'application.
 */
public class AppConfig {

    // Délais et timeouts
    public static final int CONNECT_TIMEOUT_MS = 10000;
    public static final int READ_TIMEOUT_MS = 10000;

    // Taille des images de miniatures
    public static final int THUMBNAIL_WIDTH = 320;
    public static final int THUMBNAIL_HEIGHT = 180;

    // Codes de résultat d'activité
    public static final int REQUEST_CODE_ADD_VIDEO = 1001;

    // Préférences
    public static final String PREF_FILE_NAME = "vidygo_preferences";
    public static final String PREF_KEY_VIDEOS = "videos_list";
    public static final String PREF_KEY_THEME = "theme_preference";

    // Clés Intent
    public static final String INTENT_KEY_VIDEO = "video";
    public static final String INTENT_KEY_VIDEO_ID = "video_id";

    // Formats
    public static final String URL_FORMAT_YOUTUBE_SHORT = "https://youtu.be/%s";
    public static final String URL_FORMAT_YOUTUBE_FULL = "https://www.youtube.com/watch?v=%s";

    // Logging
    public static final String LOG_TAG = "Vidygo";
    public static final boolean DEBUG_MODE = true;
}


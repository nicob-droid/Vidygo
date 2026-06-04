package com.example.vidygo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.vidygo.util.ThemePreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Écran Paramètres : apparence, informations légales et à propos.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final String KEY_THEME_MODE = "pref_theme_mode";
        private static final String KEY_TERMS = "pref_terms";
        private static final String KEY_PRIVACY = "pref_privacy";
        private static final String KEY_ABOUT = "pref_about";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            setupThemePreference();
            setupLegalAndAboutPreferences();
        }

        private void setupThemePreference() {
            ListPreference themePreference = findPreference(KEY_THEME_MODE);
            if (themePreference == null) {
                return;
            }

            String currentMode = ThemePreferenceManager.loadThemeMode(requireContext());
            themePreference.setValue(currentMode);
            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String selectedMode = String.valueOf(newValue);
                ThemePreferenceManager.saveThemeMode(requireContext(), selectedMode);
                ThemePreferenceManager.applyTheme(selectedMode);
                return true;
            });
        }

        private void setupLegalAndAboutPreferences() {
            Preference terms = findPreference(KEY_TERMS);
            if (terms != null) {
                terms.setOnPreferenceClickListener(preference -> {
                    showMessageDialog(R.string.settings_terms, R.string.settings_terms_content);
                    return true;
                });
            }

            Preference privacy = findPreference(KEY_PRIVACY);
            if (privacy != null) {
                privacy.setOnPreferenceClickListener(preference -> {
                    showMessageDialog(R.string.settings_privacy, R.string.settings_privacy_content);
                    return true;
                });
            }

            final String aboutText = getString(
                    R.string.settings_about_content,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
            );
            Preference about = findPreference(KEY_ABOUT);
            if (about != null) {
                about.setSummary(aboutText);
                about.setOnPreferenceClickListener(preference -> {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle(R.string.settings_about)
                            .setMessage(aboutText)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return true;
                });
            }
        }

        private void showMessageDialog(int titleResId, int messageResId) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(titleResId)
                    .setMessage(messageResId)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}


package io.github.nicobdroid.vidygo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import io.github.nicobdroid.vidygo.util.ThemePreferenceManager;
import io.github.nicobdroid.vidygo.util.VideoPreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Écran Paramètres : apparence, informations légales et à propos.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(ContextCompat.getColor(this, R.color.gray_dark)),
                SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        );
        setContentView(R.layout.activity_settings);
        applyEdgeToEdgeInsets();

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

    private void applyEdgeToEdgeInsets() {
        ViewGroup content = findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }
        View root = content.getChildAt(0);
        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialLeft + bars.left,
                    initialTop + bars.top,
                    initialRight + bars.right,
                    initialBottom + bars.bottom
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final String KEY_THEME_MODE = "pref_theme_mode";
        private static final String KEY_TERMS = "pref_terms";
        private static final String KEY_PRIVACY = "pref_privacy";
        private static final String KEY_ABOUT = "pref_about";
        private static final String KEY_RESET_APP = "pref_reset_app";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            setupThemePreference();
            setupLegalAndAboutPreferences();
            setupResetPreference();
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
                    showMessageDialog(R.string.settings_about, aboutText);
                    return true;
                });
            }
        }

        private void showMessageDialog(int titleResId, int messageResId) {
            showMessageDialog(titleResId, getString(messageResId));
        }

        private void showMessageDialog(int titleResId, CharSequence message) {
            androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(titleResId)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();

            dialog.setOnShowListener(unused -> {
                int buttonColor = MaterialColors.getColor(
                        requireContext(),
                        androidx.appcompat.R.attr.actionMenuTextColor,
                        0
                );
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(buttonColor);
            });
            dialog.show();
        }

        private void setupResetPreference() {
            Preference reset = findPreference(KEY_RESET_APP);
            if (reset == null) {
                return;
            }
            reset.setOnPreferenceClickListener(preference -> {
                showResetConfirmationDialog();
                return true;
            });
        }

        private void showResetConfirmationDialog() {
            androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.settings_reset_dialog_title)
                    .setMessage(R.string.settings_reset_dialog_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.settings_reset_confirm, (d, which) -> resetApplicationData())
                    .create();

            dialog.setOnShowListener(unused -> {
                int neutralColor = MaterialColors.getColor(
                        requireContext(),
                        androidx.appcompat.R.attr.actionMenuTextColor,
                        0
                );
                int destructiveColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark);
                if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(neutralColor);
                }
                if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null) {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(destructiveColor);
                }
            });
            dialog.show();
        }

        private void resetApplicationData() {
            Context context = requireContext();
            new VideoPreferenceManager(context).clearAll();
            context.getSharedPreferences("vidygo_ui_prefs", Context.MODE_PRIVATE).edit().clear().apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
            ThemePreferenceManager.applyTheme(ThemePreferenceManager.MODE_SYSTEM);

            Toast.makeText(context, R.string.settings_reset_done, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finishAffinity();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}


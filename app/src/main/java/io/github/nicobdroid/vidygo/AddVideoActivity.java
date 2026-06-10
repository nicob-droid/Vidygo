package io.github.nicobdroid.vidygo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.nicobdroid.vidygo.model.Video;
import io.github.nicobdroid.vidygo.util.Logger;
import io.github.nicobdroid.vidygo.util.VideoPreferenceManager;
import io.github.nicobdroid.vidygo.util.YouTubeMetadataUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activité pour ajouter une nouvelle vidéo YouTube.
 */
public class AddVideoActivity extends AppCompatActivity {

    public static final String EXTRA_PREFILL_PLAYLIST = "prefill_playlist_name";
    private static final String STATE_FETCHED_CHANNEL_AVATAR_URL = "state_fetched_channel_avatar_url";
    private static final String STATE_LAUNCHED_FROM_SHARE_INTENT = "state_launched_from_share_intent";

    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
            "(https?://(?:www\\.)?(?:youtube\\.com|youtu\\.be)/\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    private TextInputEditText urlInput;
    private TextInputEditText titleInput;
    private TextInputEditText channelInput;
    private TextInputEditText playlistInput;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;
    private MaterialToolbar toolbar;
    private MaterialTextView metadataStatus;
    private CircularProgressIndicator metadataLoadingIndicator;
    private View metadataLoadingOverlay;
    private VideoPreferenceManager videoPreferenceManager;
    private final ExecutorService metadataExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger metadataRequestId = new AtomicInteger(0);
    private Runnable pendingMetadataLookup;
    /** Avatar récupéré automatiquement lors du dernier lookup de métadonnées. */
    private String fetchedChannelAvatarUrl = "";
    private boolean launchedFromShareIntent = false;
    private boolean suppressAutoMetadataLookup = false;
    private boolean restoringFromSavedState = false;
    private String restoredUrlSnapshot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        Logger.d("AddVideoActivity créée");
        videoPreferenceManager = new VideoPreferenceManager(this);
        restoringFromSavedState = savedInstanceState != null;
        if (restoringFromSavedState) {
            // Empêche les TextWatcher de relancer un fetch pendant la restauration automatique.
            suppressAutoMetadataLookup = true;
            restoreState(savedInstanceState);
        }

        // Initialiser les vues
        initializeViews();
        prefillPlaylistFromIntent();

        // Configurer les actions
        setupActions();
        if (savedInstanceState == null) {
            handleIncomingShareIntent(getIntent());
        }
    }

    private void restoreState(Bundle savedInstanceState) {
        fetchedChannelAvatarUrl = savedInstanceState.getString(STATE_FETCHED_CHANNEL_AVATAR_URL, "");
        launchedFromShareIntent = savedInstanceState.getBoolean(STATE_LAUNCHED_FROM_SHARE_INTENT, false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (restoringFromSavedState) {
            // Ne relance pas le fetch sur l'URL restaurée telle quelle.
            restoredUrlSnapshot = getText(urlInput);
            restoringFromSavedState = false;
            suppressAutoMetadataLookup = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FETCHED_CHANNEL_AVATAR_URL, fetchedChannelAvatarUrl);
        outState.putBoolean(STATE_LAUNCHED_FROM_SHARE_INTENT, launchedFromShareIntent);
    }

    private void prefillPlaylistFromIntent() {
        String prefill = getIntent().getStringExtra(EXTRA_PREFILL_PLAYLIST);
        if (!TextUtils.isEmpty(prefill) && playlistInput != null) {
            playlistInput.setText(prefill.trim());
        }
    }

    /**
     * Initialise les références aux vues du layout.
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        urlInput = findViewById(R.id.video_url_input);
        titleInput = findViewById(R.id.video_title_input);
        channelInput = findViewById(R.id.video_channel_input);
        playlistInput = findViewById(R.id.video_playlist_input);
        metadataStatus = findViewById(R.id.metadata_status);
        metadataLoadingIndicator = findViewById(R.id.metadata_loading_indicator);
        metadataLoadingOverlay = findViewById(R.id.metadata_loading_overlay);
        saveButton = findViewById(R.id.btn_save);
        cancelButton = findViewById(R.id.btn_cancel);

        // Configurer le toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Configure les listeners des boutons.
     */
    private void setupActions() {
        saveButton.setOnClickListener(v -> saveVideo());
        cancelButton.setOnClickListener(v -> finish());
        toolbar.setNavigationOnClickListener(v -> finish());
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressAutoMetadataLookup) return;

                String currentUrl = s == null ? "" : s.toString().trim();
                if (restoredUrlSnapshot != null && restoredUrlSnapshot.equals(currentUrl)) {
                    return;
                }
                restoredUrlSnapshot = null;
                scheduleMetadataLookup();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
    }

    /**
     * Valide et sauvegarde la vidéo.
     */
    private void saveVideo() {
        String url = getText(urlInput);
        String title = getText(titleInput);
        String channel = getText(channelInput);
        String playlist = getText(playlistInput);

        if (url.isEmpty()) {
            urlInput.setError(getString(R.string.url_required));
            return;
        }

        if (isVideoAlreadySaved(url)) {
            Toast.makeText(this, R.string.video_already_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(channel)) {
            metadataStatus.setText(R.string.metadata_loading);
            fetchMetadata(url, new MetadataCallback() {
                @Override
                public void onSuccess(String fetchedTitle, String fetchedChannel, String avatarUrl) {
                    if (TextUtils.isEmpty(fetchedTitle) || TextUtils.isEmpty(fetchedChannel)) {
                        onError(getString(R.string.metadata_failed));
                        return;
                    }
                    titleInput.setText(fetchedTitle);
                    channelInput.setText(fetchedChannel);
                    persistVideo(url, fetchedTitle, fetchedChannel, playlist, avatarUrl);
                }

                @Override
                public void onError(String message) {
                    metadataStatus.setText(message);
                    Toast.makeText(AddVideoActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        persistVideo(url, title, channel, playlist, fetchedChannelAvatarUrl);
    }

    private boolean isVideoAlreadySaved(String url) {
        List<Video> savedVideos = videoPreferenceManager.getVideos();
        for (Video video : savedVideos) {
            if (video.getVideoUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private String getText(TextInputEditText input) {
        return input == null || input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void persistVideo(String url, String title, String channel, String playlist, String channelAvatarUrl) {
        persistVideo(url, title, channel, playlist, channelAvatarUrl, false);
    }

    private void persistVideo(String url, String title, String channel, String playlist,
                              String channelAvatarUrl, boolean openMainAfterSave) {
        String videoId = UUID.randomUUID().toString();
        Video newVideo = new Video(videoId, title, channel, playlist, "", url, channelAvatarUrl);

        Logger.d("Vidéo créée : " + title + " / " + channel);
        videoPreferenceManager.saveVideo(newVideo);
        if (launchedFromShareIntent) {
            Toast.makeText(this, R.string.video_added_from_share, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.video_added, title), Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);

        if (openMainAfterSave) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void handleIncomingShareIntent(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) {
            return;
        }

        String type = intent.getType();
        if (type == null || !type.startsWith("text/")) {
            return;
        }

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (TextUtils.isEmpty(sharedText)) {
            sharedText = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        }

        String youtubeUrl = extractFirstYoutubeUrl(sharedText);
        if (TextUtils.isEmpty(youtubeUrl)) {
            Toast.makeText(this, R.string.share_no_youtube_link, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isVideoAlreadySaved(youtubeUrl)) {
            Toast.makeText(this, R.string.video_already_exists, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        launchedFromShareIntent = true;
        suppressAutoMetadataLookup = true;
        urlInput.setText(youtubeUrl);
        suppressAutoMetadataLookup = false;
        metadataStatus.setText(R.string.metadata_loading);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

        fetchMetadata(youtubeUrl, new MetadataCallback() {
            @Override
            public void onSuccess(String fetchedTitle, String fetchedChannel, String avatarUrl) {
                String finalTitle = TextUtils.isEmpty(fetchedTitle)
                        ? getString(R.string.shared_video_default_title)
                        : fetchedTitle;
                String finalChannel = TextUtils.isEmpty(fetchedChannel)
                        ? getString(R.string.unknown_channel)
                        : fetchedChannel;
                persistVideo(youtubeUrl, finalTitle, finalChannel, "", avatarUrl, true);
            }

            @Override
            public void onError(String message) {
                // Même si les métadonnées échouent, on importe la vidéo partagée.
                persistVideo(
                        youtubeUrl,
                        getString(R.string.shared_video_default_title),
                        getString(R.string.unknown_channel),
                        "",
                        "",
                        true
                );
            }
        });

        // Sécurité: si l'import n'aboutit pas (callback perdu), on rend l'écran utilisable.
        mainHandler.postDelayed(() -> {
            if (!isFinishing() && saveButton != null && !saveButton.isEnabled()) {
                saveButton.setEnabled(true);
                cancelButton.setEnabled(true);
                metadataStatus.setText(R.string.metadata_retry);
            }
        }, 5000);
    }

    private String extractFirstYoutubeUrl(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }

        Matcher matcher = YOUTUBE_URL_PATTERN.matcher(input);
        if (!matcher.find()) {
            return "";
        }

        String url = matcher.group(1);
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return url.replaceAll("[),.;!?]+$", "").trim();
    }

    private void scheduleMetadataLookup() {
        if (pendingMetadataLookup != null) {
            mainHandler.removeCallbacks(pendingMetadataLookup);
        }

        String url = getText(urlInput);
        if (TextUtils.isEmpty(url)) {
            metadataStatus.setText("");
            titleInput.setText("");
            channelInput.setText("");
            fetchedChannelAvatarUrl = "";
            setMetadataLoading(false);
            return;
        }

        metadataStatus.setText(R.string.metadata_loading);
        pendingMetadataLookup = () -> fetchMetadata(url, new MetadataCallback() {
            @Override
            public void onSuccess(String fetchedTitle, String fetchedChannel, String avatarUrl) {
                fetchedChannelAvatarUrl = avatarUrl;
                if (!TextUtils.isEmpty(fetchedTitle)) {
                    titleInput.setText(fetchedTitle);
                }
                if (!TextUtils.isEmpty(fetchedChannel)) {
                    channelInput.setText(fetchedChannel);
                }
                if (!isFinishing()) {
                    metadataStatus.setText(R.string.metadata_loaded);
                }
            }

            @Override
            public void onError(String message) {
                if (!isFinishing()) {
                    metadataStatus.setText(message);
                }
            }
        });
        mainHandler.postDelayed(pendingMetadataLookup, 650);
    }

    private void fetchMetadata(String videoUrl, MetadataCallback callback) {
        final int requestId = metadataRequestId.incrementAndGet();
        setMetadataLoading(true);
        metadataExecutor.execute(() -> {
            try {
                Metadata metadata = loadYouTubeMetadata(videoUrl);

                mainHandler.post(() -> {
                    if (requestId != metadataRequestId.get() || isFinishing()) return;
                    setMetadataLoading(false);
                    callback.onSuccess(metadata.title, metadata.channel, metadata.channelAvatarUrl);
                });
            } catch (Exception e) {
                Logger.w("Impossible de récupérer les métadonnées de la vidéo", e);
                mainHandler.post(() -> {
                    if (requestId != metadataRequestId.get() || isFinishing()) return;
                    setMetadataLoading(false);
                    callback.onError(getString(R.string.metadata_failed));
                });
            }
        });
    }

    private void setMetadataLoading(boolean isLoading) {
        if (metadataLoadingOverlay != null) {
            metadataLoadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (metadataLoadingIndicator != null) {
            metadataLoadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private Metadata loadYouTubeMetadata(String videoUrl) throws Exception {
        YouTubeMetadataUtil.VideoMetadata meta = YouTubeMetadataUtil.fetchVideoMetadata(videoUrl);
        return new Metadata(meta.title, meta.channelName, meta.channelAvatarUrl);
    }

    private String unescapeHtml(String input) {
        return input.replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'");
    }

    private String normalizeUrl(String url) {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingMetadataLookup != null) mainHandler.removeCallbacks(pendingMetadataLookup);
        setMetadataLoading(false);
        metadataExecutor.shutdownNow();
    }

    private interface MetadataCallback {
        void onSuccess(String title, String channel, String channelAvatarUrl);
        void onError(String message);
    }

    private static class Metadata {
        final String title;
        final String channel;
        final String channelAvatarUrl;

        Metadata(String title, String channel, String channelAvatarUrl) {
            this.title = title;
            this.channel = channel;
            this.channelAvatarUrl = channelAvatarUrl != null ? channelAvatarUrl : "";
        }
    }
}

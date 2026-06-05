package io.github.nicobdroid.vidygo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.nicobdroid.vidygo.model.Video;
import io.github.nicobdroid.vidygo.util.Logger;
import io.github.nicobdroid.vidygo.util.VideoPreferenceManager;
import io.github.nicobdroid.vidygo.util.YouTubeMetadataUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

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
    private VideoPreferenceManager videoPreferenceManager;
    private final ExecutorService metadataExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger metadataRequestId = new AtomicInteger(0);
    private Runnable pendingMetadataLookup;
    /** Avatar récupéré automatiquement lors du dernier lookup de métadonnées. */
    private String fetchedChannelAvatarUrl = "";
    private boolean launchedFromShareIntent = false;
    private boolean suppressAutoMetadataLookup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        Logger.d("AddVideoActivity créée");
        videoPreferenceManager = new VideoPreferenceManager(this);

        // Initialiser les vues
        initializeViews();
        prefillPlaylistFromIntent();

        // Configurer les actions
        setupActions();
        handleIncomingShareIntent(getIntent());
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
            urlInput.setError("Veuillez entrer l'URL de la vidéo");
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
            Toast.makeText(this, "Vidéo ajoutée : " + title, Toast.LENGTH_SHORT).show();
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
        metadataExecutor.execute(() -> {
            try {
                Metadata metadata = loadYouTubeMetadata(videoUrl);

                mainHandler.post(() -> {
                    if (requestId != metadataRequestId.get() || isFinishing()) return;
                    callback.onSuccess(metadata.title, metadata.channel, metadata.channelAvatarUrl);
                });
            } catch (Exception e) {
                Logger.w("Impossible de récupérer les métadonnées de la vidéo", e);
                mainHandler.post(() -> {
                    if (requestId != metadataRequestId.get() || isFinishing()) return;
                    callback.onError(getString(R.string.metadata_failed));
                });
            }
        });
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


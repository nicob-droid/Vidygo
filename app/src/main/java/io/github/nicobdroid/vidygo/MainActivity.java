package io.github.nicobdroid.vidygo;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.net.Uri;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.nicobdroid.vidygo.adapter.ChannelAdapter;
import io.github.nicobdroid.vidygo.adapter.SectionedVideoAdapter;
import io.github.nicobdroid.vidygo.adapter.VideoAdapter;
import io.github.nicobdroid.vidygo.model.Video;
import io.github.nicobdroid.vidygo.util.Logger;
import io.github.nicobdroid.vidygo.util.VideoPreferenceManager;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activité principale de l'application Vidygo.
 * Affiche la liste des vidéos YouTube préférées sauvegardées.
 */
public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoActionListener {

    private static final String UI_PREFS = "vidygo_ui_prefs";
    private static final String KEY_ALL_SORT_MODE = "all_sort_mode";
    private static final String STATE_CURRENT_MODE = "state_current_mode";
    private static final String TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final Pattern SHORTS_PATTERN = Pattern.compile("/shorts/([a-zA-Z0-9_-]{11})");

    private RecyclerView videosRecyclerView;
    private VideoAdapter videoAdapter;
    private SectionedVideoAdapter sectionedVideoAdapter;
    private ChannelAdapter channelAdapter;
    private ChannelAdapter playlistAdapter;
    private RecyclerView.Adapter<?> currentAdapter;
    private List<Video> videoList;
    private LinearLayout emptyState;
    private android.widget.TextView emptyStateTitle;
    private android.widget.TextView emptyStateDescription;
    private FloatingActionButton fabAddVideo;
    private FloatingActionButton fabAddPlaylist;
    private FrameLayout adContainerHome;
    private AdView adViewHome;
    private VideoPreferenceManager videoPreferenceManager;
    private String currentMode = "all";
    private Chip chipAll;
    private Chip chipChannels;
    private Chip chipPlaylists;
    private GridLayoutManager gridLayoutManager;
    private SortMode currentSortMode = SortMode.DATE_DESC;

    private enum SortMode {
        DATE_DESC,
        DATE_ASC,
        ALPHA_ASC,
        ALPHA_DESC,
        CHANNEL_ASC,
        CHANNEL_DESC
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(ContextCompat.getColor(this, R.color.gray_dark)),
                SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        );
        setContentView(R.layout.activity_main);
        applyEdgeToEdgeInsets();
        videoPreferenceManager = new VideoPreferenceManager(this);
        currentSortMode = loadSortMode();
        if (savedInstanceState != null) {
            currentMode = savedInstanceState.getString(STATE_CURRENT_MODE, "all");
        }

        // Initialiser les vues
        initializeViews();
        setupFilters();

        // Initialiser la liste des vidéos
        initializeVideoList();

        // Configurer le RecyclerView
        setupRecyclerView();

        // Configurer les actions
        setupActions();
        setupAdBanner();

        // Afficher l'état vide si la liste est vide
        updateEmptyState();
    }

    /**
     * Initialise les références aux vues du layout.
     */
    private void initializeViews() {
        videosRecyclerView = findViewById(R.id.videos_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        emptyStateTitle = findViewById(R.id.empty_state_title);
        emptyStateDescription = findViewById(R.id.empty_state_description);
        fabAddVideo = findViewById(R.id.fab_add_video);
        fabAddPlaylist = findViewById(R.id.fab_add_playlist);
        adContainerHome = findViewById(R.id.ad_container_home);
        chipAll = findViewById(R.id.chip_all);
        chipChannels = findViewById(R.id.chip_channels);
        chipPlaylists = findViewById(R.id.chip_playlists);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    private void setupAdBanner() {
        if (adContainerHome == null) {
            return;
        }

        try {
            String adUnitId = resolveBannerAdUnitId();

            adViewHome = new AdView(this);
            adViewHome.setAdSize(AdSize.BANNER);
            adViewHome.setAdUnitId(adUnitId);
            adViewHome.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Logger.i("Banniere AdMob chargee avec succes");
                    adContainerHome.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    Logger.w(
                            "Echec chargement banniere AdMob code=" + adError.getCode()
                                    + " domaine=" + adError.getDomain()
                                    + " message=" + adError.getMessage()
                    );
                    adContainerHome.setVisibility(View.GONE);
                }
            });
            FrameLayout.LayoutParams adLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL
            );
            adViewHome.setLayoutParams(adLayoutParams);
            adContainerHome.removeAllViews();
            adContainerHome.addView(adViewHome);

            adViewHome.loadAd(new AdRequest.Builder().build());
            adContainerHome.setVisibility(View.GONE);
        } catch (Throwable t) {
            Logger.e("Impossible d'initialiser la banniere AdMob sur l'ecran principal", t);
            adViewHome = null;
            adContainerHome.removeAllViews();
            adContainerHome.setVisibility(View.GONE);
        }
    }

    private String resolveBannerAdUnitId() {
        // En debug, forcer une pub de test evite les erreurs "No fill" sur les nouveaux blocs.
        if (BuildConfig.DEBUG) {
            Logger.i("Build debug detecte: utilisation de l'unite de test AdMob");
            return TEST_BANNER_AD_UNIT_ID;
        }

        String configured = sanitizeAdUnitId(BuildConfig.ADMOB_HOME_BANNER_UNIT_ID);
        if (TextUtils.isEmpty(configured)) {
            Logger.w("Aucune unite AdMob configuree: fallback sur l'unite de test");
            return TEST_BANNER_AD_UNIT_ID;
        }
        return configured;
    }

    private String sanitizeAdUnitId(String rawAdUnitId) {
        if (rawAdUnitId == null) {
            return "";
        }
        String cleaned = rawAdUnitId.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> setMode("all"));
        chipChannels.setOnClickListener(v -> setMode("channels"));
        chipPlaylists.setOnClickListener(v -> setMode("playlists"));
        applyModeSelection(currentMode);
    }

    private void applyModeSelection(String mode) {
        chipAll.setChecked("all".equals(mode));
        chipChannels.setChecked("channels".equals(mode));
        chipPlaylists.setChecked("playlists".equals(mode));
    }

    /**
     * Initialise la liste des vidéos avec des exemples de données.
     */
    private void initializeVideoList() {
        videoList = new ArrayList<>(videoPreferenceManager.getVideos());
        sortVideosForAllMode(videoList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adViewHome != null) {
            adViewHome.resume();
        }
        // Recharger la liste au retour de l'écran d'ajout.
        videoList = new ArrayList<>(videoPreferenceManager.getVideos());
        refreshList();
    }

    @Override
    protected void onPause() {
        if (adViewHome != null) {
            adViewHome.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (adViewHome != null) {
            adViewHome.destroy();
        }
        super.onDestroy();
    }

    private void setMode(String mode) {
        currentMode = mode;
        applyModeSelection(mode);
        invalidateOptionsMenu();
        refreshList();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_CURRENT_MODE, currentMode);
        super.onSaveInstanceState(outState);
    }

    private void refreshList() {
        if (videoAdapter == null || channelAdapter == null || playlistAdapter == null) {
            return;
        }

        List<Video> source = new ArrayList<>(videoList);

        if ("channels".equals(currentMode)) {
            channelAdapter.rebuild(source);
            currentAdapter = channelAdapter;
            videosRecyclerView.setLayoutManager(gridLayoutManager);
        } else if ("playlists".equals(currentMode)) {
            playlistAdapter.setExplicitPlaylistNames(videoPreferenceManager.getPlaylistNames());
            playlistAdapter.rebuild(source);
            currentAdapter = playlistAdapter;
            videosRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            sortVideosForAllMode(source);
            videoAdapter.updateVideos(source);
            currentAdapter = videoAdapter;
            videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        videosRecyclerView.setAdapter(currentAdapter);
        updateEmptyState();
    }

    private void sortVideosForAllMode(List<Video> videos) {
        Comparator<Video> comparator;
        switch (currentSortMode) {
            case DATE_ASC:
                comparator = Comparator.comparingLong(Video::getDateAdded);
                break;
            case ALPHA_ASC:
                comparator = (a, b) -> safeTitle(a).compareToIgnoreCase(safeTitle(b));
                break;
            case ALPHA_DESC:
                comparator = (a, b) -> safeTitle(b).compareToIgnoreCase(safeTitle(a));
                break;
            case CHANNEL_ASC:
                comparator = (a, b) -> safeChannel(a).compareToIgnoreCase(safeChannel(b));
                break;
            case CHANNEL_DESC:
                comparator = (a, b) -> safeChannel(b).compareToIgnoreCase(safeChannel(a));
                break;
            case DATE_DESC:
            default:
                comparator = (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded());
                break;
        }
        Collections.sort(videos, comparator);
    }

    private String safeTitle(Video video) {
        String title = video.getTitle();
        return title == null ? "" : title.trim();
    }

    private String safeChannel(Video video) {
        String channel = video.getChannel();
        return channel == null ? "" : channel.trim();
    }

    /**
     * Configure le RecyclerView avec un LinearLayoutManager et l'adaptateur.
     */
    private void setupRecyclerView() {
        gridLayoutManager = new GridLayoutManager(this, 3);
        videoAdapter = new VideoAdapter(videoList, this);
        sectionedVideoAdapter = new SectionedVideoAdapter(videoList, currentMode, this);
        channelAdapter = new ChannelAdapter(videoList, ChannelAdapter.Mode.CHANNELS, channel -> {
            Intent intent = new Intent(this, ChannelVideosActivity.class);
            intent.putExtra(ChannelVideosActivity.EXTRA_CHANNEL_NAME, channel.getName());
            startActivity(intent);
        });
        playlistAdapter = new ChannelAdapter(videoList, ChannelAdapter.Mode.PLAYLISTS, playlist -> {
            Intent intent = new Intent(this, ChannelVideosActivity.class);
            intent.putExtra(ChannelVideosActivity.EXTRA_PLAYLIST_NAME, playlist.getName());
            startActivity(intent);
        }, playlist -> {
            showPlaylistActionsDialog(playlist.getName());
        });
        playlistAdapter.setExplicitPlaylistNames(videoPreferenceManager.getPlaylistNames());
        currentAdapter = videoAdapter;
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerView.setAdapter(currentAdapter);
    }

    /**
     * Configure les listeners des boutons et actions.
     */
    private void setupActions() {
        // Bouton pour ajouter une vidéo
        fabAddVideo.setOnClickListener(v -> {
            Logger.d("Bouton Ajouter vidéo cliqué");
            Intent intent = new Intent(this, AddVideoActivity.class);
            startActivity(intent);
        });

        fabAddPlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
    }

    /**
     * Affiche ou masque l'état vide selon le nombre de vidéos.
     */
    private void updateEmptyState() {
        boolean showEmpty = "all".equals(currentMode)
                ? videoList.isEmpty()
                : (currentAdapter == null || currentAdapter.getItemCount() == 0);
        if (showEmpty) {
            if ("playlists".equals(currentMode)) {
                emptyStateTitle.setText(R.string.empty_state_playlists_title);
                emptyStateDescription.setText(R.string.empty_state_playlists_description);
            } else if ("channels".equals(currentMode)) {
                emptyStateTitle.setText(R.string.empty_state_channels_title);
                emptyStateDescription.setText(R.string.empty_state_channels_description);
            } else {
                emptyStateTitle.setText(R.string.empty_state_title);
                emptyStateDescription.setText(R.string.empty_state_description);
            }
            emptyState.setVisibility(android.view.View.VISIBLE);
            videosRecyclerView.setVisibility(android.view.View.GONE);
        } else {
            emptyState.setVisibility(android.view.View.GONE);
            videosRecyclerView.setVisibility(android.view.View.VISIBLE);
        }
    }

    /**
     * Appelé quand une vidéo est cliquée.
     * Ouvre la vidéo sur YouTube ou dans le lecteur vidéo par défaut.
     */
    @Override
    public void onVideoClick(Video video) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getVideoUrl()));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_video, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Appelé quand une vidéo est supprimée.
     */
    @Override
    public void onVideoDelete(Video video) {
        showDeleteVideoConfirmation(video);
    }

    private void showDeleteVideoConfirmation(Video video) {
        String safeTitle = (video == null || TextUtils.isEmpty(video.getTitle()))
                ? getString(R.string.shared_video_default_title)
                : video.getTitle();

        AlertDialog deleteVideoDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_video)
                .setMessage(getString(R.string.delete_video_confirm_message, safeTitle))
                .setPositiveButton(R.string.confirm_delete, (dialog, which) -> {
                    if (video == null) {
                        return;
                    }
                    videoPreferenceManager.deleteVideo(video.getId());
                    videoList.remove(video);
                    refreshList();
                    Toast.makeText(this, R.string.video_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        deleteVideoDialog.show();
        styleDeleteVideoDialogButtons(deleteVideoDialog);
    }

    private void styleDeleteVideoDialogButtons(AlertDialog dialog) {
        styleDialogButtons(dialog);
        int destructiveColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(destructiveColor);
        }
    }

    /**
     * Appelé quand une vidéo est partagée.
     */
    @Override
    public void onVideoShare(Video video) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_video_text, video.getTitle(), video.getVideoUrl()));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_video)));
    }

    @Override
    public void onVideoAddToPlaylist(Video video) {
        showAssignPlaylistDialog(video);
    }

    private void showCreatePlaylistDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.playlist_name_hint);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        AlertDialog createDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.create_playlist)
                .setView(input)
                .setPositiveButton(R.string.create, null)
                .setNegativeButton(R.string.cancel, null)
                .create();
        createDialog.show();
        styleDialogButtons(createDialog);
        createDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String playlistName = input.getText() == null ? "" : input.getText().toString().trim();
            if (TextUtils.isEmpty(playlistName)) {
                input.setError(getString(R.string.playlist_name_required));
                return;
            }
            if (hasPlaylistName(playlistName)) {
                input.setError(getString(R.string.playlist_already_exists));
                return;
            }
            videoPreferenceManager.savePlaylist(playlistName);
            chipPlaylists.setChecked(true);
            setMode("playlists");
            Toast.makeText(this, R.string.playlist_created, Toast.LENGTH_SHORT).show();
            createDialog.dismiss();
        });
    }

    private void showPlaylistActionsDialog(String playlistName) {
        if (TextUtils.isEmpty(playlistName)) {
            return;
        }

        String[] actions = {
                getString(R.string.rename_playlist),
                getString(R.string.delete_playlist),
                getString(R.string.add_existing_video_to_playlist),
                getString(R.string.add_new_video_to_playlist)
        };

        AlertDialog actionsDialog = new AlertDialog.Builder(this)
                .setTitle(playlistName)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showRenamePlaylistDialog(playlistName);
                    } else if (which == 1) {
                        showDeletePlaylistDialog(playlistName);
                    } else if (which == 2) {
                        showAddExistingVideoToPlaylistDialog(playlistName);
                    } else if (which == 3) {
                        openAddNewVideoForPlaylist(playlistName);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        actionsDialog.show();
        styleDialogButtons(actionsDialog);
    }

    private void showAddExistingVideoToPlaylistDialog(String playlistName) {
        List<Video> candidates = new ArrayList<>();
        for (Video video : videoPreferenceManager.getVideos()) {
            if (!video.isInPlaylist(playlistName)) {
                candidates.add(video);
            }
        }
        sortVideosForAllMode(candidates);

        if (candidates.isEmpty()) {
            Toast.makeText(this, R.string.no_video_available_for_playlist, Toast.LENGTH_SHORT).show();
            return;
        }

        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_playlist_video_picker, null, false);
        RecyclerView recyclerView = contentView.findViewById(R.id.playlist_video_picker_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog pickerDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_existing_video_dialog_title, playlistName))
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .create();

        PlaylistVideoPickerAdapter adapter = new PlaylistVideoPickerAdapter(candidates, video -> {
            videoPreferenceManager.addVideoToPlaylist(video.getId(), playlistName);
            refreshStoredVideoList();
            Toast.makeText(this, getString(R.string.video_added_to_playlist, playlistName), Toast.LENGTH_SHORT).show();
            pickerDialog.dismiss();
        });
        recyclerView.setAdapter(adapter);

        pickerDialog.show();
        styleDialogButtons(pickerDialog);
    }

    private void openAddNewVideoForPlaylist(String playlistName) {
        Intent intent = new Intent(this, AddVideoActivity.class);
        intent.putExtra(AddVideoActivity.EXTRA_PREFILL_PLAYLIST, playlistName);
        startActivity(intent);
    }

    private void showRenamePlaylistDialog(String oldName) {
        EditText input = new EditText(this);
        input.setHint(R.string.playlist_name_hint);
        input.setText(oldName);
        input.setSelection(input.getText() == null ? 0 : input.getText().length());
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        AlertDialog renameDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.rename_playlist)
                .setView(input)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    String newName = input.getText() == null ? "" : input.getText().toString().trim();
                    if (TextUtils.isEmpty(newName)) {
                        Toast.makeText(this, R.string.playlist_name_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!oldName.equalsIgnoreCase(newName) && hasPlaylistName(newName)) {
                        Toast.makeText(this, R.string.playlist_already_exists, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    videoPreferenceManager.renamePlaylist(oldName, newName);
                    refreshStoredVideoList();
                    Toast.makeText(this, R.string.playlist_renamed, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        renameDialog.show();
        styleDialogButtons(renameDialog);
    }

    private void showDeletePlaylistDialog(String playlistName) {
        final List<Video> previousVideos = new ArrayList<>(videoPreferenceManager.getVideos());
        final List<String> previousExplicit = new ArrayList<>(videoPreferenceManager.getExplicitPlaylistNames());

        String[] options = {
                getString(R.string.delete_playlist_only),
                getString(R.string.delete_playlist_and_videos)
        };

        AlertDialog deleteDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_playlist_confirm_title, playlistName))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        videoPreferenceManager.deletePlaylistKeepVideos(playlistName);
                        refreshStoredVideoList();
                        showPlaylistUndoSnackbar(
                                getString(R.string.playlist_deleted),
                                previousVideos,
                                previousExplicit
                        );
                    } else if (which == 1) {
                        videoPreferenceManager.deletePlaylistWithVideos(playlistName);
                        refreshStoredVideoList();
                        showPlaylistUndoSnackbar(
                                getString(R.string.playlist_deleted_with_videos),
                                previousVideos,
                                previousExplicit
                        );
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        deleteDialog.show();
        styleDialogButtons(deleteDialog);
    }

    private void showPlaylistUndoSnackbar(String message, List<Video> previousVideos, List<String> previousExplicit) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    videoPreferenceManager.restoreState(previousVideos, previousExplicit);
                    refreshStoredVideoList();
                })
                .show();
    }

    private void showAssignPlaylistDialog(Video video) {
        List<String> playlists = videoPreferenceManager.getPlaylistNames();
        if (playlists.isEmpty()) {
            Toast.makeText(this, R.string.no_playlist_yet, Toast.LENGTH_SHORT).show();
            showCreatePlaylistDialog();
            return;
        }

        String[] options = playlists.toArray(new String[0]);
        AlertDialog assignDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_playlist)
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (video.isInPlaylist(selected)) {
                        Toast.makeText(this, getString(R.string.video_already_in_playlist, selected), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    videoPreferenceManager.addVideoToPlaylist(video.getId(), selected);
                    refreshStoredVideoList();
                    Toast.makeText(this, getString(R.string.video_added_to_playlist, selected), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        assignDialog.show();
        styleDialogButtons(assignDialog);
    }

    private boolean hasPlaylistName(String playlistName) {
        for (String existing : videoPreferenceManager.getPlaylistNames()) {
            if (playlistName.equalsIgnoreCase(existing)) {
                return true;
            }
        }
        return false;
    }

    private void styleDialogButtons(AlertDialog dialog) {
        int color = ContextCompat.getColor(this, R.color.appbar_text);
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        }
    }

    private void refreshStoredVideoList() {
        videoList = new ArrayList<>(videoPreferenceManager.getVideos());
        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        android.view.MenuItem sortItem = menu.findItem(R.id.action_sort_all);
        if (sortItem != null) {
            sortItem.setVisible("all".equals(currentMode));
            sortItem.setTitle(getSortLabelRes(currentSortMode));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_sort_all) {
            showSortDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        final SortMode[] modes = {
                SortMode.DATE_DESC,
                SortMode.DATE_ASC,
                SortMode.ALPHA_ASC,
                SortMode.ALPHA_DESC,
                SortMode.CHANNEL_ASC,
                SortMode.CHANNEL_DESC
        };
        final String[] labels = {
                getString(R.string.sort_date_desc),
                getString(R.string.sort_date_asc),
                getString(R.string.sort_alpha_asc),
                getString(R.string.sort_alpha_desc),
                getString(R.string.sort_channel_asc),
                getString(R.string.sort_channel_desc)
        };

        int selectedIndex = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentSortMode) {
                selectedIndex = i;
                break;
            }
        }

        final int[] newSelection = {selectedIndex};
        AlertDialog sortDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.sort_dialog_title)
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> newSelection[0] = which)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    currentSortMode = modes[newSelection[0]];
                    saveSortMode(currentSortMode);
                    refreshList();
                    invalidateOptionsMenu();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        sortDialog.show();
        styleDialogButtons(sortDialog);
    }

    private void saveSortMode(SortMode sortMode) {
        SharedPreferences prefs = getSharedPreferences(UI_PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_ALL_SORT_MODE, sortMode.name()).apply();
    }

    private SortMode loadSortMode() {
        SharedPreferences prefs = getSharedPreferences(UI_PREFS, MODE_PRIVATE);
        String raw = prefs.getString(KEY_ALL_SORT_MODE, SortMode.DATE_DESC.name());
        try {
            return SortMode.valueOf(raw);
        } catch (Exception ignored) {
            return SortMode.DATE_DESC;
        }
    }

    private int getSortLabelRes(SortMode mode) {
        switch (mode) {
            case DATE_ASC:
                return R.string.sort_date_asc;
            case ALPHA_ASC:
                return R.string.sort_alpha_asc;
            case ALPHA_DESC:
                return R.string.sort_alpha_desc;
            case CHANNEL_ASC:
                return R.string.sort_channel_asc;
            case CHANNEL_DESC:
                return R.string.sort_channel_desc;
            case DATE_DESC:
            default:
                return R.string.sort_date_desc;
        }
    }

    private static class PlaylistVideoPickerAdapter
            extends RecyclerView.Adapter<PlaylistVideoPickerAdapter.VideoPickViewHolder> {

        interface OnVideoPickListener {
            void onVideoPicked(Video video);
        }

        private final List<Video> videos;
        private final OnVideoPickListener listener;

        PlaylistVideoPickerAdapter(List<Video> videos, OnVideoPickListener listener) {
            this.videos = videos == null ? new ArrayList<>() : videos;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VideoPickViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_playlist_video_picker, parent, false);
            return new VideoPickViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoPickViewHolder holder, int position) {
            Video video = videos.get(position);
            holder.bind(video, listener);
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        static class VideoPickViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.ImageView thumbnail;
            private final TextView title;
            private final TextView subtitle;

            VideoPickViewHolder(@NonNull View itemView) {
                super(itemView);
                thumbnail = itemView.findViewById(R.id.playlist_video_item_thumbnail);
                title = itemView.findViewById(R.id.playlist_video_item_title);
                subtitle = itemView.findViewById(R.id.playlist_video_item_subtitle);
            }

            void bind(Video video, OnVideoPickListener listener) {
                String safeTitle = TextUtils.isEmpty(video.getTitle())
                        ? itemView.getContext().getString(R.string.shared_video_default_title)
                        : video.getTitle();
                String safeChannel = TextUtils.isEmpty(video.getChannel())
                        ? itemView.getContext().getString(R.string.unknown_channel)
                        : video.getChannel();
                String thumb = TextUtils.isEmpty(video.getThumbnailUrl())
                        ? buildYouTubeThumbnailUrl(video.getVideoUrl())
                        : video.getThumbnailUrl();

                Glide.with(itemView)
                        .load(thumb)
                        .placeholder(R.drawable.video_thumbnail_placeholder)
                        .error(R.drawable.video_thumbnail_placeholder)
                        .into(thumbnail);

                title.setText(safeTitle);
                subtitle.setText(safeChannel);
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onVideoPicked(video);
                    }
                });
            }

            private String buildYouTubeThumbnailUrl(String videoUrl) {
                String videoId = extractYouTubeVideoId(videoUrl);
                if (TextUtils.isEmpty(videoId)) {
                    return null;
                }
                return String.format(Locale.US, "https://i.ytimg.com/vi/%s/hqdefault.jpg", videoId);
            }

            private String extractYouTubeVideoId(String videoUrl) {
                if (TextUtils.isEmpty(videoUrl)) {
                    return null;
                }
                try {
                    Uri uri = Uri.parse(videoUrl);
                    String host = uri.getHost();
                    if (host == null) {
                        return null;
                    }
                    host = host.toLowerCase(Locale.US);
                    if (host.contains("youtu.be")) {
                        String id = uri.getLastPathSegment();
                        return isValidVideoId(id) ? id : null;
                    }
                    if (host.contains("youtube.com")) {
                        String vParam = uri.getQueryParameter("v");
                        if (isValidVideoId(vParam)) {
                            return vParam;
                        }
                        Matcher shortsMatcher = SHORTS_PATTERN.matcher(uri.getPath() == null ? "" : uri.getPath());
                        if (shortsMatcher.find()) {
                            String shortsId = shortsMatcher.group(1);
                            return isValidVideoId(shortsId) ? shortsId : null;
                        }
                    }
                } catch (Exception ignored) {
                    return null;
                }
                return null;
            }

            private boolean isValidVideoId(String videoId) {
                return !TextUtils.isEmpty(videoId) && videoId.length() == 11;
            }
        }
    }
}

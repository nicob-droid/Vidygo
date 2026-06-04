package com.example.vidygo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidygo.adapter.ChannelAdapter;
import com.example.vidygo.adapter.SectionedVideoAdapter;
import com.example.vidygo.adapter.VideoAdapter;
import com.example.vidygo.model.Video;
import com.example.vidygo.util.Logger;
import com.example.vidygo.util.VideoPreferenceManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Activité principale de l'application Vidygo.
 * Affiche la liste des vidéos YouTube préférées sauvegardées.
 */
public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoActionListener {

    private static final String UI_PREFS = "vidygo_ui_prefs";
    private static final String KEY_ALL_SORT_MODE = "all_sort_mode";

    private RecyclerView videosRecyclerView;
    private VideoAdapter videoAdapter;
    private SectionedVideoAdapter sectionedVideoAdapter;
    private ChannelAdapter channelAdapter;
    private RecyclerView.Adapter<?> currentAdapter;
    private List<Video> videoList;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddVideo;
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
        ALPHA_DESC
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoPreferenceManager = new VideoPreferenceManager(this);
        currentSortMode = loadSortMode();

        // Initialiser les vues
        initializeViews();
        setupFilters();

        // Initialiser la liste des vidéos
        initializeVideoList();

        // Configurer le RecyclerView
        setupRecyclerView();

        // Configurer les actions
        setupActions();

        // Afficher l'état vide si la liste est vide
        updateEmptyState();
    }

    /**
     * Initialise les références aux vues du layout.
     */
    private void initializeViews() {
        videosRecyclerView = findViewById(R.id.videos_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        fabAddVideo = findViewById(R.id.fab_add_video);
        chipAll = findViewById(R.id.chip_all);
        chipChannels = findViewById(R.id.chip_channels);
        chipPlaylists = findViewById(R.id.chip_playlists);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> setMode("all"));
        chipChannels.setOnClickListener(v -> setMode("channels"));
        chipPlaylists.setOnClickListener(v -> setMode("playlists"));
        chipAll.setChecked(true);
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
        // Recharger la liste au retour de l'écran d'ajout.
        videoList = new ArrayList<>(videoPreferenceManager.getVideos());
        refreshList();
    }

    private void setMode(String mode) {
        currentMode = mode;
        invalidateOptionsMenu();
        refreshList();
    }

    private void refreshList() {
        if (videoAdapter == null || sectionedVideoAdapter == null || channelAdapter == null) {
            return;
        }

        List<Video> source = new ArrayList<>(videoList);

        if ("channels".equals(currentMode)) {
            channelAdapter.rebuild(source);
            currentAdapter = channelAdapter;
            videosRecyclerView.setLayoutManager(gridLayoutManager);
        } else if ("playlists".equals(currentMode)) {
            sectionedVideoAdapter.rebuild(source, currentMode);
            currentAdapter = sectionedVideoAdapter;
            videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    /**
     * Configure le RecyclerView avec un LinearLayoutManager et l'adaptateur.
     */
    private void setupRecyclerView() {
        gridLayoutManager = new GridLayoutManager(this, 3);
        videoAdapter = new VideoAdapter(videoList, this);
        sectionedVideoAdapter = new SectionedVideoAdapter(videoList, currentMode, this);
        channelAdapter = new ChannelAdapter(videoList, channel -> {
            Intent intent = new Intent(this, ChannelVideosActivity.class);
            intent.putExtra(ChannelVideosActivity.EXTRA_CHANNEL_NAME, channel.getName());
            startActivity(intent);
        });
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
    }

    /**
     * Affiche ou masque l'état vide selon le nombre de vidéos.
     */
    private void updateEmptyState() {
        if (videoList.isEmpty()) {
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
            Toast.makeText(this, "Impossible d'ouvrir la vidéo", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Appelé quand une vidéo est supprimée.
     */
    @Override
    public void onVideoDelete(Video video) {
        videoPreferenceManager.deleteVideo(video.getId());
        videoList.remove(video);
        refreshList();
        Toast.makeText(this, "Vidéo supprimée", Toast.LENGTH_SHORT).show();
    }

    /**
     * Appelé quand une vidéo est partagée.
     */
    @Override
    public void onVideoShare(Video video) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Regarde cette vidéo : " + video.getTitle() + "\n" + video.getVideoUrl());
        startActivity(Intent.createChooser(shareIntent, "Partager la vidéo"));
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
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        final SortMode[] modes = {
                SortMode.DATE_DESC,
                SortMode.DATE_ASC,
                SortMode.ALPHA_ASC,
                SortMode.ALPHA_DESC
        };
        final String[] labels = {
                getString(R.string.sort_date_desc),
                getString(R.string.sort_date_asc),
                getString(R.string.sort_alpha_asc),
                getString(R.string.sort_alpha_desc)
        };

        int selectedIndex = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentSortMode) {
                selectedIndex = i;
                break;
            }
        }

        final int[] newSelection = {selectedIndex};
        new AlertDialog.Builder(this)
                .setTitle(R.string.sort_dialog_title)
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> newSelection[0] = which)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    currentSortMode = modes[newSelection[0]];
                    saveSortMode(currentSortMode);
                    refreshList();
                    invalidateOptionsMenu();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
            case DATE_DESC:
            default:
                return R.string.sort_date_desc;
        }
    }
}

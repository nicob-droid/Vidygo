package com.example.vidygo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité principale de l'application Vidygo.
 * Affiche la liste des vidéos YouTube préférées sauvegardées.
 */
public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoActionListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoPreferenceManager = new VideoPreferenceManager(this);

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
            videoAdapter.updateVideos(source);
            currentAdapter = videoAdapter;
            videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        videosRecyclerView.setAdapter(currentAdapter);
        updateEmptyState();
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

}


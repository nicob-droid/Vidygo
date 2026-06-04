package com.example.vidygo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidygo.adapter.VideoAdapter;
import com.example.vidygo.model.Video;
import com.example.vidygo.util.VideoPreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Affiche les vidéos d'une chaîne spécifique.
 */
public class ChannelVideosActivity extends AppCompatActivity implements VideoAdapter.OnVideoActionListener {

    public static final String EXTRA_CHANNEL_NAME = "channel_name";

    private VideoPreferenceManager videoPreferenceManager;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;
    private String channelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_videos);

        channelName = getIntent().getStringExtra(EXTRA_CHANNEL_NAME);
        videoPreferenceManager = new VideoPreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(channelName != null ? channelName : "Chaîne");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        videoList = filterByChannel(videoPreferenceManager.getVideos());
        videoAdapter = new VideoAdapter(videoList, this);

        RecyclerView recyclerView = findViewById(R.id.channel_videos_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(videoAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoList = filterByChannel(videoPreferenceManager.getVideos());
        if (videoAdapter != null) {
            videoAdapter.updateVideos(videoList);
        }
    }

    private List<Video> filterByChannel(List<Video> all) {
        List<Video> result = new ArrayList<>();
        for (Video video : all) {
            String ch = video.getChannel();
            if (channelName == null ? ch == null : channelName.equals(ch)) {
                result.add(video);
            }
        }
        return result;
    }

    @Override
    public void onVideoClick(Video video) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(video.getVideoUrl())));
        } catch (Exception e) {
            Toast.makeText(this, "Impossible d'ouvrir la vidéo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onVideoDelete(Video video) {
        videoPreferenceManager.deleteVideo(video.getId());
        videoList.remove(video);
        videoAdapter.updateVideos(videoList);
        Toast.makeText(this, "Vidéo supprimée", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVideoShare(Video video) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Regarde cette vidéo : " + video.getTitle() + "\n" + video.getVideoUrl());
        startActivity(Intent.createChooser(shareIntent, "Partager la vidéo"));
    }
}


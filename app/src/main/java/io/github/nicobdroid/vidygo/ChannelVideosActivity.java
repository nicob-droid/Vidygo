package io.github.nicobdroid.vidygo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.nicobdroid.vidygo.adapter.VideoAdapter;
import io.github.nicobdroid.vidygo.model.Video;
import io.github.nicobdroid.vidygo.util.VideoPreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Affiche les vidéos d'une chaîne spécifique.
 */
public class ChannelVideosActivity extends AppCompatActivity implements VideoAdapter.OnVideoActionListener {

    public static final String EXTRA_CHANNEL_NAME = "channel_name";
    public static final String EXTRA_PLAYLIST_NAME = "playlist_name";

    private VideoPreferenceManager videoPreferenceManager;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;
    private String channelName;
    private String playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(ContextCompat.getColor(this, R.color.gray_dark)),
                SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        );
        setContentView(R.layout.activity_channel_videos);
        applyEdgeToEdgeInsets();

        channelName = getIntent().getStringExtra(EXTRA_CHANNEL_NAME);
        playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);
        videoPreferenceManager = new VideoPreferenceManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = channelName != null ? channelName : (playlistName != null ? playlistName : getString(R.string.videos_title));
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        videoList = filterVideos(videoPreferenceManager.getVideos());
        videoAdapter = new VideoAdapter(videoList, this);

        RecyclerView recyclerView = findViewById(R.id.channel_videos_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(videoAdapter);
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

    @Override
    protected void onResume() {
        super.onResume();
        videoList = filterVideos(videoPreferenceManager.getVideos());
        if (videoAdapter != null) {
            videoAdapter.updateVideos(videoList);
        }
    }

    private List<Video> filterVideos(List<Video> all) {
        List<Video> result = new ArrayList<>();
        for (Video video : all) {
            if (channelName != null) {
                String ch = video.getChannel();
                String normalized = ch == null ? "" : ch.trim();
                if (channelName.trim().equals(normalized)) {
                    result.add(video);
                }
                continue;
            }

            if (playlistName != null) {
                if (video.isInPlaylist(playlistName)) {
                    result.add(video);
                }
                continue;
            }

            if (channelName == null && playlistName == null) {
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
            Toast.makeText(this, R.string.cannot_open_video, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onVideoDelete(Video video) {
        showDeleteVideoConfirmation(video);
    }

    private void showDeleteVideoConfirmation(Video video) {
        String safeTitle = (video == null || TextUtils.isEmpty(video.getTitle()))
                ? getString(R.string.shared_video_default_title)
                : video.getTitle();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_video)
                .setMessage(getString(R.string.delete_video_confirm_message, safeTitle))
                .setPositiveButton(R.string.confirm_delete, (d, which) -> {
                    if (video == null) {
                        return;
                    }
                    videoPreferenceManager.deleteVideo(video.getId());
                    videoList.remove(video);
                    videoAdapter.updateVideos(videoList);
                    Toast.makeText(this, R.string.video_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
        styleDeleteVideoDialogButtons(dialog);
    }

    private void styleDeleteVideoDialogButtons(AlertDialog dialog) {
        styleDialogButtons(dialog);
        int destructiveColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(destructiveColor);
        }
    }

    private void styleDialogButtons(AlertDialog dialog) {
        int color = ContextCompat.getColor(this, R.color.purple_700);
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        }
    }

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
        List<String> playlists = videoPreferenceManager.getPlaylistNames();
        if (playlists.isEmpty()) {
            Toast.makeText(this, R.string.no_playlist_yet, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = playlists.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_playlist)
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (TextUtils.isEmpty(selected)) {
                        return;
                    }
                    if (video.isInPlaylist(selected)) {
                        Toast.makeText(this, getString(R.string.video_already_in_playlist, selected), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    videoPreferenceManager.addVideoToPlaylist(video.getId(), selected);
                    videoList = filterVideos(videoPreferenceManager.getVideos());
                    videoAdapter.updateVideos(videoList);
                    Toast.makeText(this, getString(R.string.video_added_to_playlist, selected), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}


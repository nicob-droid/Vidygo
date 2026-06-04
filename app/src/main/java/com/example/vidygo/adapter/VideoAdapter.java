package com.example.vidygo.adapter;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vidygo.R;
import com.example.vidygo.model.Video;
import com.bumptech.glide.Glide;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;

/**
 * Adaptateur pour afficher la liste des vidéos YouTube dans un RecyclerView.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private static final Pattern SHORTS_PATTERN = Pattern.compile("/shorts/([a-zA-Z0-9_-]{11})");

    private List<Video> videos;
    private final OnVideoActionListener listener;

    public interface OnVideoActionListener {
        void onVideoClick(Video video);
        void onVideoDelete(Video video);
        void onVideoShare(Video video);
        void onVideoAddToPlaylist(Video video);
    }

    public VideoAdapter(List<Video> videos, OnVideoActionListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.bind(video, listener);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void updateVideos(List<Video> newVideos) {
        this.videos = newVideos;
        notifyItemRangeChanged(0, getItemCount());
    }

    public List<Video> getVideos() {
        return videos;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailImageView;
        private final TextView titleTextView;
        private final TextView channelTextView;
        private final ImageButton deleteButton;
        private final ImageButton shareButton;
        private final ImageButton addToPlaylistButton;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.video_thumbnail);
            titleTextView = itemView.findViewById(R.id.video_title);
            channelTextView = itemView.findViewById(R.id.video_channel);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            shareButton = itemView.findViewById(R.id.btn_share);
            addToPlaylistButton = itemView.findViewById(R.id.btn_add_to_playlist);
        }

        public void bind(Video video, OnVideoActionListener listener) {
            titleTextView.setText(video.getTitle());
            channelTextView.setText(video.getChannel());

            String thumbnailUrl = video.getThumbnailUrl();
            if (TextUtils.isEmpty(thumbnailUrl)) {
                thumbnailUrl = buildYouTubeThumbnailUrl(video.getVideoUrl());
            }

            Glide.with(itemView)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.video_thumbnail_placeholder)
                    .error(R.drawable.video_thumbnail_placeholder)
                    .into(thumbnailImageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoClick(video);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoDelete(video);
                }
            });

            shareButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoShare(video);
                }
            });

            addToPlaylistButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoAddToPlaylist(video);
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


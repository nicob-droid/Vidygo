package com.example.vidygo.adapter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vidygo.R;
import com.example.vidygo.model.ChannelItem;
import com.example.vidygo.model.Video;
import com.example.vidygo.util.YouTubeMetadataUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Affiche la grille des chaînes : une tuile par chaîne unique.
 * Charge les avatars réels de façon paresseuse avec cache en mémoire.
 */
public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    public enum Mode {
        CHANNELS,
        PLAYLISTS
    }

    private static final String CACHE_LOADING = "__loading__";
    private static final String CACHE_NONE = "__none__";

    public interface OnChannelClickListener {
        void onChannelClick(ChannelItem channel);
    }

    /** Cache en mémoire : nom de la chaîne → URL de l'avatar. */
    private static final Map<String, String> avatarCache = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final List<ChannelItem> channels;
    private final OnChannelClickListener listener;
    private final Mode mode;

    public ChannelAdapter(List<Video> videos, Mode mode, OnChannelClickListener listener) {
        this.listener = listener;
        this.mode = mode;
        this.channels = buildChannels(videos);
        if (this.mode == Mode.CHANNELS) {
            prefetchMissingAvatars();
        }
    }

    public void rebuild(List<Video> videos) {
        channels.clear();
        channels.addAll(buildChannels(videos));
        if (mode == Mode.CHANNELS) {
            prefetchMissingAvatars();
        }
        notifyDataSetChanged();
    }

    private void prefetchMissingAvatars() {
        for (ChannelItem channel : channels) {
            String channelName = channel.getName();
            String cached = avatarCache.get(channelName);
            if (cached != null || TextUtils.isEmpty(channel.getSampleVideoUrl())) {
                continue;
            }

            avatarCache.put(channelName, CACHE_LOADING);
            final String videoUrl = channel.getSampleVideoUrl();

            executor.execute(() -> {
                String avatarUrl = YouTubeMetadataUtil.fetchChannelAvatarForVideo(videoUrl);
                avatarCache.put(channelName, TextUtils.isEmpty(avatarUrl) ? CACHE_NONE : avatarUrl);

                if (!TextUtils.isEmpty(avatarUrl)) {
                    mainHandler.post(() -> {
                        int index = findChannelIndex(channelName);
                        if (index >= 0) {
                            notifyItemChanged(index);
                        }
                    });
                }
            });
        }
    }

    private int findChannelIndex(String channelName) {
        for (int i = 0; i < channels.size(); i++) {
            if (channelName.equals(channels.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    private List<ChannelItem> buildChannels(List<Video> videos) {
        Map<String, List<Video>> grouped = new LinkedHashMap<>();
        for (Video video : videos) {
            String key;
            if (mode == Mode.PLAYLISTS) {
                String rawPlaylist = video.getPlaylistName();
                if (TextUtils.isEmpty(rawPlaylist) || rawPlaylist.trim().isEmpty()) {
                    continue;
                }
                key = rawPlaylist.trim();
            } else {
                key = TextUtils.isEmpty(video.getChannel()) ? "Chaîne inconnue" : video.getChannel().trim();
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(video);
        }

        List<ChannelItem> result = new ArrayList<>();
        for (Map.Entry<String, List<Video>> entry : grouped.entrySet()) {
            List<Video> vids = entry.getValue();
            String thumb = mode == Mode.CHANNELS ? findChannelAvatar(vids) : findBestThumbnail(vids);
            String sampleVideoUrl = vids.get(0).getVideoUrl();
            result.add(new ChannelItem(entry.getKey(), vids.size(), thumb, sampleVideoUrl));
        }
        // Tri alphabétique (insensible à la casse)
        Collections.sort(result, (a, b) ->
                a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }

    private String findChannelAvatar(List<Video> videos) {
        for (Video v : videos) {
            if (!TextUtils.isEmpty(v.getChannelAvatarUrl())) {
                return v.getChannelAvatarUrl();
            }
        }
        return findBestThumbnail(videos);
    }

    private String findBestThumbnail(List<Video> videos) {
        for (Video video : videos) {
            if (!TextUtils.isEmpty(video.getThumbnailUrl())) return video.getThumbnailUrl();
            String derived = deriveYouTubeThumbnail(video.getVideoUrl());
            if (derived != null) return derived;
        }
        return null;
    }

    private String deriveYouTubeThumbnail(String url) {
        if (TextUtils.isEmpty(url)) return null;
        try {
            android.net.Uri uri = android.net.Uri.parse(url);
            String host = uri.getHost();
            if (host == null) return null;
            host = host.toLowerCase(Locale.US);
            String id = null;
            if (host.contains("youtu.be")) {
                id = uri.getLastPathSegment();
            } else if (host.contains("youtube.com")) {
                id = uri.getQueryParameter("v");
                if (id == null) {
                    String path = uri.getPath();
                    if (path != null && path.contains("/shorts/")) {
                        id = path.substring(path.lastIndexOf('/') + 1);
                    }
                }
            }
            if (id != null && id.length() == 11) {
                return String.format(Locale.US, "https://i.ytimg.com/vi/%s/hqdefault.jpg", id);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        holder.bind(channels.get(position), mode, listener, executor, mainHandler);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView name;
        private final TextView count;

        ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.channel_thumbnail);
            name = itemView.findViewById(R.id.channel_name);
            count = itemView.findViewById(R.id.channel_count);
        }

        void bind(ChannelItem channel, Mode mode, OnChannelClickListener listener,
                  ExecutorService executor, Handler mainHandler) {
            name.setText(channel.getName());
            int n = channel.getVideoCount();
            count.setText(n == 1 ? "1 vidéo" : n + " vidéos");

            // Tag pour détecter les vues recyclées
            thumbnail.setTag(channel.getName());

            if (mode == Mode.PLAYLISTS) {
                loadCircular(channel.getThumbnailUrl());
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onChannelClick(channel);
                });
                return;
            }

            // Vérifie le cache d'abord
            String cached = avatarCache.get(channel.getName());

            if (!TextUtils.isEmpty(cached)
                    && !CACHE_LOADING.equals(cached)
                    && !CACHE_NONE.equals(cached)) {
                // Avatar réel en cache → affichage direct
                loadCircular(cached);
            } else {
                // Affiche la miniature vidéo en attendant
                loadCircular(channel.getThumbnailUrl());

                // Lance le fetch si pas déjà en cours
                if (cached == null && !TextUtils.isEmpty(channel.getSampleVideoUrl())) {
                    avatarCache.put(channel.getName(), CACHE_LOADING);
                    final String videoUrl = channel.getSampleVideoUrl();
                    final String channelName = channel.getName();

                    executor.execute(() -> {
                        String avatarUrl = YouTubeMetadataUtil.fetchChannelAvatarForVideo(videoUrl);
                        // Stocke même si vide pour ne pas re-fetcher en boucle
                        avatarCache.put(channelName, TextUtils.isEmpty(avatarUrl) ? CACHE_NONE : avatarUrl);

                        if (!TextUtils.isEmpty(avatarUrl)) {
                            final String finalUrl = avatarUrl;
                            mainHandler.post(() -> {
                                // Met à jour seulement si la vue affiche encore cette chaîne
                                if (channelName.equals(thumbnail.getTag())) {
                                    loadCircular(finalUrl);
                                }
                            });
                        }
                    });
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onChannelClick(channel);
            });
        }

        private void loadCircular(String url) {
            Glide.with(itemView).clear(thumbnail);
            Glide.with(itemView)
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.channel_avatar_placeholder)
                    .error(R.drawable.channel_avatar_placeholder)
                    .into(thumbnail);
        }
    }
}

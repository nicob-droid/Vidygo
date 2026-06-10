package io.github.nicobdroid.vidygo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modèle de données représentant une vidéo YouTube.
 */
public class Video {
    private final String id;
    private final String title;
    private final String channel;
    private List<String> playlistNames; // Liste des playlists auxquelles la vidéo appartient
    private final String thumbnailUrl;
    private final String videoUrl;
    private final String channelAvatarUrl;
    private long dateAdded;

    public Video(String id, String title, String channel, String thumbnailUrl, String videoUrl) {
        this(id, title, channel, new ArrayList<>(), thumbnailUrl, videoUrl, "");
    }

    public Video(String id, String title, String channel, String playlistName, String thumbnailUrl, String videoUrl) {
        this(id, title, channel, createSingletonList(playlistName), thumbnailUrl, videoUrl, "");
    }

    public Video(String id, String title, String channel, String playlistName, String thumbnailUrl, String videoUrl, String channelAvatarUrl) {
        this(id, title, channel, createSingletonList(playlistName), thumbnailUrl, videoUrl, channelAvatarUrl);
    }

    public Video(String id, String title, String channel, List<String> playlistNames, String thumbnailUrl, String videoUrl, String channelAvatarUrl) {
        this.id = id;
        this.title = title;
        this.channel = channel;
        this.playlistNames = playlistNames != null ? new ArrayList<>(playlistNames) : new ArrayList<>();
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
        this.channelAvatarUrl = channelAvatarUrl != null ? channelAvatarUrl : "";
        this.dateAdded = System.currentTimeMillis();
    }

    private static List<String> createSingletonList(String playlistName) {
        List<String> list = new ArrayList<>();
        if (playlistName != null && !playlistName.trim().isEmpty()) {
            list.add(playlistName.trim());
        }
        return list;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getChannel() { return channel; }

    @Deprecated
    public String getPlaylistName() {
        return playlistNames.isEmpty() ? "" : playlistNames.get(0);
    }

    public List<String> getPlaylistNames() {
        return new ArrayList<>(playlistNames);
    }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVideoUrl() { return videoUrl; }
    public String getChannelAvatarUrl() { return channelAvatarUrl; }
    public long getDateAdded() { return dateAdded; }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Deprecated
    public void setPlaylistName(String playlistName) {
        playlistNames.clear();
        if (playlistName != null && !playlistName.trim().isEmpty()) {
            playlistNames.add(playlistName.trim());
        }
    }

    public void setPlaylistNames(List<String> playlistNames) {
        this.playlistNames.clear();
        if (playlistNames != null) {
            for (String name : playlistNames) {
                if (name != null && !name.trim().isEmpty()) {
                    this.playlistNames.add(name.trim());
                }
            }
        }
    }

    public void addToPlaylist(String playlistName) {
        if (playlistName != null && !playlistName.trim().isEmpty()) {
            String normalized = playlistName.trim();
            for (String existing : playlistNames) {
                if (existing.equalsIgnoreCase(normalized)) {
                    return; // Déjà dans cette playlist
                }
            }
            playlistNames.add(normalized);
        }
    }

    public void removeFromPlaylist(String playlistName) {
        if (playlistName == null) return;
        String normalized = playlistName.trim();
        playlistNames.removeIf(name -> name.equalsIgnoreCase(normalized));
    }

    public boolean isInPlaylist(String playlistName) {
        if (playlistName == null) return false;
        String normalized = playlistName.trim();
        for (String name : playlistNames) {
            if (name.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Video)) return false;
        Video video = (Video) o;
        return Objects.equals(id, video.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.example.vidygo.model;

import java.util.Objects;

/**
 * Modèle de données représentant une vidéo YouTube.
 */
public class Video {
    private final String id;
    private final String title;
    private final String channel;
    private final String playlistName;
    private final String thumbnailUrl;
    private final String videoUrl;
    private final String channelAvatarUrl;
    private long dateAdded;

    public Video(String id, String title, String channel, String thumbnailUrl, String videoUrl) {
        this(id, title, channel, "", thumbnailUrl, videoUrl, "");
    }

    public Video(String id, String title, String channel, String playlistName, String thumbnailUrl, String videoUrl) {
        this(id, title, channel, playlistName, thumbnailUrl, videoUrl, "");
    }

    public Video(String id, String title, String channel, String playlistName, String thumbnailUrl, String videoUrl, String channelAvatarUrl) {
        this.id = id;
        this.title = title;
        this.channel = channel;
        this.playlistName = playlistName;
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
        this.channelAvatarUrl = channelAvatarUrl != null ? channelAvatarUrl : "";
        this.dateAdded = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getChannel() { return channel; }
    public String getPlaylistName() { return playlistName; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVideoUrl() { return videoUrl; }
    public String getChannelAvatarUrl() { return channelAvatarUrl; }
    public long getDateAdded() { return dateAdded; }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
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

package com.example.vidygo.model;

/**
 * Représente une chaîne agrégée à partir de la liste des vidéos.
 */
public class ChannelItem {
    private final String name;
    private final int videoCount;
    private final String thumbnailUrl; // miniature de la première vidéo de la chaîne
    private final String sampleVideoUrl; // pour charger l'avatar réel si manquant

    public ChannelItem(String name, int videoCount, String thumbnailUrl, String sampleVideoUrl) {
        this.name = name;
        this.videoCount = videoCount;
        this.thumbnailUrl = thumbnailUrl;
        this.sampleVideoUrl = sampleVideoUrl != null ? sampleVideoUrl : "";
    }

    public String getName() {
        return name;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getSampleVideoUrl() {
        return sampleVideoUrl;
    }
}

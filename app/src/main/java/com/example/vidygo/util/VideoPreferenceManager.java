package com.example.vidygo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.vidygo.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour gérer la persistance des vidéos.
 * Utilise SharedPreferences pour stocker les vidéos en JSON.
 * À remplacer par Room Database pour une meilleure scalabilité.
 */
public class VideoPreferenceManager {

    private static final String SHARED_PREF_NAME = "vidygo_preferences";
    private static final String KEY_VIDEOS = "videos_list";

    private final SharedPreferences sharedPreferences;

    public VideoPreferenceManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Sauvegarde une liste de vidéos
     */
    public void saveVideos(List<Video> videos) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Video video : videos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", video.getId());
                jsonObject.put("title", video.getTitle());
                jsonObject.put("channel", video.getChannel());
                jsonObject.put("playlistName", video.getPlaylistName());
                jsonObject.put("thumbnailUrl", video.getThumbnailUrl());
                jsonObject.put("videoUrl", video.getVideoUrl());
                jsonObject.put("channelAvatarUrl", video.getChannelAvatarUrl());
                jsonObject.put("dateAdded", video.getDateAdded());
                jsonArray.put(jsonObject);
            }
            sharedPreferences.edit().putString(KEY_VIDEOS, jsonArray.toString()).apply();
        } catch (JSONException e) {
            Logger.e("Erreur lors de la sauvegarde des vidéos", e);
        }
    }

    /**
     * Récupère la liste des vidéos sauvegardées
     */
    public List<Video> getVideos() {
        List<Video> videos = new ArrayList<>();
        String jsonString = sharedPreferences.getString(KEY_VIDEOS, "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Video video = new Video(
                        jsonObject.getString("id"),
                        jsonObject.getString("title"),
                        jsonObject.getString("channel"),
                        jsonObject.optString("playlistName", ""),
                        jsonObject.getString("thumbnailUrl"),
                        jsonObject.getString("videoUrl"),
                        jsonObject.optString("channelAvatarUrl", "")
                );
                video.setDateAdded(jsonObject.getLong("dateAdded"));
                videos.add(video);
            }
        } catch (JSONException e) {
            Logger.e("Erreur lors du chargement des vidéos", e);
        }

        return videos;
    }

    /**
     * Sauvegarde une vidéo unique
     */
    public void saveVideo(Video video) {
        List<Video> videos = getVideos();
        videos.add(0, video); // Ajouter au début
        saveVideos(videos);
    }

    /**
     * Supprime une vidéo
     */
    public void deleteVideo(String videoId) {
        List<Video> videos = getVideos();
        videos.removeIf(video -> video.getId().equals(videoId));
        saveVideos(videos);
    }

    /**
     * Nettoie toutes les vidéos
     */
    public void clearAll() {
        sharedPreferences.edit().remove(KEY_VIDEOS).apply();
    }
}


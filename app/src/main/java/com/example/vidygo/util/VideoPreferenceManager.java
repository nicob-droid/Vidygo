package com.example.vidygo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.vidygo.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe utilitaire pour gérer la persistance des vidéos.
 * Utilise SharedPreferences pour stocker les vidéos en JSON.
 * À remplacer par Room Database pour une meilleure scalabilité.
 */
public class VideoPreferenceManager {

    private static final String SHARED_PREF_NAME = "vidygo_preferences";
    private static final String KEY_VIDEOS = "videos_list";
    private static final String KEY_PLAYLISTS = "playlists_list";

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
        if (video.getPlaylistName() != null && !video.getPlaylistName().trim().isEmpty()) {
            savePlaylist(video.getPlaylistName());
        }
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
     * Met à jour la playlist d'une vidéo existante.
     */
    public void updateVideoPlaylist(String videoId, String playlistName) {
        List<Video> videos = getVideos();
        for (Video video : videos) {
            if (video.getId().equals(videoId)) {
                video.setPlaylistName(playlistName);
                break;
            }
        }
        saveVideos(videos);
        if (playlistName != null && !playlistName.trim().isEmpty()) {
            savePlaylist(playlistName);
        }
    }

    /**
     * Crée une playlist explicite (même sans vidéo).
     */
    public void savePlaylist(String playlistName) {
        String normalized = playlistName == null ? "" : playlistName.trim();
        if (normalized.isEmpty()) {
            return;
        }
        List<String> playlists = getExplicitPlaylists();
        for (String existing : playlists) {
            if (normalized.equalsIgnoreCase(existing)) {
                return;
            }
        }
        playlists.add(normalized);
        Collections.sort(playlists, String::compareToIgnoreCase);
        saveExplicitPlaylists(playlists);
    }

    /**
     * Retourne la liste triée des playlists non vides.
     */
    public List<String> getPlaylistNames() {
        Set<String> unique = new LinkedHashSet<>();
        for (String explicit : getExplicitPlaylists()) {
            if (explicit != null && !explicit.trim().isEmpty()) {
                unique.add(explicit.trim());
            }
        }
        for (Video video : getVideos()) {
            String playlist = video.getPlaylistName();
            if (playlist != null && !playlist.trim().isEmpty()) {
                unique.add(playlist.trim());
            }
        }
        List<String> result = new ArrayList<>(unique);
        Collections.sort(result, String::compareToIgnoreCase);
        return result;
    }

    /**
     * Retourne les playlists explicites (créées manuellement), triées.
     */
    public List<String> getExplicitPlaylistNames() {
        List<String> result = getExplicitPlaylists();
        Collections.sort(result, String::compareToIgnoreCase);
        return result;
    }

    /**
     * Renomme une playlist dans la liste explicite et dans les vidéos liées.
     */
    public void renamePlaylist(String oldName, String newName) {
        String oldNorm = normalizePlaylistName(oldName);
        String newNorm = normalizePlaylistName(newName);
        if (oldNorm.isEmpty() || newNorm.isEmpty() || oldNorm.equalsIgnoreCase(newNorm)) {
            return;
        }

        List<String> explicit = getExplicitPlaylists();
        boolean hadOld = false;
        for (int i = 0; i < explicit.size(); i++) {
            if (oldNorm.equalsIgnoreCase(explicit.get(i))) {
                explicit.set(i, newNorm);
                hadOld = true;
            }
        }
        if (!hadOld) {
            explicit.add(newNorm);
        }

        List<Video> videos = getVideos();
        for (Video video : videos) {
            if (isSamePlaylist(oldNorm, video.getPlaylistName())) {
                video.setPlaylistName(newNorm);
            }
        }
        saveVideos(videos);
        saveExplicitPlaylists(explicit);
    }

    /**
     * Supprime une playlist en conservant les vidéos (elles perdent juste l'affectation playlist).
     */
    public void deletePlaylistKeepVideos(String playlistName) {
        String normalized = normalizePlaylistName(playlistName);
        if (normalized.isEmpty()) {
            return;
        }

        List<String> explicit = getExplicitPlaylists();
        explicit.removeIf(name -> normalized.equalsIgnoreCase(name));
        saveExplicitPlaylists(explicit);

        List<Video> videos = getVideos();
        for (Video video : videos) {
            if (isSamePlaylist(normalized, video.getPlaylistName())) {
                video.setPlaylistName("");
            }
        }
        saveVideos(videos);
    }

    /**
     * Supprime une playlist et toutes les vidéos qui y sont associées.
     */
    public void deletePlaylistWithVideos(String playlistName) {
        String normalized = normalizePlaylistName(playlistName);
        if (normalized.isEmpty()) {
            return;
        }

        List<String> explicit = getExplicitPlaylists();
        explicit.removeIf(name -> normalized.equalsIgnoreCase(name));
        saveExplicitPlaylists(explicit);

        List<Video> videos = getVideos();
        videos.removeIf(video -> isSamePlaylist(normalized, video.getPlaylistName()));
        saveVideos(videos);
    }

    /**
     * Restaure un état complet (utilisé par l'action Undo).
     */
    public void restoreState(List<Video> videos, List<String> explicitPlaylists) {
        saveVideos(videos == null ? new ArrayList<>() : videos);
        saveExplicitPlaylists(explicitPlaylists == null ? new ArrayList<>() : explicitPlaylists);
    }

    /**
     * Nettoie toutes les vidéos
     */
    public void clearAll() {
        sharedPreferences.edit().remove(KEY_VIDEOS).remove(KEY_PLAYLISTS).apply();
    }

    private List<String> getExplicitPlaylists() {
        List<String> playlists = new ArrayList<>();
        String raw = sharedPreferences.getString(KEY_PLAYLISTS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String name = array.optString(i, "").trim();
                if (!name.isEmpty()) {
                    playlists.add(name);
                }
            }
        } catch (JSONException e) {
            Logger.e("Erreur lors du chargement des playlists", e);
        }
        return playlists;
    }

    private void saveExplicitPlaylists(List<String> playlists) {
        List<String> normalized = new ArrayList<>();
        for (String playlist : playlists) {
            String value = normalizePlaylistName(playlist);
            if (value.isEmpty()) {
                continue;
            }
            boolean exists = false;
            for (String existing : normalized) {
                if (value.equalsIgnoreCase(existing)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                normalized.add(value);
            }
        }
        Collections.sort(normalized, String::compareToIgnoreCase);

        JSONArray array = new JSONArray();
        for (String playlist : normalized) {
            array.put(playlist);
        }
        sharedPreferences.edit().putString(KEY_PLAYLISTS, array.toString()).apply();
    }

    private String normalizePlaylistName(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isSamePlaylist(String a, String b) {
        return normalizePlaylistName(a).equalsIgnoreCase(normalizePlaylistName(b));
    }
}


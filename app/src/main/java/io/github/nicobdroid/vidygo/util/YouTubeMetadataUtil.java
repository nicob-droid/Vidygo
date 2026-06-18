package io.github.nicobdroid.vidygo.util;

import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitaires pour récupérer les métadonnées YouTube (titre, chaîne, avatar).
 */
public class YouTubeMetadataUtil {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String YOUTUBE_OEMBED_ENDPOINT = "https://www.youtube.com/oembed";
    private static final String YOUTUBE_VIDEOS_ENDPOINT = "https://www.googleapis.com/youtube/v3/videos";
    private static final String YOUTUBE_CHANNELS_ENDPOINT = "https://www.googleapis.com/youtube/v3/channels";

    /** Récupère titre, nom de chaîne et avatar via oEmbed et YouTube Data API v3. */
    public static class VideoMetadata {
        public final String title;
        public final String channelName;
        public final String channelAvatarUrl;

        VideoMetadata(String title, String channelName, String channelAvatarUrl) {
            this.title = title;
            this.channelName = channelName;
            this.channelAvatarUrl = channelAvatarUrl != null ? channelAvatarUrl : "";
        }
    }

    public static VideoMetadata fetchVideoMetadata(String videoUrl) throws Exception {
        String title = "";
        String channelName = "";
        String channelUrl = "";

        JSONObject oembedJson = fetchJson(buildOembedEndpoint(videoUrl));
        if (oembedJson != null) {
            title = oembedJson.optString("title", "");
            channelName = oembedJson.optString("author_name", "");
            channelUrl = oembedJson.optString("author_url", "");
        }

        VideoApiMetadata apiMetadata = fetchVideoApiMetadata(videoUrl);
        String avatarUrl = "";
        if (apiMetadata != null) {
            if (TextUtils.isEmpty(title)) {
                title = apiMetadata.title;
            }
            if (TextUtils.isEmpty(channelName)) {
                channelName = apiMetadata.channelTitle;
            }
            avatarUrl = fetchChannelAvatarById(apiMetadata.channelId);
        }

        if (TextUtils.isEmpty(avatarUrl) && !TextUtils.isEmpty(channelUrl)) {
            ChannelReference reference = extractChannelReference(channelUrl);
            avatarUrl = fetchChannelAvatar(reference);
            if (TextUtils.isEmpty(avatarUrl)) {
                avatarUrl = fetchChannelAvatarFromPage(channelUrl);
            }
        }

        return new VideoMetadata(title, channelName, avatarUrl);
    }

    public static String fetchChannelAvatarForVideo(String videoUrl) {
        try {
            VideoApiMetadata metadata = fetchVideoApiMetadata(videoUrl);
            if (metadata != null) {
                String avatarUrl = fetchChannelAvatarById(metadata.channelId);
                if (!TextUtils.isEmpty(avatarUrl)) {
                    return avatarUrl;
                }
            }

            JSONObject oembedJson = fetchJson(buildOembedEndpoint(videoUrl));
            if (oembedJson == null) return "";

            String channelUrl = oembedJson.optString("author_url", "");
            ChannelReference reference = extractChannelReference(channelUrl);
            String avatar = fetchChannelAvatar(reference);
            if (!TextUtils.isEmpty(avatar)) {
                return avatar;
            }
            return fetchChannelAvatarFromPage(channelUrl);
        } catch (Exception e) {
            Logger.w("fetchChannelAvatarForVideo : " + e.getMessage());
            return "";
        }
    }

    private static VideoApiMetadata fetchVideoApiMetadata(String videoUrl) throws Exception {
        String apiKey = getYoutubeApiKey();
        String videoId = extractVideoId(videoUrl);
        if (TextUtils.isEmpty(apiKey) || TextUtils.isEmpty(videoId)) {
            return null;
        }

        String endpoint = YOUTUBE_VIDEOS_ENDPOINT
                + "?part=snippet&id=" + URLEncoder.encode(videoId, StandardCharsets.UTF_8.name())
                + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name());

        JSONObject json = fetchJson(endpoint);
        if (json == null) return null;

        JSONObject item = json.optJSONArray("items") != null && json.optJSONArray("items").length() > 0
                ? json.optJSONArray("items").optJSONObject(0)
                : null;
        JSONObject snippet = item != null ? item.optJSONObject("snippet") : null;
        if (snippet == null) return null;

        return new VideoApiMetadata(
                snippet.optString("title", ""),
                snippet.optString("channelTitle", ""),
                snippet.optString("channelId", "")
        );
    }

    private static String fetchChannelAvatar(ChannelReference reference) throws Exception {
        if (reference == null) return "";
        if (!TextUtils.isEmpty(reference.channelId)) {
            return fetchChannelAvatarById(reference.channelId);
        }
        if (!TextUtils.isEmpty(reference.username)) {
            return fetchChannelAvatarByUsername(reference.username);
        }
        return "";
    }

    private static String fetchChannelAvatarById(String channelId) throws Exception {
        if (TextUtils.isEmpty(channelId)) return "";
        String apiKey = getYoutubeApiKey();
        if (TextUtils.isEmpty(apiKey)) {
            return fetchChannelAvatarFromPage("https://www.youtube.com/channel/" + channelId);
        }

        String endpoint = YOUTUBE_CHANNELS_ENDPOINT
                + "?part=snippet&id=" + URLEncoder.encode(channelId, StandardCharsets.UTF_8.name())
                + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name());
        return extractChannelAvatarUrl(fetchJson(endpoint));
    }

    private static String fetchChannelAvatarByUsername(String username) throws Exception {
        if (TextUtils.isEmpty(username)) return "";
        String apiKey = getYoutubeApiKey();
        if (TextUtils.isEmpty(apiKey)) {
            return fetchChannelAvatarFromPage("https://www.youtube.com/user/" + username);
        }

        String endpoint = YOUTUBE_CHANNELS_ENDPOINT
                + "?part=snippet&forUsername=" + URLEncoder.encode(username, StandardCharsets.UTF_8.name())
                + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name());
        return extractChannelAvatarUrl(fetchJson(endpoint));
    }

    private static String extractChannelAvatarUrl(JSONObject json) {
        if (json == null || json.optJSONArray("items") == null || json.optJSONArray("items").length() == 0) {
            return "";
        }

        JSONObject item = json.optJSONArray("items").optJSONObject(0);
        JSONObject snippet = item != null ? item.optJSONObject("snippet") : null;
        JSONObject thumbnails = snippet != null ? snippet.optJSONObject("thumbnails") : null;
        if (thumbnails == null) return "";

        String high = readThumbnailUrl(thumbnails, "high");
        if (!TextUtils.isEmpty(high)) return high;

        String medium = readThumbnailUrl(thumbnails, "medium");
        if (!TextUtils.isEmpty(medium)) return medium;

        return readThumbnailUrl(thumbnails, "default");
    }

    private static String readThumbnailUrl(JSONObject thumbnails, String quality) {
        JSONObject thumbnail = thumbnails.optJSONObject(quality);
        return thumbnail != null ? thumbnail.optString("url", "") : "";
    }

    private static String fetchChannelAvatarFromPage(String channelUrl) {
        if (TextUtils.isEmpty(channelUrl)) return "";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(channelUrl).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            try {
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return "";
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder html = new StringBuilder();
                String line;
                int total = 0;
                while ((line = reader.readLine()) != null && total < 700_000) {
                    html.append(line);
                    total += line.length();
                    if (total > 30_000 && html.indexOf("og:image") >= 0) {
                        break;
                    }
                }
                reader.close();
                return extractAvatarFromHtml(html.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Logger.w("fetchChannelAvatarFromPage : " + e.getMessage());
            return "";
        }
    }

    private static String extractAvatarFromHtml(String html) {
        if (TextUtils.isEmpty(html)) return "";

        Matcher ogImage = Pattern.compile("property=\"og:image\"[^>]*content=\"([^\"]+)\"")
                .matcher(html);
        if (ogImage.find()) {
            return decodeHtml(ogImage.group(1));
        }

        Matcher reverse = Pattern.compile("content=\"([^\"]+)\"[^>]*property=\"og:image\"")
                .matcher(html);
        if (reverse.find()) {
            return decodeHtml(reverse.group(1));
        }

        Matcher ytc = Pattern.compile("https://yt3\\.googleusercontent\\.com/[^\"'<>\\s]+")
                .matcher(html);
        if (ytc.find()) {
            return decodeHtml(ytc.group(0));
        }

        return "";
    }

    private static String decodeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&amp;", "&")
                .replace("\\u0026", "&")
                .replace("\\u003d", "=");
    }

    public static String extractVideoId(String url) {
        if (TextUtils.isEmpty(url)) return "";
        try {
            Uri uri = Uri.parse(url.trim());
            String host = uri.getHost();
            if (host == null) return "";

            host = host.toLowerCase(Locale.US);
            if (host.contains("youtu.be")) {
                String id = uri.getLastPathSegment();
                return isValidVideoId(id) ? id : "";
            }

            if (!host.contains("youtube.com")) {
                return "";
            }

            String id = uri.getQueryParameter("v");
            if (isValidVideoId(id)) {
                return id;
            }

            String path = uri.getPath();
            if (TextUtils.isEmpty(path)) {
                return "";
            }

            String[] segments = path.split("/");
            for (int i = 0; i < segments.length - 1; i++) {
                if ("embed".equals(segments[i]) || "shorts".equals(segments[i]) || "live".equals(segments[i])) {
                    String candidate = segments[i + 1];
                    return isValidVideoId(candidate) ? candidate : "";
                }
            }
        } catch (Exception e) {
            Logger.w("Impossible d'extraire l'identifiant vidéo", e);
        }
        return "";
    }

    private static boolean isValidVideoId(String id) {
        return !TextUtils.isEmpty(id) && id.length() == 11;
    }

    private static ChannelReference extractChannelReference(String channelUrl) {
        if (TextUtils.isEmpty(channelUrl)) return null;
        try {
            Uri uri = Uri.parse(channelUrl.trim());
            String host = uri.getHost();
            if (host == null || !host.toLowerCase(Locale.US).contains("youtube.com")) {
                return null;
            }

            String[] segments = uri.getPath() != null ? uri.getPath().split("/") : new String[0];
            for (int i = 0; i < segments.length; i++) {
                if (TextUtils.isEmpty(segments[i])) continue;

                if ("channel".equals(segments[i]) && i + 1 < segments.length) {
                    return new ChannelReference(segments[i + 1], "");
                }

                if ("user".equals(segments[i]) && i + 1 < segments.length) {
                    return new ChannelReference("", segments[i + 1]);
                }
            }
        } catch (Exception e) {
            Logger.w("Impossible de résoudre la référence de chaîne", e);
        }
        return null;
    }

    private static String buildOembedEndpoint(String videoUrl) throws Exception {
        return YOUTUBE_OEMBED_ENDPOINT + "?url="
                + URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.name())
                + "&format=json";
    }

    private static String getYoutubeApiKey() {
        // Pas de dépendance compile-time à BuildConfig.YOUTUBE_DATA_API_KEY.
        try {
            Class<?> buildConfigClass = Class.forName("io.github.nicobdroid.vidygo.BuildConfig");
            Object value = buildConfigClass.getField("YOUTUBE_DATA_API_KEY").get(null);
            return value == null ? "" : String.valueOf(value).trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static JSONObject fetchJson(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Logger.w("HTTP " + responseCode + " pour " + url);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return new JSONObject(sb.toString());
        } finally {
            conn.disconnect();
        }
    }

    private static class VideoApiMetadata {
        final String title;
        final String channelTitle;
        final String channelId;

        VideoApiMetadata(String title, String channelTitle, String channelId) {
            this.title = title != null ? title : "";
            this.channelTitle = channelTitle != null ? channelTitle : "";
            this.channelId = channelId != null ? channelId : "";
        }
    }

    private static class ChannelReference {
        final String channelId;
        final String username;

        ChannelReference(String channelId, String username) {
            this.channelId = channelId != null ? channelId : "";
            this.username = username != null ? username : "";
        }
    }
}


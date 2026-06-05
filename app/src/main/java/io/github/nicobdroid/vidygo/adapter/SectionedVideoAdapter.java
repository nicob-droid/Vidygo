package io.github.nicobdroid.vidygo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.nicobdroid.vidygo.R;
import io.github.nicobdroid.vidygo.model.Video;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptateur qui affiche les vidéos en sections par chaîne ou par playlist.
 */
public class SectionedVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<RowItem> items = new ArrayList<>();
    private final VideoAdapter.OnVideoActionListener listener;

    public SectionedVideoAdapter(List<Video> videos, String mode, VideoAdapter.OnVideoActionListener listener) {
        this.listener = listener;
        rebuild(videos, mode);
    }

    public void rebuild(List<Video> videos, String mode) {
        int oldCount = items.size();
        items.clear();
        if (videos == null || videos.isEmpty()) {
            if (oldCount > 0) {
                notifyItemRangeRemoved(0, oldCount);
            }
            return;
        }

        if ("channels".equals(mode)) {
            buildSections(videos, true);
        } else if ("playlists".equals(mode)) {
            buildSections(videos, false);
        } else {
            for (Video video : videos) {
                items.add(RowItem.video(video));
            }
        }
        if (oldCount > 0) {
            notifyItemRangeRemoved(0, oldCount);
        }
        if (!items.isEmpty()) {
            notifyItemRangeInserted(0, items.size());
        }
    }

    private void buildSections(List<Video> videos, boolean byChannel) {
        Map<String, List<Video>> grouped = new LinkedHashMap<>();
        for (Video video : videos) {
            String key = byChannel
                    ? safeLabel(video.getChannel(), "Chaîne inconnue")
                    : safeLabel(video.getPlaylistName(), "Sans playlist");
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(video);
        }

        for (Map.Entry<String, List<Video>> entry : grouped.entrySet()) {
            items.add(RowItem.header(entry.getKey()));
            for (Video video : entry.getValue()) {
                items.add(RowItem.video(video));
            }
        }
    }

    private String safeLabel(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RowItem.TYPE_HEADER) {
            TextView header = new TextView(parent.getContext());
            header.setTextSize(18f);
            header.setPadding(32, 24, 32, 12);
            header.setTextColor(parent.getContext().getResources().getColor(android.R.color.black, null));
            return new HeaderViewHolder(header);
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoAdapter.VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RowItem item = items.get(position);
        if (item.type == RowItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(item.label);
        } else {
            ((VideoAdapter.VideoViewHolder) holder).bind(item.video, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(String label) {
            ((TextView) itemView).setText(label);
        }
    }

    private static class RowItem {
        static final int TYPE_HEADER = 0;
        static final int TYPE_VIDEO = 1;

        final int type;
        final String label;
        final Video video;

        private RowItem(int type, String label, Video video) {
            this.type = type;
            this.label = label;
            this.video = video;
        }

        static RowItem header(String label) {
            return new RowItem(TYPE_HEADER, label, null);
        }

        static RowItem video(Video video) {
            return new RowItem(TYPE_VIDEO, null, video);
        }
    }
}


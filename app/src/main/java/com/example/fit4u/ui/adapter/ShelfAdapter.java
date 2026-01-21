package com.example.fit4u.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit4u.ui.model.ClothingItem;
import com.example.fit4u.R;

import java.util.List;
import java.util.Map;

public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.VH> {

    public interface OnItemClick {
        void onClick(ClothingItem item);
    }

    private final List<String> categories;
    private final Map<String, List<ClothingItem>> data;
    private final OnItemClick listener;

    public ShelfAdapter(List<String> categories,
                        Map<String, List<ClothingItem>> data,
                        OnItemClick listener) {
        this.categories = categories;
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shelf, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String category = categories.get(position);

        // Title (fill)
        if (h.tvTitle != null) h.tvTitle.setText(category);

        // Title (outline) - exists only if you used the new item_shelf.xml
        if (h.tvTitleOutline != null) h.tvTitleOutline.setText(category);

        List<ClothingItem> items = data.get(category);

        // avoid null list crash
        if (items == null) items = java.util.Collections.emptyList();

        ClosetItemAdapter adapter = new ClosetItemAdapter(items, item -> {
            if (listener != null) listener.onClick(item);
        });

        h.rvItems.setLayoutManager(new LinearLayoutManager(
                h.itemView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        h.rvItems.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvTitleOutline; // <-- NEW
        RecyclerView rvItems;

        VH(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvShelfTitle);
            rvItems = itemView.findViewById(R.id.rvItems);

            // If you didn't add tvShelfTitleOutline in XML, this will just be null (and that's ok)
            tvTitleOutline = itemView.findViewById(R.id.tvShelfTitleOutline);
        }
    }
}

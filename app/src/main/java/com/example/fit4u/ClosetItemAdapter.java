package com.example.fit4u;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ClosetItemAdapter extends RecyclerView.Adapter<ClosetItemAdapter.VH> {

    private final List<ClothingItem> items;
    private final ShelfAdapter.OnItemClick listener; // ✅ אותו listener

    public ClosetItemAdapter(List<ClothingItem> items, ShelfAdapter.OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clothing, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ClothingItem item = items.get(position);

        h.tvColor.setText(item.color == null ? "" : item.color);

        Glide.with(h.img.getContext())
                .load(item.imageUrl)
                .centerCrop()
                .into(h.img);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvColor;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgItem);
            tvColor = itemView.findViewById(R.id.tvColor);
        }
    }
}

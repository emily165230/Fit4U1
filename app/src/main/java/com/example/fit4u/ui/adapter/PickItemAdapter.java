package com.example.fit4u.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fit4u.ui.model.ClothingItem;
import com.example.fit4u.R;

import java.util.List;

public class PickItemAdapter extends RecyclerView.Adapter<PickItemAdapter.VH> {

    public interface OnPick {
        void onPick(ClothingItem item);
    }

    private final List<ClothingItem> items;
    private final OnPick listener;

    public PickItemAdapter(List<ClothingItem> items, OnPick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pick, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ClothingItem item = items.get(position);

        if (item.imageUrl != null && !item.imageUrl.trim().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(h.img);
        } else {
            h.img.setImageResource(R.drawable.placeholder_item);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPick);
        }
    }
}

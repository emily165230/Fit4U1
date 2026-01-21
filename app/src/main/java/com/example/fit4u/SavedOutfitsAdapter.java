package com.example.fit4u;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedOutfitsAdapter extends RecyclerView.Adapter<SavedOutfitsAdapter.VH> {

    public interface OnOutfitClick {
        void onClick(SavedOutfit outfit);
    }

    private final List<SavedOutfit> items;
    private final OnOutfitClick listener;

    public SavedOutfitsAdapter(List<SavedOutfit> items, OnOutfitClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_outfit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SavedOutfit o = items.get(position);

        // Title (date)
        String title = (o.dayKey != null && !o.dayKey.isEmpty())
                ? o.dayKey
                : formatDate(o.createdAtMs);
        h.tvDate.setText(title);

        load(h.img1, o.topsUrl);
        load(h.img2, o.pantsUrl);
        load(h.img3, o.shoesUrl);
        load(h.img4, o.jacketUrl);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(o);
        });
    }

    private void load(ImageView iv, String url) {
        if (url == null || url.trim().isEmpty()) {
            iv.setImageResource(R.drawable.placeholder_item);
            return;
        }
        Glide.with(iv.getContext())
                .load(url)
                .centerCrop()
                .into(iv);
    }

    private String formatDate(long ms) {
        if (ms <= 0) return "Saved outfit";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(ms));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageView img1, img2, img3, img4;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvOutfitDate);
            img1 = itemView.findViewById(R.id.imgGrid1);
            img2 = itemView.findViewById(R.id.imgGrid2);
            img3 = itemView.findViewById(R.id.imgGrid3);
            img4 = itemView.findViewById(R.id.imgGrid4);
        }
    }
}

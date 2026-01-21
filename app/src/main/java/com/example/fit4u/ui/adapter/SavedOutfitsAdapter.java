package com.example.fit4u.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fit4u.R;
import com.example.fit4u.ui.model.SavedOutfit;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SavedOutfitsAdapter extends RecyclerView.Adapter<SavedOutfitsAdapter.VH> {

    public interface OnDeleteClick {
        void onDelete(SavedOutfit outfit);
    }

    private final List<SavedOutfit> data;
    private final OnDeleteClick deleteListener;

    public SavedOutfitsAdapter(List<SavedOutfit> data, OnDeleteClick deleteListener) {
        this.data = data;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_outfit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SavedOutfit o = data.get(position);

        // Title date
        String title = (o.dayKey != null && !o.dayKey.isEmpty()) ? o.dayKey : formatTimestamp(o);
        h.tvDate.setText(title);

        loadInto(h.img1, o.topsUrl);
        loadInto(h.img2, o.pantsUrl);
        loadInto(h.img3, o.shoesUrl);
        loadInto(h.img4, o.jacketUrl);

        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(o);
        });
    }

    private void loadInto(ImageView img, String url) {
        if (url == null || url.trim().isEmpty()) {
            img.setImageResource(R.drawable.placeholder_item);
            return;
        }
        Glide.with(img.getContext())
                .load(url)
                .centerCrop()
                .into(img);
    }

    private String formatTimestamp(SavedOutfit o) {
        try {
            if (o.createdAt == null) return "Saved outfit";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault());
            return sdf.format(o.createdAt.toDate());
        } catch (Exception e) {
            return "Saved outfit";
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageButton btnDelete;
        ImageView img1, img2, img3, img4;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvOutfitDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteOutfit);

            img1 = itemView.findViewById(R.id.imgGrid1);
            img2 = itemView.findViewById(R.id.imgGrid2);
            img3 = itemView.findViewById(R.id.imgGrid3);
            img4 = itemView.findViewById(R.id.imgGrid4);
        }
    }
}

package com.example.fit4u;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.VH> {

    private final List<String> categories;
    private final Map<String, List<ClothingItem>> data;

    public ShelfAdapter(List<String> categories, Map<String, List<ClothingItem>> data) {
        this.categories = categories;
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shelf, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String cat = categories.get(position);
        h.tvTitle.setText(cat);

        List<ClothingItem> items = data.get(cat);

        h.rv.setLayoutManager(new LinearLayoutManager(h.rv.getContext(), LinearLayoutManager.HORIZONTAL, false));
        h.rv.setAdapter(new ClosetItemAdapter(items));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        RecyclerView rv;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvShelfTitle);
            rv = itemView.findViewById(R.id.rvItemsHorizontal);
        }
    }
}

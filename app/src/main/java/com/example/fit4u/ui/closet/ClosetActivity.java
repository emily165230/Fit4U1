package com.example.fit4u.ui.closet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit4u.ui.model.ClothingItem;
import com.example.fit4u.R;
import com.example.fit4u.ui.adapter.ShelfAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClosetActivity extends AppCompatActivity {

    private RecyclerView rvShelves;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<String> categories = new ArrayList<>();
    private final Map<String, List<ClothingItem>> data = new HashMap<>();

    private ShelfAdapter shelfAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);

        // Back - הכי נקי: פשוט לחזור אחורה (ל-Home)
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvShelves = findViewById(R.id.rvShelves);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // קטגוריות
        categories.clear();
        categories.add("Jackets");
        categories.add("Pants");
        categories.add("Tops");
        categories.add("Bags");
        categories.add("Shoes");
        categories.add("Accessories");
        categories.add("Other");

        data.clear();
        for (String c : categories) data.put(c, new ArrayList<>());

        shelfAdapter = new ShelfAdapter(categories, data, item -> {
            Intent i = new Intent(ClosetActivity.this, ItemDetailActivity.class);
            i.putExtra("docId", item.id);
            i.putExtra("category", item.category);
            i.putExtra("color", item.color);
            i.putExtra("imageUrl", item.imageUrl);
            startActivity(i);
        });

        rvShelves.setLayoutManager(new LinearLayoutManager(this));
        rvShelves.setAdapter(shelfAdapter);

        loadCloset();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCloset(); // רענון אחרי שחוזרים מדף פריט
    }

    private void loadCloset() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("closet")
                .get()
                .addOnSuccessListener(qs -> {

                    // ניקוי מדפים
                    for (String c : categories) {
                        List<ClothingItem> list = data.get(c);
                        if (list != null) list.clear();
                    }

                    // מילוי מחדש
                    for (QueryDocumentSnapshot doc : qs) {
                        String docId = doc.getId();
                        String rawCategory = doc.getString("category");
                        String color = doc.getString("color");
                        String imageUrl = doc.getString("imageUrl");

                        String category = normalizeCategory(rawCategory);
                        if (!data.containsKey(category)) category = "Other";

                        data.get(category).add(new ClothingItem(docId, category, color, imageUrl));
                    }

                    shelfAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String normalizeCategory(String c) {
        if (c == null) return "Other";

        String s = c.trim().toLowerCase();

        if (s.contains("jacket") || s.contains("coat") || s.contains("outer") || s.contains("trench"))
            return "Jackets";

        if (s.contains("pant") || s.contains("jean") || s.contains("shorts") || s.contains("tights") )
            return "Pants";

        if (s.contains("top") || s.contains("shirt") || s.contains("tee") || s.contains("tshirt") || s.contains("tank") || s.contains("sweater") || s.contains("cardigan"))
            return "Tops";

        if (s.contains("bag") || s.contains("purse") || s.contains("handbag"))
            return "Bags";

        if (s.contains("shoes") || s.contains("sneakers") || s.contains("heels") || s.contains("boots"))
            return "Shoes";

        if (s.contains("access") || s.contains("jewel") || s.contains("belt") || s.contains("hat") || s.contains("scarf") || s.contains("watch") || s.contains("necklace") || s.contains("ring"))
            return "Accessories";

        if (c.equals("Jackets") || c.equals("Pants") || c.equals("Tops")
                || c.equals("Bags") || c.equals("Shoes")
                || c.equals("Accessories") || c.equals("Other")) {
            return c;
        }

        return "Other";
    }
}

package com.example.fit4u;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PickItemActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category"; // "Tops" / "Pants" / "Shoes" / "Jackets"
    public static final String RESULT_DOC_ID = "docId";
    public static final String RESULT_CATEGORY = "category";
    public static final String RESULT_COLOR = "color";
    public static final String RESULT_IMAGE_URL = "imageUrl";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<ClothingItem> items = new ArrayList<>();
    private PickItemAdapter adapter;

    private String wantedCategory = "Tops";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_item);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        wantedCategory = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (wantedCategory == null || wantedCategory.trim().isEmpty()) wantedCategory = "Tops";

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Pick " + wantedCategory);

        RecyclerView rv = findViewById(R.id.rvPick);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new PickItemAdapter(items, item -> {
            Intent data = new Intent();
            data.putExtra(RESULT_DOC_ID, item.id);
            data.putExtra(RESULT_CATEGORY, item.category);
            data.putExtra(RESULT_COLOR, item.color);
            data.putExtra(RESULT_IMAGE_URL, item.imageUrl);
            setResult(RESULT_OK, data);
            finish();
        });

        rv.setAdapter(adapter);

        loadItems();
    }

    private void loadItems() {
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
                    items.clear();

                    for (QueryDocumentSnapshot doc : qs) {
                        String docId = doc.getId();
                        String rawCategory = doc.getString("category");
                        String color = doc.getString("color");
                        String imageUrl = doc.getString("imageUrl");

                        String normalized = normalizeCategory(rawCategory);

                        if (wantedCategory.equals(normalized)) {
                            items.add(new ClothingItem(docId, normalized, color, imageUrl));
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (items.isEmpty()) {
                        Toast.makeText(this, "No items in " + wantedCategory + " yet 😅", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // אותו normalize כמו אצלך – כדי שכל מיני "shirt" "tops" וכו' יתכנסו
    private String normalizeCategory(String c) {
        if (c == null) return "Other";

        String s = c.trim().toLowerCase();

        if (s.contains("jacket") || s.contains("coat") || s.contains("outer"))
            return "Jackets";

        if (s.contains("pant") || s.contains("jean") || s.contains("trouser"))
            return "Pants";

        if (s.contains("top") || s.contains("shirt") || s.contains("tee") || s.contains("tshirt"))
            return "Tops";

        if (s.contains("shoe") || s.contains("sneaker") || s.contains("heel") || s.contains("boot"))
            return "Shoes";

        if (c.equals("Jackets") || c.equals("Pants") || c.equals("Tops")
                || c.equals("Shoes") || c.equals("Other")) {
            return c;
        }

        return "Other";
    }
}

package com.example.fit4u;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);

        rvShelves = findViewById(R.id.rvShelves);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // קטגוריות קבועות לפי מה שביקשת
        categories.add("Bags");
        categories.add("Shoes");
        categories.add("Jackets");
        categories.add("Shirts");
        categories.add("Pants");
        categories.add("Skirts");
        categories.add("Dresses");
        categories.add("Other");

        for (String c : categories) data.put(c, new ArrayList<>());

        rvShelves.setLayoutManager(new LinearLayoutManager(this));
        rvShelves.setAdapter(new ShelfAdapter(categories, data));

        loadCloset();
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
                    // ניקוי
                    for (String c : categories) data.get(c).clear();

                    for (QueryDocumentSnapshot doc : qs) {
                        String category = doc.getString("category");
                        String color = doc.getString("color");
                        String imageUrl = doc.getString("imageUrl");

                        if (category == null) category = "Other";

                        // אם מישהו כתב קטגוריה שלא קיימת — נכניס ל-Other
                        if (!data.containsKey(category)) category = "Other";

                        data.get(category).add(new ClothingItem(category, color, imageUrl));
                    }

                    // רענון
                    rvShelves.getAdapter().notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

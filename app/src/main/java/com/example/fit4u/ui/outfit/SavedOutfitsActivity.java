package com.example.fit4u.ui.outfit;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit4u.R;
import com.example.fit4u.ui.model.SavedOutfit;
import com.example.fit4u.ui.adapter.SavedOutfitsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SavedOutfitsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private RecyclerView rv;
    private SavedOutfitsAdapter adapter;

    private final List<SavedOutfit> outfits = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_outfits);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rv = findViewById(R.id.rvSavedOutfits);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SavedOutfitsAdapter(outfits, outfit -> deleteOutfit(outfit));
        rv.setAdapter(adapter);

        loadOutfits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOutfits();
    }

    private void loadOutfits() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("savedOutfits")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    outfits.clear();
                    qs.getDocuments().forEach(doc -> {
                        SavedOutfit o = SavedOutfit.fromDoc(doc.getId(), doc);
                        outfits.add(o);
                    });
                    adapter.notifyDataSetChanged();

                    if (outfits.isEmpty()) {
                        Toast.makeText(this, "No saved outfits yet 💕", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void deleteOutfit(SavedOutfit outfit) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("savedOutfits")
                .document(outfit.id)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Deleted ✅", Toast.LENGTH_SHORT).show();
                    loadOutfits();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

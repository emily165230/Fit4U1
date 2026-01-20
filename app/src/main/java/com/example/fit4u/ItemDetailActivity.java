package com.example.fit4u;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ItemDetailActivity extends AppCompatActivity {

    private ImageView imgDetail;
    private EditText etCategory, etColor;
    private Button btnSave, btnDelete;
    private TextView tvStatus;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String docId;
    private String imageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        imgDetail = findViewById(R.id.imgDetail);
        etCategory = findViewById(R.id.etDetailCategory);
        etColor = findViewById(R.id.etDetailColor);
        btnSave = findViewById(R.id.btnSaveChanges);
        btnDelete = findViewById(R.id.btnDeleteItem);
        tvStatus = findViewById(R.id.tvDetailStatus);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        docId = getIntent().getStringExtra("docId");
        String category = getIntent().getStringExtra("category");
        String color = getIntent().getStringExtra("color");
        imageUrl = getIntent().getStringExtra("imageUrl");

        if (category != null) etCategory.setText(category);
        if (color != null) etColor.setText(color);

        Glide.with(this).load(imageUrl).centerCrop().into(imgDetail);

        btnSave.setOnClickListener(v -> saveChanges());
        btnDelete.setOnClickListener(v -> deleteItem());
    }

    private void saveChanges() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in ❌", Toast.LENGTH_SHORT).show();
            return;
        }
        if (docId == null || docId.trim().isEmpty()) {
            Toast.makeText(this, "Missing item id ❌", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String newCategory = etCategory.getText() == null ? "" : etCategory.getText().toString().trim();
        String newColor = etColor.getText() == null ? "" : etColor.getText().toString().trim();

        if (newCategory.isEmpty()) {
            tvStatus.setText("Please enter category");
            return;
        }
        if (newColor.isEmpty()) {
            tvStatus.setText("Please enter color");
            return;
        }

        btnSave.setEnabled(false);
        btnDelete.setEnabled(false);
        tvStatus.setText("Saving...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("category", newCategory);
        updates.put("color", newColor);

        db.collection("users")
                .document(uid)
                .collection("closet")
                .document(docId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Saved ✅", Toast.LENGTH_SHORT).show();
                    finish(); // ✅ סוגר מיד
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnDelete.setEnabled(true);
                    tvStatus.setText("Save failed: " + e.getMessage());
                });
    }

    private void deleteItem() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in ❌", Toast.LENGTH_SHORT).show();
            return;
        }
        if (docId == null || docId.trim().isEmpty()) {
            Toast.makeText(this, "Missing item id ❌", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        btnSave.setEnabled(false);
        btnDelete.setEnabled(false);
        tvStatus.setText("Deleting...");

        db.collection("users")
                .document(uid)
                .collection("closet")
                .document(docId)
                .delete()
                .addOnSuccessListener(v -> {
                    // delete image (best effort)
                    deleteStorageBestEffort();
                    Toast.makeText(this, "Deleted ✅", Toast.LENGTH_SHORT).show();
                    finish(); // ✅ סוגר מיד
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnDelete.setEnabled(true);
                    tvStatus.setText("Delete failed: " + e.getMessage());
                });
    }

    private void deleteStorageBestEffort() {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        try {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            ref.delete();
        } catch (Exception ignored) {}
    }
}

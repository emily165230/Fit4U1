package com.example.fit4u;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {


    private Button btnAddItem, btnCloset;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Dialog elements
    private AlertDialog addDialog;
    private ImageView dlgPreview;
    private EditText dlgType, dlgColor;
    private TextView dlgStatus;

    private Uri pickedImageUri;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImageUri = uri;
                    if (dlgPreview != null) dlgPreview.setImageURI(uri);
                    if (dlgStatus != null) dlgStatus.setText("Image selected ✅");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        btnAddItem = findViewById(R.id.btnAddItem);
        btnCloset  = findViewById(R.id.btnCloset);

        if (btnAddItem == null || btnCloset == null) {
            Toast.makeText(this, "Button id not found in activity_home.xml", Toast.LENGTH_LONG).show();
            return;
        }

        btnAddItem.setOnClickListener(v -> openAddItemDialog());

        btnCloset.setOnClickListener(v -> {
            Toast.makeText(this, "Closet clicked!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, ClosetActivity.class));
        });
    }


    private void openAddItemDialog() {
        pickedImageUri = null;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);

        dlgPreview = view.findViewById(R.id.imgPreview);
        Button btnPick = view.findViewById(R.id.btnPickImage);
        dlgType = view.findViewById(R.id.etType);
        dlgColor = view.findViewById(R.id.etColor);
        dlgStatus = view.findViewById(R.id.tvStatus);

        btnPick.setOnClickListener(v -> pickImage.launch("image/*"));

        addDialog = new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(view)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Save", null)
                .create();

        addDialog.setOnShowListener(d -> {
            Button saveBtn = addDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveBtn.setOnClickListener(v -> saveItemFromDialog());
        });

        addDialog.show();
    }

    private void saveItemFromDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            dlgStatus.setText("Not logged in ❌");
            return;
        }

        String type = dlgType.getText() == null ? "" : dlgType.getText().toString().trim();
        String color = dlgColor.getText() == null ? "" : dlgColor.getText().toString().trim();

        if (pickedImageUri == null) {
            dlgStatus.setText("Please choose an image");
            return;
        }
        if (type.isEmpty()) {
            dlgStatus.setText("Please enter type");
            return;
        }
        if (color.isEmpty()) {
            dlgStatus.setText("Please enter color");
            return;
        }

        dlgStatus.setText("Uploading...");

        String uid = user.getUid();
        String fileName = UUID.randomUUID().toString() + ".jpg";

        StorageReference ref = storage.getReference()
                .child("users")
                .child(uid)
                .child("closet")
                .child(fileName);

        ref.putFile(pickedImageUri)
                .addOnSuccessListener(ts ->
                        ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            saveToFirestore(uid, type, color, downloadUri.toString());
                        }).addOnFailureListener(e ->
                                dlgStatus.setText("URL error: " + e.getMessage()))
                )
                .addOnFailureListener(e ->
                        dlgStatus.setText("Upload failed: " + e.getMessage()));
    }

    private void saveToFirestore(String uid, String type, String color, String imageUrl) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", type); // type שדה בדיאלוג = קטגוריה
        item.put("color", color);
        item.put("imageUrl", imageUrl);
        item.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .collection("closet")
                .add(item)
                .addOnSuccessListener(doc -> {
                    dlgStatus.setText("Saved ✅");
                    if (addDialog != null) addDialog.dismiss();
                })
                .addOnFailureListener(e ->
                        dlgStatus.setText("Save failed: " + e.getMessage()));
    }

}


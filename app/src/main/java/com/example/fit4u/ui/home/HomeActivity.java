package com.example.fit4u.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit4u.ui.closet.ClosetActivity;
import com.example.fit4u.ui.outfit.OutfitTodayActivity;
import com.example.fit4u.R;
import com.example.fit4u.ui.outfit.SavedOutfitsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private Button btnAddItem, btnCloset;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private ImageView dlgPreview;
    private EditText dlgType, dlgColor;
    private TextView dlgStatus;

    private Uri pickedImageUri;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // timeout
    private static final long UPLOAD_TIMEOUT_MS = 25000L;
    private Runnable timeoutRunnable;

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

        findViewById(R.id.btnToday).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, OutfitTodayActivity.class))
        );

        // ✅ NEW: Saved outfits
        findViewById(R.id.btnSavedOutfits).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SavedOutfitsActivity.class))
        );

        btnAddItem.setOnClickListener(v -> openAddItemDialog());

        btnCloset.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ClosetActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent i = new Intent(HomeActivity.this,
                    com.example.fit4u.ui.auth.MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            finishAffinity();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
        cancelTimeout();
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

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(view)
                .setNegativeButton("Cancel", (d, w) -> {
                    hideKeyboard(dlgColor);
                    cancelTimeout();
                    d.dismiss();
                })
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveBtn.setOnClickListener(v -> saveItemFromDialog(dialog, saveBtn));
        });

        dialog.show();
    }

    private void saveItemFromDialog(AlertDialog dialog, Button saveBtn) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            dlgStatus.setText("Not logged in ❌");
            return;
        }

        String type = dlgType.getText() == null ? "" : dlgType.getText().toString().trim();
        String color = dlgColor.getText() == null ? "" : dlgColor.getText().toString().trim();

        if (pickedImageUri == null) { dlgStatus.setText("Please choose an image"); return; }
        if (type.isEmpty()) { dlgStatus.setText("Please enter category"); return; }
        if (color.isEmpty()) { dlgStatus.setText("Please enter color"); return; }

        hideKeyboard(dlgColor);

        saveBtn.setEnabled(false);
        dlgStatus.setText("Preparing image...");

        startTimeout(dialog, saveBtn);

        String uid = user.getUid();
        String fileName = UUID.randomUUID().toString() + ".jpg";

        StorageReference ref = storage.getReference()
                .child("users")
                .child(uid)
                .child("closet")
                .child(fileName);

        ioExecutor.execute(() -> {
            Uri uploadUri;
            try {
                uploadUri = copyToCacheFile(pickedImageUri);
            } catch (Exception ex) {
                mainHandler.post(() -> {
                    cancelTimeout();
                    saveBtn.setEnabled(true);
                    dlgStatus.setText("Image read error: " + ex.getMessage());
                });
                return;
            }

            mainHandler.post(() -> dlgStatus.setText("Uploading..."));

            ref.putFile(uploadUri)
                    .addOnSuccessListener(ts -> ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                mainHandler.post(() -> dlgStatus.setText("Saving..."));

                                saveToFirestore(uid, type, color, downloadUri.toString(),
                                        () -> {
                                            mainHandler.post(() -> {
                                                cancelTimeout();

                                                Toast.makeText(HomeActivity.this, "Saved ✅", Toast.LENGTH_SHORT).show();

                                                setResult(RESULT_OK);

                                                try { hideKeyboard(dlgColor); } catch (Exception ignored) {}
                                                try { if (dialog.isShowing()) dialog.dismiss(); } catch (Exception ignored) {}
                                                try { dialog.cancel(); } catch (Exception ignored) {}

                                                mainHandler.postDelayed(() -> {
                                                    try { if (dialog.isShowing()) dialog.dismiss(); } catch (Exception ignored) {}
                                                }, 200);
                                            });
                                        },
                                        err -> mainHandler.post(() -> {
                                            cancelTimeout();
                                            saveBtn.setEnabled(true);
                                            dlgStatus.setText("Save failed: " + err);
                                        })
                                );
                            })
                            .addOnFailureListener(e -> mainHandler.post(() -> {
                                cancelTimeout();
                                saveBtn.setEnabled(true);
                                dlgStatus.setText("URL error: " + e.getMessage());
                            })))
                    .addOnFailureListener(e -> mainHandler.post(() -> {
                        cancelTimeout();
                        saveBtn.setEnabled(true);
                        dlgStatus.setText("Upload failed: " + e.getMessage());
                    }));
        });
    }

    private void startTimeout(AlertDialog dialog, Button saveBtn) {
        cancelTimeout();
        timeoutRunnable = () -> {
            try {
                if (dlgStatus != null) dlgStatus.setText("Still working... check internet / rules, then try again.");
                if (saveBtn != null) saveBtn.setEnabled(true);
                Toast.makeText(HomeActivity.this, "Taking too long ⏳", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        };
        mainHandler.postDelayed(timeoutRunnable, UPLOAD_TIMEOUT_MS);
    }

    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private interface Ok { void run(); }
    private interface Fail { void run(String msg); }

    private void saveToFirestore(String uid, String type, String color, String imageUrl, Ok ok, Fail fail) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", type);
        item.put("color", color);
        item.put("imageUrl", imageUrl);
        item.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .collection("closet")
                .add(item)
                .addOnSuccessListener(doc -> ok.run())
                .addOnFailureListener(e -> fail.run(e.getMessage()));
    }

    private Uri copyToCacheFile(Uri sourceUri) throws Exception {
        InputStream in = getContentResolver().openInputStream(sourceUri);
        if (in == null) throw new Exception("Cannot open selected image");

        File outFile = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
        OutputStream out = new FileOutputStream(outFile);

        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);

        in.close();
        out.close();

        return Uri.fromFile(outFile);
    }

    private void hideKeyboard(View v) {
        if (v == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}

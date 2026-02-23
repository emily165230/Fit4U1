package com.example.fit4u.ui.closet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fit4u.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ItemDetailFragment extends Fragment {

    private static final String ARG_DOC_ID = "arg_doc_id";
    private static final String ARG_CATEGORY = "arg_category";
    private static final String ARG_COLOR = "arg_color";
    private static final String ARG_IMAGE_URL = "arg_image_url";

    private ImageView imgDetail;
    private EditText etCategory, etColor;
    private Button btnSave, btnDelete;
    private TextView tvStatus;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String docId;
    private String imageUrl;

    public ItemDetailFragment() {}

    public static ItemDetailFragment newInstance(@Nullable String docId,
                                                 @Nullable String category,
                                                 @Nullable String color,
                                                 @Nullable String imageUrl) {
        ItemDetailFragment f = new ItemDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_DOC_ID, docId);
        b.putString(ARG_CATEGORY, category);
        b.putString(ARG_COLOR, color);
        b.putString(ARG_IMAGE_URL, imageUrl);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // משתמשים באותו XML שהיה למסך (הקיים אצלך)
        return inflater.inflate(R.layout.activity_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imgDetail = view.findViewById(R.id.imgDetail);
        etCategory = view.findViewById(R.id.etDetailCategory);
        etColor = view.findViewById(R.id.etDetailColor);
        btnSave = view.findViewById(R.id.btnSaveChanges);
        btnDelete = view.findViewById(R.id.btnDeleteItem);
        tvStatus = view.findViewById(R.id.tvDetailStatus);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().finish());

        Bundle args = getArguments();
        if (args != null) {
            docId = args.getString(ARG_DOC_ID);
            String category = args.getString(ARG_CATEGORY);
            String color = args.getString(ARG_COLOR);
            imageUrl = args.getString(ARG_IMAGE_URL);

            if (category != null) etCategory.setText(category);
            if (color != null) etColor.setText(color);

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(imgDetail);
            }
        }

        btnSave.setOnClickListener(v -> saveChanges());
        btnDelete.setOnClickListener(v -> deleteItem());
    }

    private void saveChanges() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Not logged in ❌", Toast.LENGTH_SHORT).show();
            return;
        }
        if (docId == null || docId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Missing item id ❌", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Saved ✅", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnDelete.setEnabled(true);
                    tvStatus.setText("Save failed: " + e.getMessage());
                });
    }

    private void deleteItem() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Not logged in ❌", Toast.LENGTH_SHORT).show();
            return;
        }
        if (docId == null || docId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Missing item id ❌", Toast.LENGTH_SHORT).show();
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
                    deleteStorageBestEffort();
                    Toast.makeText(requireContext(), "Deleted ✅", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnDelete.setEnabled(true);
                    tvStatus.setText("Delete failed: " + e.getMessage());
                });
    }

    private void deleteStorageBestEffort() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return;
        try {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            ref.delete();
        } catch (Exception ignored) {}
    }
}
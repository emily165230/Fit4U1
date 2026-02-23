package com.example.fit4u.ui.pick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit4u.R;
import com.example.fit4u.ui.adapter.PickItemAdapter;
import com.example.fit4u.ui.model.ClothingItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PickItemFragment extends Fragment {

    public static final String REQUEST_KEY = "pick_item_result";

    public static final String BUNDLE_DOC_ID = "docId";
    public static final String BUNDLE_CATEGORY = "category";
    public static final String BUNDLE_COLOR = "color";
    public static final String BUNDLE_IMAGE_URL = "imageUrl";

    private static final String ARG_WANTED_CATEGORY = "arg_wanted_category";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<ClothingItem> items = new ArrayList<>();
    private PickItemAdapter adapter;

    private String wantedCategory = "Tops";

    public PickItemFragment() {}

    public static PickItemFragment newInstance(@Nullable String wantedCategory) {
        PickItemFragment f = new PickItemFragment();
        Bundle b = new Bundle();
        b.putString(ARG_WANTED_CATEGORY, wantedCategory);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // משתמשים באותו XML שהיה למסך
        return inflater.inflate(R.layout.activity_pick_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        wantedCategory = (args != null) ? args.getString(ARG_WANTED_CATEGORY) : null;
        if (wantedCategory == null || wantedCategory.trim().isEmpty()) wantedCategory = "Tops";

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().finish());

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Pick " + wantedCategory);

        RecyclerView rv = view.findViewById(R.id.rvPick);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new PickItemAdapter(items, item -> {
            Bundle result = new Bundle();
            result.putString(BUNDLE_DOC_ID, item.id);
            result.putString(BUNDLE_CATEGORY, item.category);
            result.putString(BUNDLE_COLOR, item.color);
            result.putString(BUNDLE_IMAGE_URL, item.imageUrl);

            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        });

        rv.setAdapter(adapter);

        loadItems();
    }

    private void loadItems() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
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
                        Toast.makeText(requireContext(),
                                "No items in " + wantedCategory + " yet 😅",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Load failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

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
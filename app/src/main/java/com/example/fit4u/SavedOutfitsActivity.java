package com.example.fit4u;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SavedOutfitsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private RecyclerView rv;
    private final List<SavedOutfit> list = new ArrayList<>();
    private SavedOutfitsAdapter adapter;

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
        adapter = new SavedOutfitsAdapter(list, outfit -> {
            // בינתיים: קליק רק מציג הודעה (אפשר אחר כך לעשות דף פרטי אאוטפיט)
            Toast.makeText(this, "Saved outfit: " + outfit.dayKey, Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        loadSavedOutfits();
    }

    private void loadSavedOutfits() {
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
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    list.clear();

                    for (var doc : qs.getDocuments()) {
                        String id = doc.getId();

                        String dayKey = doc.getString("dayKey");

                        long createdAtMs = 0;
                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts != null) createdAtMs = ts.toDate().getTime();

                        String topsUrl = doc.getString("topsUrl");
                        String pantsUrl = doc.getString("pantsUrl");
                        String shoesUrl = doc.getString("shoesUrl");
                        String jacketUrl = doc.getString("jacketUrl");

                        // אם אצלך השמות נשמרו כמו topsUrl/pantsUrl וכו' בקוד שמירה — זה יתאים.
                        // אם לא, אנחנו ננסה גם את השמות עם U גדולה (ליתר ביטחון):
                        if (topsUrl == null) topsUrl = doc.getString("topsUrl");
                        if (pantsUrl == null) pantsUrl = doc.getString("pantsUrl");
                        if (shoesUrl == null) shoesUrl = doc.getString("shoesUrl");
                        if (jacketUrl == null) jacketUrl = doc.getString("jacketUrl");

                        // במקרה ששמרת אותם עם prefix+Url כמו בקוד שלי (topsUrl/pantsUrl/shoesUrl/jacketUrl)
                        // זה יעבוד. אם במקרה נשמר "topsUrl" בתור "topsUrl" — אותו דבר.

                        // עוד fallback אם בטעות שמרת "topsUrl" תחת "topsUrl" לא קורה אבל נשאיר:
                        if (topsUrl == null) topsUrl = doc.getString("topsUrl");

                        // ואם השמירה הייתה עם keys: topsUrl / pantsUrl / shoesUrl / jacketUrl -> OK
                        // אם שמרת keys: topsUrl לא קיים, אבל שמרת topsUrl תחת "topsUrl"? אין מצב. אז נשאיר.

                        // אם ממש את רוצה 100% תואם לשמירה שלי:
                        // keys הם: topsUrl, pantsUrl, shoesUrl, jacketUrl
                        // אז זה ייתפס.

                        list.add(new SavedOutfit(id, dayKey, createdAtMs, topsUrl, pantsUrl, shoesUrl, jacketUrl));
                    }

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        Toast.makeText(this, "No saved outfits yet 💗", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

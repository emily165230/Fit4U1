package com.example.fit4u.ui.outfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fit4u.R;
import com.example.fit4u.ui.model.ClothingItem;
import com.example.fit4u.ui.pick.PickItemActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class OutfitTodayActivity extends AppCompatActivity {

    private static final String PREFS = "fit4u_daily_outfit";
    private static final String K_DAY = "dayKey";

    private ImageView imgTops, imgPants, imgShoes, imgJacket;
    private Button btnShuffle, btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final Random rnd = new Random();

    private final Map<String, List<ClothingItem>> closetByCategory = new HashMap<>();

    private ClothingItem pickTops, pickPants, pickShoes, pickJacket;

    private String pendingSlot = null;
    private boolean isSaving = false;

    private final ActivityResultLauncher<Intent> pickLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                String docId = result.getData().getStringExtra(PickItemActivity.RESULT_DOC_ID);
                String category = result.getData().getStringExtra(PickItemActivity.RESULT_CATEGORY);
                String color = result.getData().getStringExtra(PickItemActivity.RESULT_COLOR);
                String imageUrl = result.getData().getStringExtra(PickItemActivity.RESULT_IMAGE_URL);

                if (docId == null || imageUrl == null) return;

                ClothingItem picked = new ClothingItem(docId, category, color, imageUrl);

                if ("Tops".equals(pendingSlot)) {
                    pickTops = picked;
                    showItem(imgTops, pickTops);
                } else if ("Pants".equals(pendingSlot)) {
                    pickPants = picked;
                    showItem(imgPants, pickPants);
                } else if ("Shoes".equals(pendingSlot)) {
                    pickShoes = picked;
                    showItem(imgShoes, pickShoes);
                } else if ("Jackets".equals(pendingSlot)) {
                    pickJacket = picked;
                    showItem(imgJacket, pickJacket);
                }

                pendingSlot = null;

                // ✅ נשמור את הבחירה הידנית כדי שלא יתחלף בכניסה מחדש
                saveDailyOutfitToPrefs();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_today);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        imgTops = findViewById(R.id.imgTops);
        imgPants = findViewById(R.id.imgPants);
        imgShoes = findViewById(R.id.imgShoes);
        imgJacket = findViewById(R.id.imgJacket);

        btnShuffle = findViewById(R.id.btnShuffle);
        btnSave = findViewById(R.id.btnSave);

        btnShuffle.setOnClickListener(v -> {
            // Shuffle אמיתי: טוען ארון אם צריך ואז מגריל + שומר להיום
            if (closetByCategory.isEmpty()) {
                loadClosetThenShuffle(true);
            } else {
                shuffleFromLoadedCloset(true);
            }
        });

        btnSave.setOnClickListener(v -> saveOutfit());

        findViewById(R.id.btnChangeTops).setOnClickListener(v -> openPicker("Tops"));
        findViewById(R.id.btnChangePants).setOnClickListener(v -> openPicker("Pants"));
        findViewById(R.id.btnChangeShoes).setOnClickListener(v -> openPicker("Shoes"));
        findViewById(R.id.btnChangeJacket).setOnClickListener(v -> openPicker("Jackets"));

        // ✅ קודם ננסה לטעון אאוטפיט של היום (כדי שלא ישתנה כל כניסה)
        boolean loaded = loadDailyOutfitFromPrefs();
        if (!loaded) {
            // אין להיום -> נטען ארון ונגריל פעם ראשונה + נשמור להיום
            loadClosetThenShuffle(true);
        } else {
            // יש להיום -> רק נוודא שיש לנו ארון מוכן כדי ש-Shuffle יעבוד מהר
            loadClosetThenShuffle(false);
        }
    }

    private void openPicker(String category) {
        pendingSlot = category;
        Intent i = new Intent(this, PickItemActivity.class);
        i.putExtra(PickItemActivity.EXTRA_CATEGORY, category);
        pickLauncher.launch(i);
    }

    /**
     * @param doShuffle האם בסיום הטעינה לעשות Shuffle
     */
    private void loadClosetThenShuffle(boolean doShuffle) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        closetByCategory.clear();
        closetByCategory.put("Tops", new ArrayList<>());
        closetByCategory.put("Pants", new ArrayList<>());
        closetByCategory.put("Shoes", new ArrayList<>());
        closetByCategory.put("Jackets", new ArrayList<>());
        closetByCategory.put("Other", new ArrayList<>());

        db.collection("users")
                .document(uid)
                .collection("closet")
                .get()
                .addOnSuccessListener(qs -> {
                    for (QueryDocumentSnapshot doc : qs) {
                        String docId = doc.getId();
                        String rawCategory = doc.getString("category");
                        String color = doc.getString("color");
                        String imageUrl = doc.getString("imageUrl");

                        String category = normalizeCategory(rawCategory);
                        if (!closetByCategory.containsKey(category)) category = "Other";

                        List<ClothingItem> list = closetByCategory.get(category);
                        if (list != null) {
                            list.add(new ClothingItem(docId, category, color, imageUrl));
                        }
                    }

                    if (doShuffle) shuffleFromLoadedCloset(true);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load closet failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    /**
     * @param saveToPrefs אם true -> נשמור את התוצאה כ-Outfit של היום
     */
    private void shuffleFromLoadedCloset(boolean saveToPrefs) {
        if (totalItems() == 0) {
            Toast.makeText(this, "Your closet is empty 😅 Add items first", Toast.LENGTH_LONG).show();
            setPlaceholder(imgTops);
            setPlaceholder(imgPants);
            setPlaceholder(imgShoes);
            setPlaceholder(imgJacket);
            pickTops = pickPants = pickShoes = pickJacket = null;
            clearDailyPrefs();
            return;
        }

        pickTops = randomFrom("Tops");
        pickPants = randomFrom("Pants");
        pickShoes = randomFrom("Shoes");
        pickJacket = randomFrom("Jackets");

        showItem(imgTops, pickTops);
        showItem(imgPants, pickPants);
        showItem(imgShoes, pickShoes);
        showItem(imgJacket, pickJacket);

        if (saveToPrefs) saveDailyOutfitToPrefs();

        Toast.makeText(this, "New outfit ✨", Toast.LENGTH_SHORT).show();
    }

    private int totalItems() {
        int sum = 0;
        for (List<ClothingItem> list : closetByCategory.values()) {
            if (list != null) sum += list.size();
        }
        return sum;
    }

    private ClothingItem randomFrom(String category) {
        List<ClothingItem> list = closetByCategory.get(category);
        if (list == null || list.isEmpty()) return null;
        return list.get(rnd.nextInt(list.size()));
    }

    private void showItem(ImageView target, ClothingItem item) {
        if (item == null || item.imageUrl == null || item.imageUrl.trim().isEmpty()) {
            setPlaceholder(target);
            return;
        }

        Glide.with(this)
                .load(item.imageUrl)
                .centerCrop()
                .into(target);
    }

    private void setPlaceholder(ImageView target) {
        target.setImageResource(R.drawable.placeholder_item);
    }

    // ✅ שמירת outfit ל-Firestore (כבר היה אצלך)
    private void saveOutfit() {
        if (isSaving) return;

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pickTops == null || pickPants == null || pickShoes == null) {
            Toast.makeText(this, "Choose at least Tops + Pants + Shoes", Toast.LENGTH_LONG).show();
            return;
        }

        isSaving = true;
        btnSave.setEnabled(false);

        String uid = user.getUid();

        Map<String, Object> outfit = new HashMap<>();
        outfit.put("createdAt", Timestamp.now());
        outfit.put("dayKey", getTodayKey());

        putItem(outfit, "tops", pickTops);
        putItem(outfit, "pants", pickPants);
        putItem(outfit, "shoes", pickShoes);
        putItem(outfit, "jacket", pickJacket);

        db.collection("users")
                .document(uid)
                .collection("savedOutfits")
                .add(outfit)
                .addOnSuccessListener(ref -> {
                    isSaving = false;
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Outfit saved ✅", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OutfitTodayActivity.this, SavedOutfitsActivity.class));
                })
                .addOnFailureListener(e -> {
                    isSaving = false;
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void putItem(Map<String, Object> map, String prefix, ClothingItem item) {
        if (item == null) return;
        map.put(prefix + "Id", item.id);
        map.put(prefix + "Category", item.category);
        map.put(prefix + "Color", item.color);
        map.put(prefix + "Url", item.imageUrl);
    }

    private String getTodayKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new java.util.Date());
    }

    // -------------------------
    // ✅ DAILY OUTFIT PREFS
    // -------------------------

    private boolean loadDailyOutfitFromPrefs() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String day = sp.getString(K_DAY, null);
        String today = getTodayKey();
        if (day == null || !day.equals(today)) return false;

        // חובה מינימום (tops+pants+shoes)
        String topsUrl = sp.getString("topsUrl", null);
        String pantsUrl = sp.getString("pantsUrl", null);
        String shoesUrl = sp.getString("shoesUrl", null);

        String topsId = sp.getString("topsId", null);
        String pantsId = sp.getString("pantsId", null);
        String shoesId = sp.getString("shoesId", null);

        if (topsId == null || pantsId == null || shoesId == null) return false;
        if (topsUrl == null || pantsUrl == null || shoesUrl == null) return false;

        pickTops = new ClothingItem(topsId,
                sp.getString("topsCategory", "Tops"),
                sp.getString("topsColor", ""),
                topsUrl);

        pickPants = new ClothingItem(pantsId,
                sp.getString("pantsCategory", "Pants"),
                sp.getString("pantsColor", ""),
                pantsUrl);

        pickShoes = new ClothingItem(shoesId,
                sp.getString("shoesCategory", "Shoes"),
                sp.getString("shoesColor", ""),
                shoesUrl);

        // jacket אופציונלי
        String jacketId = sp.getString("jacketId", null);
        String jacketUrl = sp.getString("jacketUrl", null);
        if (jacketId != null && jacketUrl != null) {
            pickJacket = new ClothingItem(jacketId,
                    sp.getString("jacketCategory", "Jackets"),
                    sp.getString("jacketColor", ""),
                    jacketUrl);
        } else {
            pickJacket = null;
        }

        showItem(imgTops, pickTops);
        showItem(imgPants, pickPants);
        showItem(imgShoes, pickShoes);
        showItem(imgJacket, pickJacket);

        return true;
    }

    private void saveDailyOutfitToPrefs() {
        // נשמור רק אם יש מינימום הגיוני
        if (pickTops == null || pickPants == null || pickShoes == null) return;

        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();

        ed.putString(K_DAY, getTodayKey());

        putPrefItem(ed, "tops", pickTops);
        putPrefItem(ed, "pants", pickPants);
        putPrefItem(ed, "shoes", pickShoes);

        if (pickJacket != null) putPrefItem(ed, "jacket", pickJacket);
        else {
            ed.remove("jacketId");
            ed.remove("jacketCategory");
            ed.remove("jacketColor");
            ed.remove("jacketUrl");
        }

        ed.apply();
    }

    private void putPrefItem(SharedPreferences.Editor ed, String prefix, ClothingItem item) {
        ed.putString(prefix + "Id", item.id);
        ed.putString(prefix + "Category", item.category);
        ed.putString(prefix + "Color", item.color);
        ed.putString(prefix + "Url", item.imageUrl);
    }

    private void clearDailyPrefs() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
    }

    // -------------------------

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

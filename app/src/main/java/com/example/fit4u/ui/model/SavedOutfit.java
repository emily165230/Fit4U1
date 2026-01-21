package com.example.fit4u.ui.model;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

public class SavedOutfit {
    public String id;

    public String dayKey;
    public Timestamp createdAt;

    public String topsUrl, pantsUrl, shoesUrl, jacketUrl;

    public static SavedOutfit fromDoc(String id, DocumentSnapshot doc) {
        SavedOutfit o = new SavedOutfit();
        o.id = id;

        o.dayKey = doc.getString("dayKey");
        o.createdAt = doc.getTimestamp("createdAt");

        o.topsUrl = doc.getString("topsUrl");
        o.pantsUrl = doc.getString("pantsUrl");
        o.shoesUrl = doc.getString("shoesUrl");
        o.jacketUrl = doc.getString("jacketUrl");

        return o;
    }
}

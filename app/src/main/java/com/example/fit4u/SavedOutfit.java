package com.example.fit4u;

public class SavedOutfit {
    public String id;

    public String dayKey;      // yyyy-MM-dd
    public long createdAtMs;   // timestamp in ms (נמיר מ-Firestore)

    public String topsUrl;
    public String pantsUrl;
    public String shoesUrl;
    public String jacketUrl;   // יכול להיות null

    public SavedOutfit() {}

    public SavedOutfit(String id, String dayKey, long createdAtMs,
                       String topsUrl, String pantsUrl, String shoesUrl, String jacketUrl) {
        this.id = id;
        this.dayKey = dayKey;
        this.createdAtMs = createdAtMs;
        this.topsUrl = topsUrl;
        this.pantsUrl = pantsUrl;
        this.shoesUrl = shoesUrl;
        this.jacketUrl = jacketUrl;
    }
}

package com.example.fit4u.ui.model;

public class ClothingItem {
    public String id;       // Firestore document id
    public String category;
    public String color;
    public String imageUrl;

    public ClothingItem() {}

    public ClothingItem(String id, String category, String color, String imageUrl) {
        this.id = id;
        this.category = category;
        this.color = color;
        this.imageUrl = imageUrl;
    }
}

package com.example.fit4u.ui.closet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fit4u.R;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        if (savedInstanceState == null) {

            String docId = getIntent().getStringExtra("docId");
            String category = getIntent().getStringExtra("category");
            String color = getIntent().getStringExtra("color");
            String imageUrl = getIntent().getStringExtra("imageUrl");

            ItemDetailFragment fragment =
                    ItemDetailFragment.newInstance(docId, category, color, imageUrl);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
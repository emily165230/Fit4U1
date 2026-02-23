package com.example.fit4u.ui.pick;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit4u.R;

public class PickItemActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category"; // "Tops" / "Pants" / "Shoes" / "Jackets"
    public static final String RESULT_DOC_ID = "docId";
    public static final String RESULT_CATEGORY = "category";
    public static final String RESULT_COLOR = "color";
    public static final String RESULT_IMAGE_URL = "imageUrl";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        // מקשיבים לתוצאה מה-Fragment ומחזירים Result כמו שהיה קודם
        getSupportFragmentManager().setFragmentResultListener(
                PickItemFragment.REQUEST_KEY,
                this,
                (requestKey, bundle) -> {
                    Intent data = new Intent();
                    data.putExtra(RESULT_DOC_ID, bundle.getString(PickItemFragment.BUNDLE_DOC_ID));
                    data.putExtra(RESULT_CATEGORY, bundle.getString(PickItemFragment.BUNDLE_CATEGORY));
                    data.putExtra(RESULT_COLOR, bundle.getString(PickItemFragment.BUNDLE_COLOR));
                    data.putExtra(RESULT_IMAGE_URL, bundle.getString(PickItemFragment.BUNDLE_IMAGE_URL));
                    setResult(RESULT_OK, data);
                    finish();
                }
        );

        if (savedInstanceState == null) {
            String wantedCategory = getIntent().getStringExtra(EXTRA_CATEGORY);
            PickItemFragment fragment = PickItemFragment.newInstance(wantedCategory);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
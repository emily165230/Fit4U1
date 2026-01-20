package com.example.fit4u;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OutfitTodayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_today);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnShuffle = findViewById(R.id.btnShuffle);
        Button btnSave = findViewById(R.id.btnSave);

        btnShuffle.setOnClickListener(v ->
                Toast.makeText(this, "Shuffle (next step)", Toast.LENGTH_SHORT).show()
        );

        btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Save Outfit (next step)", Toast.LENGTH_SHORT).show()
        );

        // Change buttons - עכשיו רק הודעה. בשלב הבא נחבר לבחירה מהארון.
        findViewById(R.id.btnChangeTops).setOnClickListener(v ->
                Toast.makeText(this, "Change Tops (next step)", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.btnChangePants).setOnClickListener(v ->
                Toast.makeText(this, "Change Pants (next step)", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.btnChangeShoes).setOnClickListener(v ->
                Toast.makeText(this, "Change Shoes (next step)", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.btnChangeJacket).setOnClickListener(v ->
                Toast.makeText(this, "Change Jacket (next step)", Toast.LENGTH_SHORT).show()
        );
    }
}

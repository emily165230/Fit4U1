package com.example.fit4u;

import android.content.Intent;   // ← שורה חשובה: מאפשרת לעבור בין מסכים
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView signupText = findViewById(R.id.signupText);
        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        Button btnSkip = findViewById(R.id.btnSkipIntro);
        btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, IntroVideoActivity.class));
            finish(); // שלא יחזור למסך ה-Sign In בלחיצה אחורה
        });


    }
}

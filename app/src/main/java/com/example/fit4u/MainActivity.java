package com.example.fit4u;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // תופסים את תיבת הטקסט (EditText) ואת שדה התצוגה (TextView) מה־XML

        EditText input = findViewById(R.id.inputText);
        TextView output = findViewById(R.id.outputText);

        // מאזינים לשינויים בתיבת הטקסט כדי לעדכן את התצוגה בכל פעם שמשהו נכתב

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                output.setText(s);
                // מציג את מה שנכתב בתיבת הטקסט בזמן אמת על המסך
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}

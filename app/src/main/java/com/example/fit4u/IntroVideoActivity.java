package com.example.fit4u;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class IntroVideoActivity extends AppCompatActivity {

    // פונקציית onCreate — נקראת כשהמסך נטען בפעם הראשונה
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // קובע איזה עיצוב (layout) נטען למסך הזה
        setContentView(R.layout.activity_intro_video);

        // מאתרים את רכיב ה-VideoView מה-XML לפי ה-id שלו
        VideoView vv = findViewById(R.id.video);

        // יוצרים כתובת URI שמפנה לקובץ הווידאו מתוך התיקייה res/raw
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.homepagevideo);

        // מחברים את כתובת הווידאו לרכיב כדי שיוכל לנגן אותו
        vv.setVideoURI(uri);

        // מאזין שנקרא ברגע שהווידאו מוכן לניגון
        vv.setOnPreparedListener(mp -> {
            mp.setVolume(0f, 0f); // השתקת אודיו ליתר ביטחון (אם קיים)
            vv.start(); // מתחיל לנגן את הסרטון
        });

        // מאזין לסיום הווידאו — כשמגיעים לסוף הסרטון
        vv.setOnCompletionListener(mp -> {
            // מעבר אוטומטי למסך הבית (HomeActivity)
            startActivity(new Intent(IntroVideoActivity.this, HomeActivity.class));
            // סגירת המסך הנוכחי כדי שלא יוכלו לחזור אליו בלחיצה על Back
            finish();
            // אנימציית מעבר רכה (fade in/out)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // טיפול במצב של שגיאה בהפעלת הסרטון
        vv.setOnErrorListener((mp, what, extra) -> {
            // במקרה של שגיאה — לעבור ישר למסך הבית
            startActivity(new Intent(IntroVideoActivity.this, HomeActivity.class));
            finish();
            return true; // מחזירים true כדי לציין שטיפלנו בשגיאה
        });

    }
}

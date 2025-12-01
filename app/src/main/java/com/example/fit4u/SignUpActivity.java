package com.example.fit4u;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * SignUpActivity
 * רושם משתמש חדש ב-FirebaseAuth עם אימייל+סיסמה,
 * מעדכן תצוגת שם (displayName), ואז מפעיל את סרטון הפתיחה ועובר לבית.
 */
public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText etEmail, etPassword, etConfirm, etUsername;
    private Button btnSignup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView back = findViewById(R.id.backToLogin);
        back.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        });

        // 1) הפניה לרכיבים
        etEmail    = findViewById(R.id.emailEditText);
        etPassword = findViewById(R.id.passwordEditText);
        etConfirm  = findViewById(R.id.confirmPasswordEditText);
        etUsername = findViewById(R.id.usernameEditText);
        btnSignup  = findViewById(R.id.signupButton);

        auth = FirebaseAuth.getInstance();

        // 2) לוגיקת הרשמה
        btnSignup.setOnClickListener(v -> trySignUp());
    }

    private void trySignUp() {
        String email    = safe(etEmail);
        String pass     = safe(etPassword);
        String confirm  = safe(etConfirm);
        String username = safe(etUsername);

        // בדיקות בסיס
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("אימייל לא תקין"); etEmail.requestFocus(); return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("סיסמה חייבת להיות 6 תווים ומעלה"); etPassword.requestFocus(); return;
        }
        if (!pass.equals(confirm)) {
            etConfirm.setError("הסיסמאות לא תואמות"); etConfirm.requestFocus(); return;
        }
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("שם משתמש נדרש"); etUsername.requestFocus(); return;
        }

        btnSignup.setEnabled(false);

        // יצירת משתמש
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                btnSignup.setEnabled(true);
                String msg = task.getException() != null ? task.getException().getMessage() : "Sign up failed";
                Toast.makeText(this, "שגיאה בהרשמה: " + msg, Toast.LENGTH_LONG).show();
                return;
            }

            // עדכון displayName (לא חובה, אבל נחמד)
            if (auth.getCurrentUser() != null) {
                UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();
                auth.getCurrentUser().updateProfile(req).addOnCompleteListener(upt -> {
                    // בין אם הצליח או לא — ממשיכים לסרטון ולבית
                    goToIntroVideo();
                });
            } else {
                goToIntroVideo();
            }
        });
    }

    private void goToIntroVideo() {
        Toast.makeText(this, "נרשמת בהצלחה ✨", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignUpActivity.this, IntroVideoActivity.class));
        finish();
    }

    private static String safe(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

}

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

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        etEmail       = findViewById(R.id.usernameEditText); // עכשיו זה שדה אימייל
        etPassword    = findViewById(R.id.passwordEditText);
        btnLogin      = findViewById(R.id.loginButton);
        tvSignupLink  = findViewById(R.id.signupText);

        btnLogin.setOnClickListener(v -> tryLogin());

        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignUpActivity.class))
        );
    }

    private void tryLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("נא להכניס אימייל תקין");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("נא להכניס סיסמה");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            btnLogin.setEnabled(true);

            if (!task.isSuccessful()) {
                String msg = task.getException() != null ? task.getException().getMessage() : "Login failed";
                Toast.makeText(this, "שגיאה בהתחברות: " + msg, Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, "מחוברת 💖", Toast.LENGTH_SHORT).show();
            goToIntroVideo();
        });
    }

    private void goToIntroVideo() {
        startActivity(new Intent(MainActivity.this, IntroVideoActivity.class));
        finish();
    }
}

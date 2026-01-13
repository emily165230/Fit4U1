package com.example.fit4u;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink;

    private AuthViewModel vm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // אם כבר מחוברת -> לדלג קדימה
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            goToIntroVideo();
            return;
        }

        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.usernameEditText); // אצלך זה אימייל
        etPassword = findViewById(R.id.passwordEditText);
        btnLogin = findViewById(R.id.loginButton);
        tvSignupLink = findViewById(R.id.signupText);

        vm = new ViewModelProvider(this).get(AuthViewModel.class);

        vm.getState().observe(this, ui -> {
            btnLogin.setEnabled(!ui.loading);

            if (ui.message != null) {
                Toast.makeText(this, ui.message, Toast.LENGTH_LONG).show();
            }

            if (ui.success) {
                goToIntroVideo();
            }
        });

        btnLogin.setOnClickListener(v ->
                vm.login(getText(etEmail), getText(etPassword))
        );

        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignUpActivity.class))
        );
    }

    private void goToIntroVideo() {
        startActivity(new Intent(MainActivity.this, IntroVideoActivity.class));
        finish();
    }

    private String getText(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}

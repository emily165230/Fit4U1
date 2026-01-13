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

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirm, etUsername;
    private Button btnSignup;

    private AuthViewModel vm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView back = findViewById(R.id.backToLogin);
        back.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        etEmail = findViewById(R.id.emailEditText);
        etPassword = findViewById(R.id.passwordEditText);
        etConfirm = findViewById(R.id.confirmPasswordEditText);
        etUsername = findViewById(R.id.usernameEditText);
        btnSignup = findViewById(R.id.signupButton);

        vm = new ViewModelProvider(this).get(AuthViewModel.class);

        vm.getState().observe(this, ui -> {
            btnSignup.setEnabled(!ui.loading);

            if (ui.message != null) {
                Toast.makeText(this, ui.message, Toast.LENGTH_LONG).show();
            }

            if (ui.success) {
                goToIntroVideo();
            }
        });

        btnSignup.setOnClickListener(v ->
                vm.signUp(
                        getText(etEmail),
                        getText(etPassword),
                        getText(etConfirm),
                        getText(etUsername)
                )
        );
    }

    private void goToIntroVideo() {
        startActivity(new Intent(this, IntroVideoActivity.class));
        finish();
    }

    private String getText(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}

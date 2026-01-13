package com.example.fit4u;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthViewModel extends ViewModel {

    public static class UiState {
        public boolean loading;
        public boolean success;
        public String message;

        public UiState(boolean loading, boolean success, String message) {
            this.loading = loading;
            this.success = success;
            this.message = message;
        }
    }

    private final MutableLiveData<UiState> state = new MutableLiveData<>();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public LiveData<UiState> getState() {
        return state;
    }

    // LOGIN
    public void login(String email, String pass) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            state.setValue(new UiState(false, false, "נא להכניס אימייל תקין"));
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            state.setValue(new UiState(false, false, "נא להכניס סיסמה"));
            return;
        }

        state.setValue(new UiState(true, false, null));

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res ->
                        state.setValue(new UiState(false, true, "מחוברת 💖"))
                )
                .addOnFailureListener(e ->
                        state.setValue(new UiState(false, false, "שגיאה בהתחברות: " + e.getMessage()))
                );
    }

    // SIGN UP
    public void signUp(String email, String pass, String confirm, String username) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            state.setValue(new UiState(false, false, "אימייל לא תקין"));
            return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            state.setValue(new UiState(false, false, "סיסמה חייבת להיות לפחות 6 תווים"));
            return;
        }
        if (!pass.equals(confirm)) {
            state.setValue(new UiState(false, false, "הסיסמאות לא תואמות"));
            return;
        }
        if (TextUtils.isEmpty(username)) {
            state.setValue(new UiState(false, false, "נא להכניס שם משתמש"));
            return;
        }

        state.setValue(new UiState(true, false, null));

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    if (auth.getCurrentUser() == null) {
                        state.setValue(new UiState(false, false, "שגיאה: אין משתמש מחובר אחרי הרשמה"));
                        return;
                    }

                    UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();

                    auth.getCurrentUser().updateProfile(req)
                            .addOnCompleteListener(t -> {
                                if (t.isSuccessful()) {
                                    state.setValue(new UiState(false, true, "נרשמת בהצלחה ✨"));
                                } else {
                                    state.setValue(new UiState(false, true, "נרשמת! (השם לא עודכן)"));
                                }
                            });
                })
                .addOnFailureListener(e ->
                        state.setValue(new UiState(false, false, "שגיאה בהרשמה: " + e.getMessage()))
                );
    }
}

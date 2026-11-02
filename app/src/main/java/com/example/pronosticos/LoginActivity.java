package com.example.pronosticos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Idioma espanol (Latinoamerica) - Clase 10
        FirebaseAuth.getInstance().setLanguageCode("es-419");

        // Si ya inicio sesion, ir directo a MainActivity (Clase 10, pag. 30)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            irAMain();
            return;
        }

        // Layout personalizado del picker (Clase 10, pag. 39-40)
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_login_custom)
                .setGoogleButtonId(R.id.btnGoogle)
                .setEmailButtonId(R.id.btnEmail)
                .build();

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                .setAuthMethodPickerLayout(customLayout)
                //.setIsSmartLockEnabled(false)   // evita error de smartlock (Clase 10, pag. 28)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            irAMain();
        } else {
            Toast.makeText(this, "Inicio de sesion cancelado", Toast.LENGTH_SHORT).show();
        }
    }

    private void irAMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
package com.example.pronosticos;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pronosticos.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Si no hay sesion, volver al login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Crear canal de notificaciones (Clase 08.1)
        NotificationHelper.crearCanal(this);

        // Fragment por defecto
        cargarFragment(new PronosticosFragment());

        BottomNavigationView nav = binding.bottomNavigation;
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pronosticos) {
                cargarFragment(new PronosticosFragment());
                return true;
            } else if (id == R.id.nav_estadisticas) {
                cargarFragment(new EstadisticasFragment());
                return true;
            } else if (id == R.id.nav_logout) {
                cerrarSesion();
                return true;
            }
            return false;
        });
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void cerrarSesion() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
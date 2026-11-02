package com.example.pronosticos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pronosticos.databinding.ActivityFormularioBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FormularioActivity extends AppCompatActivity {

    private ActivityFormularioBinding binding;
    private CollectionReference ref;
    private boolean modoEditar = false;
    private String idDocumento;
    private Calendar fechaSeleccionada = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormularioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid).collection("pronosticos");

        // Spinner de estados (solo editable en modo edicion)
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Pendiente", "Acertado", "Fallado"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEstado.setAdapter(spinnerAdapter);

        // Selector de fecha con DatePickerDialog
        binding.btnFecha.setOnClickListener(v -> mostrarDatePicker());

        // Detectar modo editar
        if ("editar".equals(getIntent().getStringExtra("modo"))) {
            modoEditar = true;
            idDocumento = getIntent().getStringExtra("id");
            binding.tvTitulo.setText("Editar pronostico");
            binding.spinnerEstado.setVisibility(View.VISIBLE);

            binding.etSeleccionA.setText(getIntent().getStringExtra("seleccionA"));
            binding.etSeleccionB.setText(getIntent().getStringExtra("seleccionB"));
            binding.etGolesA.setText(String.valueOf(getIntent().getIntExtra("golesA", 0)));
            binding.etGolesB.setText(String.valueOf(getIntent().getIntExtra("golesB", 0)));
            fechaSeleccionada.setTimeInMillis(getIntent().getLongExtra("fecha", System.currentTimeMillis()));
            String estado = getIntent().getStringExtra("estado");
            binding.spinnerEstado.setSelection(
                    estado.equals("Acertado") ? 1 : estado.equals("Fallado") ? 2 : 0);
        }
        actualizarBotonFecha();

        binding.btnGuardar.setOnClickListener(v -> guardar());
    }

    private void mostrarDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            fechaSeleccionada.set(year, month, dayOfMonth);
            actualizarBotonFecha();
        }, fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH),
                fechaSeleccionada.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void actualizarBotonFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.btnFecha.setText("Fecha del partido: " + sdf.format(fechaSeleccionada.getTime()));
    }

    private void guardar() {
        String selA = binding.etSeleccionA.getText().toString().trim();
        String selB = binding.etSeleccionB.getText().toString().trim();
        String sGolesA = binding.etGolesA.getText().toString().trim();
        String sGolesB = binding.etGolesB.getText().toString().trim();

        // Validaciones del enunciado
        if (selA.isEmpty() || selB.isEmpty() || sGolesA.isEmpty() || sGolesB.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selA.equalsIgnoreCase(selB)) {
            Toast.makeText(this, "Las selecciones no pueden ser iguales",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int golesA, golesB;
        try {
            golesA = Integer.parseInt(sGolesA);
            golesB = Integer.parseInt(sGolesB);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Los goles deben ser numeros enteros",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (golesA < 0 || golesB < 0) {
            Toast.makeText(this, "Los goles no pueden ser negativos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp fecha = new Timestamp(fechaSeleccionada.getTime());

        if (!modoEditar) {
            // REGISTRAR: estado siempre "Pendiente" (lo pone el constructor)
            Pronostico p = new Pronostico(selA, selB, fecha, golesA, golesB);
            ref.add(p).addOnSuccessListener(doc -> {
                NotificationHelper.notificar(this, "Pronostico registrado",
                        selA + " " + golesA + " - " + golesB + " " + selB);
                finish();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show());
        } else {
            // EDITAR: se permite fijar Acertado/Fallado (queda bloqueado despues)
            Pronostico p = new Pronostico(selA, selB, fecha, golesA, golesB);
            p.setEstado(binding.spinnerEstado.getSelectedItem().toString());
            ref.document(idDocumento).set(p).addOnSuccessListener(unused -> {
                NotificationHelper.notificar(this, "Pronostico actualizado",
                        selA + " vs " + selB + " -> " + p.getEstado());
                finish();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
        }
    }
}